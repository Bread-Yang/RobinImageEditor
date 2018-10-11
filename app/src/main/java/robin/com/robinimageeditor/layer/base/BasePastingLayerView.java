package robin.com.robinimageeditor.layer.base;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import robin.com.robinimageeditor.data.savestate.PastingSaveStateMarker;
import robin.com.robinimageeditor.data.share.SharableData;
import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * Created by Robin Yang on 1/4/18.
 */

public abstract class BasePastingLayerView<T extends PastingSaveStateMarker> extends BaseLayerView<T> {

    private RectF dragViewRect;

    protected boolean pastingOutOfBound;
    protected boolean pastingDoubleClick;
    protected T currentPastingState;
    protected HidePastingOutOfBoundsRunnable hidePastingOutOfBoundsRunnable;

    /* pasting always in editMode*/
    protected Paint focusRectPaint;
    protected Paint focusRectCornerPaint;
    protected float focusRectCornerWidth;

    protected OnOperateCallback mCallback;

    public interface OnOperateCallback {
        void showOrHideDragCallback(boolean b);

        void setOrNotDragCallback(boolean b);

        void onLayerViewDoubleClick(View view, SharableData sharableData);
    }

    public BasePastingLayerView(Context context) {
        super(context);
        init();
    }

    public BasePastingLayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BasePastingLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BasePastingLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        // 继承于BasePastingLayerView的子类默认是可拦截触控事件
        setLayerInEditMode(true);

        dragViewRect = new RectF();
        hidePastingOutOfBoundsRunnable = new HidePastingOutOfBoundsRunnable();
    }

    @Override
    protected void initSupportView(Context context) {
        super.initSupportView(context);

        // focus rect paint
        focusRectPaint = new Paint();
        focusRectPaint.setStyle(Paint.Style.STROKE);
        focusRectPaint.setAntiAlias(true);
        focusRectPaint.setStrokeWidth(2f);
        focusRectPaint.setColor(Color.WHITE);

        // focusCornerRect
        focusRectCornerWidth = MatrixUtils.dp2px(context, 2f);
        focusRectCornerPaint = MatrixUtils.copyPaint(focusRectPaint);
        focusRectCornerPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void drawMask(Canvas canvas) {
        if (pastingOutOfBound) {
            if (currentPastingState != null) {
                drawPastingState(currentPastingState, canvas);
            }
        }
        drawFocusDecorate(canvas);
    }

    protected void drawFocusDecorate(Canvas canvas) {
        if (currentPastingState != null) {
            RectF initDisplayRectF = currentPastingState.getInitDisplayRect();
            float[] polygonPoint = new float[8];

            // left_top
            polygonPoint[0] = initDisplayRectF.left;
            polygonPoint[1] = initDisplayRectF.top;

            // top_right
            polygonPoint[2] = initDisplayRectF.right;
            polygonPoint[3] = initDisplayRectF.top;

            // bottom_right
            polygonPoint[4] = initDisplayRectF.right;
            polygonPoint[5] = initDisplayRectF.bottom;

            // bottom_left
            polygonPoint[6] = initDisplayRectF.left;
            polygonPoint[7] = initDisplayRectF.bottom;

            currentPastingState.getTransformMatrix().mapPoints(polygonPoint);

            Path path = new Path();
            path.moveTo(polygonPoint[0], polygonPoint[1]);
            path.lineTo(polygonPoint[2], polygonPoint[3]);
            path.lineTo(polygonPoint[4], polygonPoint[5]);
            path.lineTo(polygonPoint[6], polygonPoint[7]);
            path.close();

            canvas.drawPath(path, focusRectPaint);

            // draw bigRect corner's smallRect
            drawFocusRectCornerRect(canvas, initDisplayRectF.left, initDisplayRectF.top);
            drawFocusRectCornerRect(canvas, initDisplayRectF.right, initDisplayRectF.top);
            drawFocusRectCornerRect(canvas, initDisplayRectF.right, initDisplayRectF.bottom);
            drawFocusRectCornerRect(canvas, initDisplayRectF.left, initDisplayRectF.bottom);

//            RectF display = getStateDisplayRect(currentPastingState, false);
//            canvas.drawRect(display, focusRectPaint);

            // rect corner rect
//            drawFocusRectCornerRect(canvas, display.left, display.top);
//            drawFocusRectCornerRect(canvas, display.right, display.top);
//            drawFocusRectCornerRect(canvas, display.right, display.bottom);
//            drawFocusRectCornerRect(canvas, display.left, display.bottom);
        }
    }

    protected void drawFocusRectCornerRect(Canvas canvas, float centerX, float centerY) {
        RectF rect = new RectF();
        MatrixUtils.RectFSchedule(rect, centerX, centerY, focusRectCornerWidth, focusRectCornerWidth);

        float[] polygonPoint = new float[8];

        // left_top
        polygonPoint[0] = rect.left;
        polygonPoint[1] = rect.top;

        // top_right
        polygonPoint[2] = rect.right;
        polygonPoint[3] = rect.top;

        // bottom_right
        polygonPoint[4] = rect.right;
        polygonPoint[5] = rect.bottom;

        // bottom_left
        polygonPoint[6] = rect.left;
        polygonPoint[7] = rect.bottom;

        currentPastingState.getTransformMatrix().mapPoints(polygonPoint);

        Path path = new Path();
        path.moveTo(polygonPoint[0], polygonPoint[1]);
        path.lineTo(polygonPoint[2], polygonPoint[3]);
        path.lineTo(polygonPoint[4], polygonPoint[5]);
        path.lineTo(polygonPoint[6], polygonPoint[7]);
        path.close();

        canvas.drawPath(path, focusRectCornerPaint);
//        canvas.drawRect(rect, focusRectCornerPaint);
    }

    @Override
    protected boolean checkInterceptedOnTouchEvent(MotionEvent event) {
        super.checkInterceptedOnTouchEvent(event);

        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            T downState = getFingerDownState(event.getX(), event.getY());
            if (downState != null && downState == currentPastingState) {
                pastingDoubleClick = true;
            }
            currentPastingState = downState;
            if (currentPastingState != null) {
                checkDisplayRegion(currentPastingState);
                saveStateMap.remove(currentPastingState.getId());
                saveStateMap.put(currentPastingState.getId(), currentPastingState);
//                currentPastingState.getInitDisplayMatrix().set(currentPastingState.getTransformMatrix());
//                currentPastingState.getInitDisplayMatrix().postConcat(getDrawMatrix());
                redrawAllCache();
            }
            if (currentPastingState == null) {
                return false;
            }
        }

        return true;
    }

    protected T getFingerDownState(float downX, float downY) {
        for (int i = saveStateMap.size() - 1; i >= 0; i--) {
            T state = saveStateMap.valueAt(i);
//            RectF displayRect = getStateDisplayRect(state, true);
//            if (displayRect.contains(downX, downY)) {
//                return state;
//            }
            if (containsTouchPoint(state, (int) downX, (int) downY)) {
                return state;
            }
        }
        return null;
    }

    private void checkDisplayRegion(PastingSaveStateMarker state) {
        RectF rect = getStateDisplayRect(state, true);
        pastingOutOfBound = !validateRect.contains(rect);
    }

    private void checkDisplayRegion(RectF display) {
        pastingOutOfBound = !validateRect.contains(display);
    }

    protected boolean containsTouchPoint(PastingSaveStateMarker state, int x, int y) {
        Path path = new Path();
        path.addRect(state.getInitDisplayRect(), Path.Direction.CW);

        Matrix finalMatrix = new Matrix();
        finalMatrix.set(state.getTransformMatrix());
        finalMatrix.postConcat(getDrawMatrix());
        path.transform(finalMatrix);

        RectF rectF = new RectF();
        path.computeBounds(rectF, true);

        Region region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));

        if (region.contains(x, y)) {
            return true;
        }
        return false;
    }

    protected RectF getStateDisplayRect(PastingSaveStateMarker state, boolean realDisplay) {
        Matrix finalMatrix = new Matrix();
        finalMatrix.set(state.getTransformMatrix());
        if (realDisplay) {
            finalMatrix.postConcat(getDrawMatrix());
        }
        RectF displayRect = new RectF();
        finalMatrix.mapRect(displayRect, state.getInitDisplayRect());
        return displayRect;
    }

    /* gesture state detect with viewValidate */

    @Override
    public void onFingerDown(float downX, float downY) {
        removeCallbacks(hidePastingOutOfBoundsRunnable);
    }

    @Override
    public void onDrag(float dx, float dy, float x, float y, boolean rootLayer) {
        if (!rootLayer) {
            pastingDoubleClick = false;
            if (currentPastingState != null) {
                if (x != -1 || y != -1) {
                    if (mCallback != null) {
                        mCallback.showOrHideDragCallback(true);
                    }
                    // calc
                    float[] invert = MatrixUtils.mapInvertMatrixTranslate(getDrawMatrix(), dx, dy);
                    currentPastingState.getTransformMatrix().postTranslate(invert[0], invert[1]);
                    RectF displayRect = getStateDisplayRect(currentPastingState, true);
                    checkDisplayRegion(displayRect);
                    // setStates
                    if (mCallback != null) {
                        mCallback.setOrNotDragCallback(!dragViewRect.contains(displayRect.centerX(), displayRect.centerY()));
                    }
                    redrawAllCache();
                }
            }
        }
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY, boolean rootLayer) {
        if (!rootLayer) {
            if (currentPastingState != null) {
                float[] invert = MatrixUtils.mapInvertMatrixScale(getDrawMatrix(), scaleFactor, scaleFactor);
                checkDisplayRegion(currentPastingState);
//                currentPastingState.getTransformMatrix().postScale(invert[0], invert[1], focusX, focusY);

                float[] points = new float[2];
                RectF initDisplayRectF = currentPastingState.getInitDisplayRect();
                points[0] = initDisplayRectF.centerX();
                points[1] = initDisplayRectF.centerY();
                currentPastingState.getTransformMatrix().mapPoints(points);

                currentPastingState.getTransformMatrix().postScale(invert[0], invert[1],
                        points[0], points[1]);
                redrawAllCache();
            }
        }
    }

    @Override
    public void onRotate(float rotateDegree, float focusX, float focusY, boolean rootLayer) {
        if (!rootLayer) {
            if (currentPastingState != null) {
//                currentPastingState.getTransformMatrix().postRotate(rotateDegree, focusX, focusY);

                float[] points = new float[2];

                RectF initDisplayRectF = currentPastingState.getInitDisplayRect();
                Matrix transformMatrix = currentPastingState.getTransformMatrix();

                points[0] = initDisplayRectF.centerX();
                points[1] = initDisplayRectF.centerY();

                transformMatrix.mapPoints(points);
                currentPastingState.getTransformMatrix().postRotate(rotateDegree, points[0], points[1]);
                redrawAllCache();
            }
        }
    }

    @Override
    public void onFingerUp(float upX, float upY) {
        if (mCallback != null) {
            mCallback.showOrHideDragCallback(false);
        }
        // edit mode
        if (pastingDoubleClick && currentPastingState != null) {
            onPastingDoubleClick(currentPastingState);
        }
        pastingDoubleClick = false;
        // remove mode, rebound mode
        if (currentPastingState != null) {
            RectF displayRect = getStateDisplayRect(currentPastingState, true);
            boolean delete = dragViewRect.contains(displayRect.centerX(), displayRect.centerY());
            // remove
            if (delete) {
                saveStateMap.remove(currentPastingState.getId());
                currentPastingState = null;
                redrawAllCache();
            } else {
                // rebound
                if (!validateRect.contains(displayRect.centerX(), displayRect.centerY())) {
                    Matrix initEventMatrix = currentPastingState.getInitDisplayMatrix();
                    Matrix currentMatrix = new Matrix();
                    currentMatrix.set(currentPastingState.getTransformMatrix());
                    currentMatrix.postConcat(getDrawMatrix());
                    float dx = MatrixUtils.getMatrixTransX(currentMatrix) - MatrixUtils.getMatrixTransX(initEventMatrix);
                    float dy = MatrixUtils.getMatrixTransY(currentMatrix) - MatrixUtils.getMatrixTransY(initEventMatrix);
                    rebound(dx, dy);
                }
//                currentPastingState.getInitDisplayMatrix().reset();
            }
        }
        // hide extra validate rect (over mValidateRect should be masked)
        hideExtraValidateRect();
    }

    protected void hideExtraValidateRect() {
        postDelayed(hidePastingOutOfBoundsRunnable, 1500);
    }

    private void rebound(float dx, float dy) {
        new OverBoundRunnable(dx, dy).run();
    }

    public void setCallback(OnOperateCallback callback) {
        this.mCallback = callback;
    }

    class HidePastingOutOfBoundsRunnable implements Runnable {

        @Override
        public void run() {
            recover2ValidateRect();
        }
    }

    private void recover2ValidateRect() {
        currentPastingState = null;
        pastingOutOfBound = false;
        redrawAllCache();
    }

    @Override
    protected void drawAllCachedState(Canvas canvas) {
        for (int i = 0; i < saveStateMap.size(); i++) {
            T state = saveStateMap.valueAt(i);
            drawPastingState(state, canvas);
        }
    }

    public void setDragViewRect(RectF dragViewRect) {
        this.dragViewRect = dragViewRect;
    }

    @Override
    public void redrawOnPhotoRectUpdate() {
        redrawAllCache();
    }

    protected abstract void onPastingDoubleClick(T state);

    protected abstract void drawPastingState(T state, Canvas canvas);
}
