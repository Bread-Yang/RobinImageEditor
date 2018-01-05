package robin.com.robinimageeditor.layer;

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

import robin.com.robinimageeditor.bean.EditorCacheData;
import robin.com.robinimageeditor.bean.LayerEditResult;
import robin.com.robinimageeditor.bean.SaveStateMarker;
import robin.com.robinimageeditor.util.Utils;

/**
 * Created by Robin Yang on 12/28/17.
 */

public abstract class BaseLayerView<T extends SaveStateMarker> extends View
        implements LayerTransformer, OnPhotoRectUpdateListener, LayerCacheNode {

    private static final String TAG = "BaseLayerView";

    /*support matrix for drawing layerView*/
    private Matrix drawMatrix;
    protected final Matrix supportMatrix = new Matrix();
    protected final Matrix rootLayerMatrix = new Matrix();
    protected final RectF validateRect = new RectF();

    /*support drawing*/
    protected Bitmap displayBitmap;
    protected Canvas displayCanvas;

    /*saveState Info*/
    protected ArrayMap<String, T> saveStateMap = new ArrayMap<>();

    /*gesture*/
    protected CustomGestureDetector gestureDetector;
    protected AccelerateDecelerateInterpolator adInterpolator = new AccelerateDecelerateInterpolator();

    /*paint*/
    protected Paint maskPaint;

    /*operation*/
    private boolean isLayerInEditMode;
    protected Matrix unitMatrix = new Matrix();
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
        Utils.recycleBitmap(displayBitmap);
        displayCanvas = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // drawDisplay
        if (displayBitmap != null) {
            if (clipRect()) {
                canvas.save();
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
//        val diffs = Utils.diffRect(layerRect, validateRect)
//        for (rect in diffs) {
//            canvas.drawRect(Utils.mapInvertMatrixRect(getDrawMatrix(), rect), maskPaint)
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
     * open fun for intercept touch event or not.
     * if intercept this layer will handle it,other wise do nothing
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
    abstract void drawAllCachedState(Canvas canvas);

    protected void initSupportView(Context context) {

    }

    /**
     * ui element undo clicked
     */
    public void revoke() {

    }

    //region of save and restore data
    public LayerEditResult getEditorResult() {
        return new LayerEditResult(supportMatrix, displayBitmap);
    }

    //cache layer data.
    @Override
    public void saveLayerData(HashMap<String, EditorCacheData> output) {
        output.put(getLayerTag(), new EditorCacheData(new ArrayMap<String, SaveStateMarker>(saveStateMap)));
    }

    @Override
    public void restoreLayerData(HashMap<String, EditorCacheData> input) {
        EditorCacheData lastCache = input.get(getLayerTag());
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

    public void setLayerInEditMode(boolean layerInEditMode) {
        isLayerInEditMode = layerInEditMode;
    }
}
