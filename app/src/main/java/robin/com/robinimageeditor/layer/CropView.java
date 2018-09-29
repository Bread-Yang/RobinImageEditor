package robin.com.robinimageeditor.layer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import robin.com.robinimageeditor.layer.base.OnPhotoRectUpdateListener;
import robin.com.robinimageeditor.layer.base.detector.CustomGestureDetector;
import robin.com.robinimageeditor.layer.base.detector.GestureDetectorListener;
import robin.com.robinimageeditor.util.MatrixUtils;

/**
 * Created by Robin Yang on 1/17/18.
 */

public class CropView extends View implements GestureDetectorListener, OnPhotoRectUpdateListener {

    private int DEFAULT_BG_COLOR = Color.parseColor("#99000000");
    private int DEFAULT_GUIDE_LINE_COLOR = Color.WHITE;
    private int DEFAULT_BORDER_LINE_COLOR = Color.WHITE;
    private float DEFAULT_GUIDE_LINE_WIDTH = 2.0f;
    private float DEFAULT_BORDER_LINE_WIDTH = 2.0f;

    private int mBackgroundColor = DEFAULT_BG_COLOR;
    private int mGuidelineColor = DEFAULT_GUIDE_LINE_COLOR;
    private int mBorderlineColor = DEFAULT_BORDER_LINE_COLOR;
    private float mGuidelineStrokeWidth = DEFAULT_GUIDE_LINE_WIDTH;
    private float mBorderlineWidth = DEFAULT_BORDER_LINE_WIDTH;
    private int mBorderCornerLength = 0;
    private int mBorderCornerOffset = 0;

    private Paint mGuidelinePaint;
    private Paint mBorderlinePaint;
    private Paint mBorderCornerPaint;
    private Paint mPaintTranslucent;

    private RectF mViewRect = new RectF();

    private CustomGestureDetector mScaleDragDetector;
    private CropWindowHelper mCropWindowHelper;

    private Path mBgPath = new Path();

    /* 当前view绘制crop的区域 */
    private RectF mDrawingRect = new RectF();
    /* 限制cropWindow 通过 mValidateBorderRect，max,min with,or height */
    private RectF mValidateBorderRect = new RectF();

    private boolean mCropViewIsUpdated = false;
    private OnCropViewUpdatedListener onCropViewUpdatedListener;
    private float mLastRotateDegree;

    public interface OnCropViewUpdatedListener {
        void onCropViewUpdated();
    }

    public CropView(Context context) {
        super(context);
        initView(context);
    }

    public CropView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        // mDrawingRect = new RectF(200f, 200f, 1000f, 1000f);  //test
        int mCropTouchSlop = MatrixUtils.dp2px(context, 15f);
        // init cropWindowHelper.
        mCropWindowHelper = new CropWindowHelper(mCropTouchSlop);
        mCropWindowHelper.setEdge(mDrawingRect);
        // mValidateBorderRect = RectF(0f, 0f, 1080f, 1920f);  //test
        int minCrop = MatrixUtils.dp2px(context, 60f);
        mCropWindowHelper.minCropWindowWidth = minCrop;
        mCropWindowHelper.minCropWindowHeight = minCrop;
        // touchEventSupport.
        mScaleDragDetector = new CustomGestureDetector(context, this, false);
        // paint
        mBorderlinePaint = getBorderPaint(mBorderlineWidth, mBorderlineColor);
        mGuidelinePaint = getBorderPaint(mGuidelineStrokeWidth, mGuidelineColor);
        mBorderCornerPaint = getBorderPaint(mBorderlineWidth * 3, mBorderlineColor);
        // bgPath
        mPaintTranslucent = getBorderPaint(mGuidelineStrokeWidth, mBackgroundColor);
        mPaintTranslucent.setStyle(Paint.Style.FILL);
        // inner border
        mBorderCornerLength = MatrixUtils.dp2px(context, 20f);
        mBorderCornerOffset = MatrixUtils.dp2px(context, 3f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawingRect.width() <= 0) {
            return;
        }
        // drawBgPath
        drawBgPath(canvas);
        // drawBorder
        canvas.drawRect(mDrawingRect, mBorderlinePaint);
        // Draw 2 vertical and 2 horizontal guidelines
        drawGuideLines(canvas);
        // drawCorners
        drawCorners(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            boolean intercept = mCropWindowHelper.interceptTouchEvent(event);
            if (!intercept) {
                return false;
            }
        } else if (action == MotionEvent.ACTION_UP) {
            mCropWindowHelper.resetTouchEvent(event);
        }
        return mScaleDragDetector.onTouchEvent(event);
    }

    private Paint getBorderPaint(float thickness, int color) {
        Paint borderPaint = new Paint();
        borderPaint.setColor(color);
        borderPaint.setStrokeWidth(thickness);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        return borderPaint;
    }

    private void drawBgPath(Canvas canvas) {
        mBgPath.reset();
        mBgPath.addRect(mViewRect, Path.Direction.CW);
        mBgPath.addRect(mDrawingRect, Path.Direction.CCW);
        canvas.drawPath(mBgPath, mPaintTranslucent);
    }

    private void drawGuideLines(Canvas canvas) {
        RectF rect = mDrawingRect;
        float oneThirdCropWidth = rect.width() / 3;
        float oneThirdCropHeight = rect.height() / 3;
        // Draw vertical guidelines.
        float x1 = rect.left + oneThirdCropWidth;
        float x2 = rect.right - oneThirdCropWidth;
        canvas.drawLine(x1, rect.top, x1, rect.bottom, mGuidelinePaint);
        canvas.drawLine(x2, rect.top, x2, rect.bottom, mGuidelinePaint);
        // Draw horizontal guidelines.
        float y1 = rect.top + oneThirdCropHeight;
        float y2 = rect.bottom - oneThirdCropHeight;
        canvas.drawLine(rect.left, y1, rect.right, y1, mGuidelinePaint);
        canvas.drawLine(rect.left, y2, rect.right, y2, mGuidelinePaint);
    }

    private void drawCorners(Canvas canvas) {
        RectF rect = mDrawingRect;
        int cornerOffset = mBorderCornerOffset;
        // Top left
        canvas.drawLine(rect.left + cornerOffset, rect.top + cornerOffset, rect.left + cornerOffset, rect.top + mBorderCornerLength + cornerOffset, mBorderCornerPaint);
        canvas.drawLine(rect.left + cornerOffset, rect.top + cornerOffset, rect.left + mBorderCornerLength + cornerOffset, rect.top + cornerOffset, mBorderCornerPaint);
        // Top right
        canvas.drawLine(rect.right - cornerOffset, rect.top + cornerOffset, rect.right - cornerOffset, rect.top + mBorderCornerLength + cornerOffset, mBorderCornerPaint);
        canvas.drawLine(rect.right - cornerOffset, rect.top + cornerOffset, rect.right - mBorderCornerLength - cornerOffset, rect.top + cornerOffset, mBorderCornerPaint);
        // Bottom left
        canvas.drawLine(rect.left + cornerOffset, rect.bottom - cornerOffset, rect.left + cornerOffset, rect.bottom - mBorderCornerLength - cornerOffset, mBorderCornerPaint);
        canvas.drawLine(rect.left + cornerOffset, rect.bottom - cornerOffset, rect.left + mBorderCornerLength + cornerOffset, rect.bottom - cornerOffset, mBorderCornerPaint);
        // Bottom left
        canvas.drawLine(rect.right - cornerOffset, rect.bottom - cornerOffset, rect.right - cornerOffset, rect.bottom - mBorderCornerLength - cornerOffset, mBorderCornerPaint);
        canvas.drawLine(rect.right - cornerOffset, rect.bottom - cornerOffset, rect.right - mBorderCornerLength - cornerOffset, rect.bottom - cornerOffset, mBorderCornerPaint);
    }

    @Override
    public void onFingerDown(float downX, float downY) {

    }

    @Override
    public void onFingerUp(float upX, float upY) {

    }

    @Override
    public void onFingerCancel() {

    }

    @Override
    public void onDrag(float dx, float dy, float x, float y, boolean rootLayer) {
        boolean feedBack = mCropWindowHelper.onCropWindowDrag(dx, dy, mValidateBorderRect);
        if (feedBack) {
            mDrawingRect.set(mCropWindowHelper.getEdge());
            invalidate();
            notifyCropViewUpdated();
        }
    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY, boolean rootLayer) {

    }

    @Override
    public void cancelFling(boolean rootLayer) {

    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY, boolean rootLayer) {

    }

    @Override
    public void onRotate(float rotateDegree, float focusX, float focusY, boolean rootLayer) {

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mViewRect.isEmpty()){
            mViewRect.set(left, top, right, bottom);
        }
        if(mValidateBorderRect.isEmpty()){
            mValidateBorderRect.set(mViewRect);
        }
    }

    @Override
    public void onPhotoRectUpdate(RectF rect, Matrix matrix) {
        RectF validateBorder = rect;
        if (validateBorder.left < mViewRect.left) {
            validateBorder.left = mViewRect.left;
        }
        if (validateBorder.top < mViewRect.top) {
            validateBorder.top = mViewRect.top;
        }
        if (validateBorder.right > mViewRect.right) {
            validateBorder.right = mViewRect.right;
        }
        if (validateBorder.bottom > mViewRect.bottom) {
            validateBorder.bottom = mViewRect.bottom;
        }
        rotateCropWindow(MatrixUtils.getMatrixDegree(matrix)); //rotate.
        mValidateBorderRect.set(validateBorder);
        boolean boundsChanged = mCropWindowHelper.checkCropWindowBounds(mValidateBorderRect);
        if (boundsChanged) {
            mDrawingRect.set(mCropWindowHelper.getEdge());
            invalidate();
        }
        notifyCropViewUpdated();
    }

    private void notifyCropViewUpdated() {
        mCropViewIsUpdated = true;
        if (onCropViewUpdatedListener != null) {
            onCropViewUpdatedListener.onCropViewUpdated();
        }
    }

    private void rotateCropWindow(float rotateDegree) {
        float degree = rotateDegree - mLastRotateDegree;
        if (degree == 0f) {
            return;
        }
        mLastRotateDegree = rotateDegree;
        Matrix matrix = new Matrix();
        RectF result = new RectF();
        matrix.postRotate(degree % 360, mDrawingRect.centerX(), mDrawingRect.centerY());
        matrix.postTranslate(mViewRect.centerX() - mDrawingRect.centerX(), mViewRect.centerY() - mDrawingRect.centerY());
        matrix.mapRect(result, mDrawingRect);
        //exchange
        if (degree % 90 == 0f) {
            float oldHeight = mCropWindowHelper.maxCropWindowHeight;
            mCropWindowHelper.maxCropWindowHeight = mCropWindowHelper.maxCropWindowWidth;
            mCropWindowHelper.maxCropWindowWidth = oldHeight;
        }
        updateDrawingRect(result, true);
    }

    private void updateDrawingRect(RectF rect, boolean notifyUpdated) {
        mDrawingRect.set(rect);
        mCropWindowHelper.setEdge(mDrawingRect);
        invalidate();
        if (notifyUpdated)  {
            notifyCropViewUpdated();
        }
    }

    public void setupDrawingRect(RectF rect) {
        mValidateBorderRect.set(rect);
        updateDrawingRect(rect, false);
        mCropViewIsUpdated = false;
    }

    public void updateCropMaxSize(float maxWidth,float  maxHeight) {
        mCropWindowHelper.maxCropWindowHeight = maxHeight;
        mCropWindowHelper.maxCropWindowWidth = maxWidth;
    }

    public void clearDrawingRect() {
        setupDrawingRect(new RectF());
    }

    public RectF getCropRect() {
        return new RectF(mDrawingRect);
    }

    public boolean isCropWindowEdit() {
        return mCropViewIsUpdated;
    }

    public void setOnCropViewUpdatedListener(OnCropViewUpdatedListener onCropViewUpdatedListener) {
        this.onCropViewUpdatedListener = onCropViewUpdatedListener;
    }
}
