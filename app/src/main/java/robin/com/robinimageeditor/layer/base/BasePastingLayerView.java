package robin.com.robinimageeditor.layer.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.data.savestate.PastingSaveStateMarker;
import robin.com.robinimageeditor.data.share.SharableData;
import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * ## Base  pasting layerView  for [StickerView] and [TextPastingView]
 * It's hold drag info and callback of show or hide pasting removable
 * Created by Robin Yang on 1/4/18.
 */

public abstract class BasePastingLayerView<T extends PastingSaveStateMarker> extends BaseLayerView<T> {

    // BasePastingLayerView当前是否正在操作
    public static boolean sIsPastingLayerTouching = false;

    private static final String TAG = "BasePastingLayerView";

    // 拖动删除当前历史编辑记录的RectF范围, 该RectF是相对于屏幕左上角的绝对位置, 不是是相对于编辑框的左上角
    private RectF dragToDeleteViewRect;

    // 当前编辑记录的RectF中心点, 是否不在validateRect范围内, 如果不在validateRect的范围内， 则调用rebound()方法把编辑记录拉回来
    protected boolean pastingOutOfBound;
    // 双击编辑
    protected boolean pastingDoubleClick;
    // 当前正在操作的编辑记录
    protected T currentPastingState;
    protected HidePastingOutOfBoundsRunnable hidePastingOutOfBoundsRunnable;

    /* pasting always in editMode*/
    protected Paint focusRectPaint;
    protected Paint focusRectCornerPaint;
    protected float focusRectCornerWidth;

    protected OnOperateCallback mCallback;

    public static int icon_radius;
    private float mLastX, mLastY;

    /**
     * 用于处理拖动、缩放、旋转等操作的回调
     */
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
        icon_radius = MatrixUtils.dp2px(getContext(), 10f);
        // 继承于BasePastingLayerView的子类默认是可拦截触控事件
        setLayerInEditMode(true);

        dragToDeleteViewRect = new RectF();
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
                /**
                 * 这里画多一次的目的是因为在因为在{@link BaseLayerView#onDraw}里，调用了canvas.clipRect(validateRect),
                 * 因为所以的历史编辑记录是在@{@link displayBitmap}上画的, 所以超出{@link validateRect}范围外的编辑记录无法显示，
                 * 但是这里的canvas，不是@{@link displayCanvas}，而是{@link BaseLayerView#onDraw}方法上的canvas，所以
                 * {@link validateRect}范围外能显示出来
                 */
                drawPastingState(currentPastingState, canvas);
            }
        }
        drawFocusDecorate(canvas);
    }

    // 画出高亮的白色框
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

            // 将高亮白色框框画出来
            canvas.drawPath(path, focusRectPaint);

            // draw bigRect corner's smallRect
            drawFocusRectCornerRect(canvas, initDisplayRectF.left, initDisplayRectF.top);
            drawFocusRectCornerRect(canvas, initDisplayRectF.right, initDisplayRectF.top);
            drawFocusRectCornerRect(canvas, initDisplayRectF.right, initDisplayRectF.bottom);
            drawFocusRectCornerRect(canvas, initDisplayRectF.left, initDisplayRectF.bottom);

            Bitmap bitmapDelete = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_delete);
            Bitmap bitmapAdjust = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_adjust);

//            int padding = icon_radius;
//            Rect deleteRect = new Rect((int) (polygonPoint[0] - padding), (int) (polygonPoint[1] - padding), (int) (polygonPoint[0] + padding), (int) (polygonPoint[1] + padding));
//            Rect adjustRect = new Rect((int) (polygonPoint[4] - padding), (int) (polygonPoint[5] - padding), (int) (polygonPoint[4] + padding), (int) (polygonPoint[5] + padding));
//            canvas.drawBitmap(bitmapDelete, null, deleteRect, null);
//            canvas.drawBitmap(bitmapAdjust, null, adjustRect, null);
            RectF adjustRect = new RectF(currentPastingState.getAdjustIconRectF());
            currentPastingState.getTransformMatrix().mapRect(adjustRect);
            canvas.drawBitmap(bitmapAdjust, null, adjustRect, null);

//            RectF display = getStateDisplayRect(currentPastingState, false);
//            canvas.drawRect(display, focusRectPaint);

            // rect corner rect
//            drawFocusRectCornerRect(canvas, display.left, display.top);
//            drawFocusRectCornerRect(canvas, display.right, display.top);
//            drawFocusRectCornerRect(canvas, display.right, display.bottom);
//            drawFocusRectCornerRect(canvas, display.left, display.bottom);
        }
    }

    // 画高亮白色框框各个角的点
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
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = super.onTouchEvent(event);

        int action = event.getAction() & MotionEvent.ACTION_MASK;

        Log.e(TAG, "action : " + action);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (result) {
                    sIsPastingLayerTouching = true;
                }
                break;
            case MotionEvent.ACTION_UP:
                sIsPastingLayerTouching = false;
                break;
        }

        return result;
    }

    @Override
    protected boolean checkInterceptedOnTouchEvent(MotionEvent event) {
        super.checkInterceptedOnTouchEvent(event);

        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            // 当前手指触控的位置，是否有历史编辑记录
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
            boolean isTouchAdjustIcon = containsAdjustTouchPoint(state, (int) downX, (int) downY);
            state.setAdjustPointTouch(isTouchAdjustIcon);
            if (isTouchAdjustIcon || containsTouchPoint(state, (int) downX, (int) downY)) {
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

        // 因为PastingSaveStateMarker的坐标都是在原图没有经过任何变换操作的原始x，y坐标，所以这里要再次乘上matrix
        Matrix finalMatrix = new Matrix();
        finalMatrix.set(state.getTransformMatrix());
        // public boolean postConcat (Matrix other)
        // Postconcats the matrix with the specified matrix. M' = other * M
        finalMatrix.postConcat(getDrawMatrix());  // 这里就是getDrawMatrix() * state.getTransformMatrix(), 先做state.getTransformMatrix()矩阵变换，再做getDrawMatrix()变化
        path.transform(finalMatrix);

        RectF rectF = new RectF();
        path.computeBounds(rectF, true);

        // Region 表示一个范围，它可以由0个或多个矩形组成
        Region region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));

        if (region.contains(x, y)) {
            return true;
        }
        return false;
    }

    protected boolean containsAdjustTouchPoint(PastingSaveStateMarker state, int x, int y) {
        Path path = new Path();
        path.addRect(state.getAdjustIconRectF(), Path.Direction.CW);

        // 因为PastingSaveStateMarker的坐标都是在原图没有经过任何变换操作的原始x，y坐标，所以这里要再次乘上matrix
        Matrix finalMatrix = new Matrix();
        finalMatrix.set(state.getTransformMatrix());
        // public boolean postConcat (Matrix other)
        // Postconcats the matrix with the specified matrix. M' = other * M
        finalMatrix.postConcat(getDrawMatrix());  // 这里就是getDrawMatrix() * state.getTransformMatrix(), 先做state.getTransformMatrix()矩阵变换，再做getDrawMatrix()变化
        path.transform(finalMatrix);

        RectF rectF = new RectF();
        path.computeBounds(rectF, true);

        // Region 表示一个范围，它可以由0个或多个矩形组成，可以检测xy坐标是否在不规则图形里
        Region region = new Region();
        region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));

        if (region.contains(x, y)) {
            return true;
        }
        return false;
    }

    /**
     * 获取编辑历史的显示范围Rect
     *
     * @param state
     * @param realDisplay 如果是true, 则返回相对于手机左上角的位置(transformMatrix * drawMatrix); 如果是false, 则返回相对于图片(编辑框)左上角的位置(做了transformMatrix变换的位置)
     * @return
     */
    protected RectF getStateDisplayRect(PastingSaveStateMarker state, boolean realDisplay) {
        Matrix finalMatrix = new Matrix();
        finalMatrix.set(state.getTransformMatrix());
        if (realDisplay) {
            // PastingSaveStateMarker的getDrawMatrix() * TransformMatrix
            finalMatrix.postConcat(getDrawMatrix()); // 这里就是getDrawMatrix() * state.getTransformMatrix(), 先做state.getTransformMatrix()矩阵变换，再做getDrawMatrix()变化
        }
        RectF displayRect = new RectF();
        finalMatrix.mapRect(displayRect, state.getInitDisplayRect());
        return displayRect;
    }

    /* gesture state detect with viewValidate */

    @Override
    public void onFingerDown(float downX, float downY) {
        mLastX = downX;
        mLastY = downY;
        removeCallbacks(hidePastingOutOfBoundsRunnable);
    }

    @Override
    public void onDrag(float dx, float dy, float x, float y, boolean isRootLayer) {
        if (!isRootLayer) {
            pastingDoubleClick = false;
            if (currentPastingState != null) {
                if (x != -1 || y != -1) {
                    if (mCallback != null) {
                        mCallback.showOrHideDragCallback(true);
                    }
                }
                // 做缩放旋转操作
                if (currentPastingState.isAdjustPointTouch()) {
                    float[] points = new float[2];
                    points[0] = currentPastingState.getInitDisplayRect().centerX();
                    points[1] = currentPastingState.getInitDisplayRect().centerY();
                    currentPastingState.getTransformMatrix().mapPoints(points);

                    float centerX = points[0];
                    float centerY = points[1];

                    // 这里InitDisplayRect的中心点，是在canvas没有做过任何缩放操作的情况下的坐标
                    // 所以为了得到跟触控坐标系一致的坐标，这里需要用getDrawMatrix()*TransformMatrix后再映射
                    // 就能得到坐标系一致的真实中心点了
                    // 其实这里还有一种做法，就是将mLastX, mLastY, x, y通过getDrawMatrix()*TransformMatrix的逆矩阵,
                    // 转换成没有变化前的坐标，这样就可以直接用InitDisplayRect的中心点来计算了
                    float[] realPoints = new float[2];
                    realPoints[0] = currentPastingState.getInitDisplayRect().centerX();
                    realPoints[1] = currentPastingState.getInitDisplayRect().centerY();
                    Matrix matrix = new Matrix(currentPastingState.getTransformMatrix());
                    matrix.postConcat(getDrawMatrix());
                    matrix.mapPoints(realPoints);

                    // 缩放
                    float scale = calculateScale(realPoints[0], realPoints[1], mLastX, mLastY, x, y);
                    float[] invert = MatrixUtils.mapInvertMatrixScale(getDrawMatrix(), scale, scale);
                    currentPastingState.getTransformMatrix().postScale(invert[0], invert[1], points[0], points[1]);

                    // 旋转
                    float rotateDegree = calculateAngle(realPoints[0], realPoints[1], mLastX, mLastY, x, y);
                    currentPastingState.getTransformMatrix().postRotate(rotateDegree, points[0], points[1]);

                    mLastX = x;
                    mLastY = y;
                } else { // 做平移操作
                    // calc
                    float[] invert = MatrixUtils.mapInvertMatrixTranslate(getDrawMatrix(), dx, dy);
                    currentPastingState.getTransformMatrix().postTranslate(invert[0], invert[1]);
                    RectF displayRect = getStateDisplayRect(currentPastingState, true);
                    checkDisplayRegion(displayRect);
                    // setStates
                    if (mCallback != null) {
                        mCallback.setOrNotDragCallback(!dragToDeleteViewRect.contains(displayRect.centerX(), displayRect.centerY()));
                    }
                }
                redrawAllCache();
            }
        }
    }

    public static float calculateScale(float centerX, float centerY, float x1, float y1, float x2, float y2) {
        float v1X = x1 - centerX;
        float v1Y = y1 - centerY;
        float v2X = x2 - centerX;
        float v2Y = y2 - centerY;

        float lenV1 = (float) Math.sqrt(v1X * v1X + v1Y * v1Y);
        float lenV2 = (float) Math.sqrt(v2X * v2X + v2Y * v2Y);

        return lenV2 / lenV1;
    }

    public float calculateAngle(float centerX, float centerY, float x1, float y1, float x2, float y2) {
        float lastDegree = getAngle(centerX, centerY, x1, y1);
        float newDegree = getAngle(centerX, centerY, x2, y2);
        if (Float.isNaN(lastDegree) || Float.isNaN(newDegree)) {
            return 0;
        }
        return newDegree - lastDegree;
    }

    private float getAngle(float centerX, float centerY, float touchX, float touchY) {
        float angle = 0;
        float x2 = touchX - centerX;
        float y2 = touchY - centerY;
        float d1 = (float) Math.sqrt((centerY * centerY));
        float d2 = (float) Math.sqrt((x2 * x2 + y2 * y2));
        double toDegrees = Math.toDegrees(Math.acos((-centerY * y2) / (d1 * d2)));
        if (touchX >= centerX) {
            angle = (float) toDegrees;
        } else
            angle = (float) (360 - toDegrees);
        return angle;
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
                // public boolean postRotate (float degrees)
                //Postconcats the matrix with the specified rotation. M' = R(degrees) * M
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
            // 获取相对于手机屏幕左上角的位置(绝对位置)
            RectF displayRect = getStateDisplayRect(currentPastingState, true);
            boolean delete = dragToDeleteViewRect.contains(displayRect.centerX(), displayRect.centerY());
            // remove
            if (delete) {
                saveStateMap.remove(currentPastingState.getId());
                currentPastingState = null;
                redrawAllCache();
            } else {
                // rebound
                if (!validateRect.contains(displayRect.centerX(), displayRect.centerY())) {
                    float dx = displayRect.centerX() - getResources().getDisplayMetrics().widthPixels / 2;
                    float dy = displayRect.centerY() - getResources().getDisplayMetrics().heightPixels / 2;
                    // 如果超出编辑框, 把历史记录挪回当前图片编辑框的中心
                    rebound(dx, dy);
                }
//                currentPastingState.getInitDisplayMatrix().reset();
            }
        }
        // hide extra validate rect (over mValidateRect should be masked)
        // 手指抬起时，过1.5s后，不高亮当前操作的pasting
//        hideExtraValidateRect();
    }

    // 把在validateRect外面的pasting隐藏
    protected void hideExtraValidateRect() {
        postDelayed(hidePastingOutOfBoundsRunnable, 1500);
    }

    // 将在validateRect边界外的控件translate回来
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

    // 把在validateRect外面的pasting隐藏
    private void recover2ValidateRect() {
        currentPastingState = null;
        pastingOutOfBound = false;
        redrawAllCache();
    }

    @Override
    protected void drawAllCachedState(Canvas canvas) {
        /**
         * 这里重画的所有历史编辑记录的x,y坐标, 都是在通过getDrawMatrix()的逆矩阵计算, 得到原图在没有做过任何缩放,平移,裁剪操作情况下的x',y'坐标
         * 在{@link BaseLayerView#onDraw}方法调用时,
         * 1.先把所有的path, 通过{@link BaseLayerView#displayCanvas}, 画在没有经过任何变换操作的{@link BaseLayerView#displayBitmap}上
         * 2.再把{@link BaseLayerView#displayBitmap}, 通过canvas.drawBitmap(displayBitmap, getDrawMatrix(), null), 在经过getDrawMatrix()的变换后, 画到canvas上
         */
        for (int i = 0; i < saveStateMap.size(); i++) {
            T state = saveStateMap.valueAt(i);
            drawPastingState(state, canvas);
        }
    }

    public void setDragToDeleteViewRect(RectF dragToDeleteViewRect) {
        this.dragToDeleteViewRect = dragToDeleteViewRect;
    }

    @Override
    public void redrawOnPhotoRectUpdate() {
        redrawAllCache();
    }

    protected abstract void onPastingDoubleClick(T state);

    protected abstract void drawPastingState(T state, Canvas canvas);
}
