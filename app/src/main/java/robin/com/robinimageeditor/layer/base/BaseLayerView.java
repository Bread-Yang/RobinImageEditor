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
import robin.com.robinimageeditor.view.ActionFrameLayout;

/**
 * All photo edit layer base on this view.
 *
 * Created by Robin Yang on 12/28/17.
 */

public abstract class BaseLayerView<T extends SaveStateMarker> extends View
        implements LayerTransformer, OnPhotoRectUpdateListener, LayerCacheNode {

    private static final String TAG = "BaseLayerView";

    /**
     * Support matrix for drawing layerView, PhotoView放大、缩小、移动等等操作不改变supportMatrix,
     * 只改变rootLayerMatrix.只有当图片裁剪了，才会改变supportMatrix
     * {@link CropHelper#resetEditorSupportMatrix}
     */
    protected final Matrix supportMatrix = new Matrix();
    /**
     * PhotoView放大、缩小、移动等等操作会改变此值(PhotoAttacher持有RootEditorDelegate的引用，RootEditorDelegate持有
     * 各个BaseLayerView的引用，PhotoView操作最后导致BaseLayerView.resetEditorSupportMatrix()被调用)
     * {@link robin.com.robinimageeditor.layer.photoview.PhotoViewAttacher#setImageViewMatrix}
     */
    protected final Matrix rootLayerMatrix = new Matrix();
    protected final RectF validateRect = new RectF();

    /* support drawing */
    protected Bitmap displayBitmap;
    /**
     * Canvas that draw on {@link displayBitmap}
     */
    protected Canvas displayCanvas;

    /* saveState Info */
    protected ArrayMap<String, T> saveStateMap = new ArrayMap<>();

    /* gesture */
    protected CustomGestureDetector gestureDetector;
    protected AccelerateDecelerateInterpolator adInterpolator = new AccelerateDecelerateInterpolator();

    /* not used */
    protected Paint maskPaint;

    /* 当前layer是否能拦截触控事件 */
    private boolean isLayerInEditMode;
    protected Matrix unitMatrix = new Matrix();
    /** view是否已经加载完*/
    protected boolean viewIsLayout;

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
        viewIsLayout = true;
    }

    @Override
    public void onPhotoRectUpdate(RectF rect, Matrix matrix) {
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
    public void onDrag(float dx, float dy, float x, float y, boolean rootLayer) {

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

            if (viewIsLayout) {
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
        matrix.set(supportMatrix);
        matrix.postConcat(rootLayerMatrix);
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
