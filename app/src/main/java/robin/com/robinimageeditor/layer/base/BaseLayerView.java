package robin.com.robinimageeditor.layer.base;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import robin.com.robinimageeditor.data.savestate.SaveStateMarker;
import robin.com.robinimageeditor.data.share.LayerEditResult;
import robin.com.robinimageeditor.editcache.LayerEditCache;
import robin.com.robinimageeditor.layer.base.detector.CustomGestureDetector;
import robin.com.robinimageeditor.layer.crop.CropHelper;
import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * 看看自己github上的另一个工程MatrixPractice : https://github.com/Bread-Yang/MatrixPractice
 * 安卓自定义View进阶-Matrix原理 : https://www.gcssloop.com/customview/Matrix_Basic.html
 * 安卓自定义View进阶-Matrix详解 : https://www.gcssloop.com/customview/Matrix_Method.html
 * matrix最全方法详解与进阶: https://cloud.tencent.com/developer/article/2384173
 * All photo edit layer base on this view.
 *
 * Created by Robin Yang on 12/28/17.
 */

public abstract class BaseLayerView<T extends SaveStateMarker> extends View
        implements LayerTransformer, OnPhotoRectUpdateListener, LayerCacheNode {

    private static final String TAG = "BaseLayerView";

    /**
     * matrix最全方法详解与进阶: https://cloud.tencent.com/developer/article/2384173
     *
     * Support matrix for drawing layerView
     * PhotoView放大、缩小、移动等等操作不改变supportMatrix,只改变rootLayerMatrix.
     * 只有当图片裁剪了，才会改变supportMatrix
     * (PhotoAttacher持有RootEditorDelegate的引用，RootEditorDelegate持有各个BaseLayerView的引用，PhotoView操作最后导致BaseLayerView.resetEditorSupportMatrix()被调用)
     * {@link CropHelper#resetEditorSupportMatrix}
     */
    protected final Matrix supportMatrix = new Matrix();
    /**
     * PhotoView放大、缩小、移动等等操作会改变此值
     * {@link robin.com.robinimageeditor.layer.photoview.PhotoViewAttacher#setImageViewMatrix}
     */
    protected final Matrix rootLayerMatrix = new Matrix();
    /**
     * 原图显示出来的区域, 也就是validateRect以外的区域, 不显示
     * 在{@link BaseLayerView#onDraw}方法里, 调用canvas.clipRect(validateRect);
     * 调用canvas.drawXXX()后, 能显示出来drawXXX效果的Rect区域
     * PhotoView放大、缩小、移动等等操作会改变此值
     */
    protected final RectF validateRect = new RectF();

    /**
     * 通过{@link displayCanvas}, 将所有编辑记录画到该displayBitmap上
     * 然后在{@link BaseLayerView#onDraw}方法里, 通过canvas.drawBitmap(displayBitmap, getDrawMatrix(), null)方法调用, 在经过getDrawMatrix()的变换后, 画到canvas上
     */
    protected Bitmap displayBitmap;
    /**
     * 该displayCanvas由displayBitmap生成, 所有的历史编辑记录, 都在displayCanvas上画
     * displayCanvas = new Canvas(displayBitmap)
     * 每个BaseLayerView都有自己的一层displayCanvas, 生成编辑图片时, 再叠加
     * Canvas that draw on {@link displayBitmap}
     */
    protected Canvas displayCanvas;

    /**
     * 历史操作
     * saveState Info
     */
    protected ArrayMap<String, T> saveStateMap = new ArrayMap<>();

    /* gesture */
    protected CustomGestureDetector gestureDetector;
    protected AccelerateDecelerateInterpolator adInterpolator = new AccelerateDecelerateInterpolator();

    /* not used */
    protected Paint maskPaint;

    /* 当前layer是否能拦截触控事件 */
    private boolean isLayerInEditMode;
    /**
     * 单位矩阵，是一个对角线上的元素全为1，其他元素全为0的标量矩阵
     */
    protected Matrix unitMatrix = new Matrix();
    /** view是否已经加载完*/
    protected boolean viewIsAlreadyLayout;

    public BaseLayerView(Context context) {
        super(context);
        initView(context);
    }

    public BaseLayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public BaseLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    protected void initView(Context context) {
        gestureDetector = new CustomGestureDetector(context, this);
        // maskPaint
        maskPaint = new Paint();
        maskPaint.setStyle(Paint.Style.FILL);
        maskPaint.setAntiAlias(true);
        maskPaint.setColor(Color.BLACK);
        initSupportView(context);
        if (this instanceof BasePaintLayerView) {
//            setBackgroundColor(Color.parseColor("#40001111"));
//            setBackgroundColor(Color.BLUE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MatrixUtils.recycleBitmap(displayBitmap);
        displayCanvas = null;
    }

    @CallSuper
    @Override
    protected void onDraw(Canvas canvas) {
        // drawDisplay
        if (displayBitmap != null) {
            if (clipRect()) {
                canvas.save();
                // 该方法用于裁剪画布，也就是设置画布的显示区域
                // https://blog.csdn.net/u010015108/article/details/52817431
                // 比如调用了canvas.clipRect(0, 0, getWidth()/2, getHeight()/2);
                // 这行代码将画布的绘制区域限制到了屏幕的左上角的四分之一的区域中
                canvas.clipRect(validateRect);
                canvas.drawBitmap(displayBitmap, getDrawMatrix(), null);
                canvas.restore();
            } else {
                canvas.drawBitmap(displayBitmap, getDrawMatrix(), null);
            }
        }
        // drawExtra
        canvas.save();
        canvas.setMatrix(getDrawMatrix());
        drawMask(canvas);
        canvas.setMatrix(unitMatrix);
        canvas.restore();
    }

    public boolean clipRect() {
        return true;
    }

    // 子类BasePastingLayerView用到，用于画正在编辑的历史记录的高亮白色框框
    protected void drawMask(Canvas canvas) {
//        val layerRect = RectF(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat())
//        val diffs = MatrixUtils.diffRect(layerRect, validateRect)
//        for (rect in diffs) {
//            canvas.drawRect(MatrixUtils.mapInvertMatrixRect(getDrawMatrix(), rect), maskPaint)
//        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (validateRect.isEmpty()) {
            validateRect.set(left, top, right, bottom);
        }
        viewIsAlreadyLayout = true;
    }

    // PhotoView缩放平移时, 回调该方法
    @Override
    public void onPhotoRectUpdate(RectF rect, Matrix matrix) {
        Log.e(TAG, "validateRect的宽高, width : " + rect.width() + ", height : " + rect.height());
        Log.e(TAG, "validateRect的中心, x : " + rect.centerX() + ", y : " + rect.centerY());
        validateRect.set(rect);
        rootLayerMatrix.set(matrix);
        redrawOnPhotoRectUpdate();
    }

    protected void genDisplayCanvas() {
        if (displayBitmap == null) {
            displayBitmap = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            displayCanvas = new Canvas(displayBitmap);
        }
    }

    // 剪裁区域变化时, 回调该方法
    @Override
    public void resetEditorSupportMatrix(Matrix matrix) {
        supportMatrix.set(matrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;

        if (isLayerInEditMode) {
            return checkInterceptedOnTouchEvent(event) && gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFingerUp(float upX, float upY) {
        Log.e(TAG, "onFingerUp()");
    }

    @Override
    public void onFingerDown(float downX, float downY) {
        Log.e(TAG, "onFingerDown()");
    }

    @Override
    public void onFingerCancel() {

    }

    @Override
    public void onDrag(float dx, float dy, float x, float y, boolean isRootLayer) {

    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY, boolean rootLayer) {
        Log.e(TAG, "onScale()");
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY, boolean rootLayer) {

    }

    @Override
    public void cancelFling(boolean rootLayer) {

    }

    @Override
    public void onRotate(float rotateDegree, float focusX, float focusY, boolean rootLayer) {
        Log.e(TAG, "onRotate(), rotateDegree : " + rotateDegree);
    }

    protected class OverBoundRunnable implements Runnable {

        float dx, dy;

        long mStartTime = System.currentTimeMillis();
        int mZoomDuration = 300;
        float mLastDiffX = 0f;
        float mLastDiffY = 0f;

        public OverBoundRunnable(float dx, float dy) {
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public void run() {
            float t = interpolate();
            float ddx = t * dx - mLastDiffX;
            float ddy = t * dy - mLastDiffY;
            onDrag(-ddx, -ddy, -1f, -1f, false);
            mLastDiffX = t * dx;
            mLastDiffY = t * dy;
            if (t < 1f) {
                ViewCompat.postOnAnimation(BaseLayerView.this, this);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = adInterpolator.getInterpolation(t);
            return t;
        }
    }

    /**
     * method for intercept touch event or not.
     * if intercept this layer will handle it,otherwise do nothing
     */
    protected boolean checkInterceptedOnTouchEvent(MotionEvent event) {
        return true;
    }

    public void onStartCompose() {

    }

    public void redrawOnPhotoRectUpdate() {
        invalidate();
    }

    protected void redrawAllCache() {
        if (!saveStateMap.isEmpty()) {
            genDisplayCanvas();
        }
        if (displayCanvas != null) {
            displayCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            /**
             * 这里重画的所有历史编辑记录的x,y坐标, 都是在通过getDrawMatrix()的逆矩阵计算, 得到原图在没有做过任何缩放,平移,裁剪操作情况下的x',y'坐标
             * 在{@link BaseLayerView#onDraw}方法调用时,
             * 1.先把所有的path, 通过{@link BaseLayerView#displayCanvas}, 画在没有经过任何变换操作的{@link BaseLayerView#displayBitmap}上
             * 2.再把{@link BaseLayerView#displayBitmap}, 通过canvas.drawBitmap(displayBitmap, getDrawMatrix(), null), 在经过getDrawMatrix()的变换后, 画到canvas上
             * 3.因为都是在displayBitmap上画的，所以在validateRect范围外的历史编辑记录是无法显示出来(因为在{@link BaseLayerView#onDraw}里，调用了canvas.clipRect(validateRect);)
             */
            drawAllCachedState(displayCanvas);
        }
        postInvalidate();
    }

    /**
     * invalidate all cached data
     */
    protected abstract void drawAllCachedState(Canvas canvas);

    protected void initSupportView(Context context) {

    }

    /**
     * ui element undo clicked
     */
    public void revoke() {

    }

    // region of save and restore data
    public LayerEditResult getEditorResult() {
        return new LayerEditResult(supportMatrix, displayBitmap);
    }

    // cache layer data.
    @Override
    public void saveLayerEditData(HashMap<String, LayerEditCache> cacheDataHashMap) {
        cacheDataHashMap.put(getLayerTag(), new LayerEditCache(new ArrayMap<String, SaveStateMarker>(saveStateMap)));
    }

    @Override
    public void restoreLayerEditData(HashMap<String, LayerEditCache> cacheDataHashMap) {
        LayerEditCache lastCache = cacheDataHashMap.get(getLayerTag());
        if (lastCache != null) {
            ArrayMap<String, T> restore = ((ArrayMap<String, T>) lastCache.getLayerCache());
            Iterator iterator = restore.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, T> entry = (Map.Entry) iterator.next();
                if (entry.getValue() != null) {
                    saveStateMap.put(entry.getKey(), (T) entry.getValue().deepCopy());
                }
            }

            if (viewIsAlreadyLayout) {
                redrawAllCache();
            } else {
                addOnLayoutChangeListener(new OnLayerLayoutListener());
            }
        }
    }

    class OnLayerLayoutListener implements OnLayoutChangeListener {

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            redrawAllCache();
            removeOnLayoutChangeListener(this);
        }
    }

    @Override
    public String getLayerTag() {
        Log.e("getSimpleName() : ", this.getClass().getSimpleName());
        return this.getClass().getSimpleName();
    }

    public Matrix getDrawMatrix() {
        Matrix matrix = new Matrix();
        // 矩阵的用法 : https://cloud.baidu.com/article/3151272
        // matrix1.postConcat(matrix2);
        // 就是将matrix1后接到matrix2
        // 注意！！！！！！不管是前乘后乘或者左乘右乘，反正知道postXXX()方法，都是按照代码顺序，从上到下执行就行，验证链接：https://blog.51cto.com/u_12395319/5715265
        // 参考MainActivity的testPre()和testPost()方法
        matrix.set(supportMatrix);
        // public boolean postConcat (Matrix other)
        // Postconcats the matrix with the specified matrix. M' = other * M
        // 这里对比PhotoViewAttacher的getDrawMatrix()顺序后的理解，PhotoViewAttacher是在做了基础矩阵mBaseMatrix变换，再做mSuppMatrix手势操作变换
        // 这里是先做supportMatrix变换，再做rootLayerMatrix变换，是因为rootLayerMatrix是手势操作PhotoView后再改变此矩阵的，所以postConcat(rootLayerMatrix)
        matrix.postConcat(rootLayerMatrix);  // 就是 rootLayerMatrix * supportMatrix, 就是先做supportMatrix变换，再做rootLayerMatrix变换
        return matrix;
    }

    /**
     * 该layer是否已经编辑过,如果编辑过，则编辑缓存 {@link saveStateMap} 大于0
     *
     * @return
     */
    public boolean hasEdited() {
        return saveStateMap.size() > 0;
    }

    public void setLayerInEditMode(boolean isLayerInEditMode) {
        this.isLayerInEditMode = isLayerInEditMode;
    }
}
