package robin.com.robinimageeditor.layer.textpasting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import robin.com.robinimageeditor.data.share.InputTextSharableData;
import robin.com.robinimageeditor.data.savestate.TextPastingSaveState;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * Created by Robin Yang on 1/9/18.
 */

public class TextPastingView extends BasePastingLayerView<TextPastingSaveState> {

    // 高亮的白色选中框相对于文本Text的offset
    private float mFocusRectOffset;
    private Paint mTextPaint;
    private Paint mTempTextPaint;

    public TextPastingView(Context context) {
        super(context);
    }

    public TextPastingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextPastingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextPastingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initSupportView(Context context) {
        super.initSupportView(context);
        mFocusRectOffset = MatrixUtils.dp2px(context, 10f);
        // textPaint
        mTextPaint = new Paint();
        mTextPaint.setTextSize(MatrixUtils.sp2px(context, 25f));
        mTextPaint.setAntiAlias(true);
        mTempTextPaint = MatrixUtils.copyPaint(mTextPaint);
    }

    public void onTextPastingChanged(InputTextSharableData data) {
        if (data.getText() == null || TextUtils.isEmpty(data.getText()) || data.getColor() == 0) {
            return;
        }
        addTextPasting(data.getId(), data.getText(), data.getColor());
    }

    private void addTextPasting(String id, String text, int color) {
        genDisplayCanvas();
        // old matrix info
        Matrix displayMatrix = new Matrix();
        if (id != null) {
            TextPastingSaveState result = saveStateMap.get(id);
            if (result != null) {
                displayMatrix = result.getTransformMatrix();
            }
        }
        TextPastingSaveState state = initTextPastingSaveState(text, color, displayMatrix);
        if (id != null) {
            state.setId(id);
        }
        saveStateMap.put(state.getId(), state);
        currentPastingState = state;
        redrawAllCache();
        // hideBorder...
        hideExtraValidateRect();
    }

    private TextPastingSaveState initTextPastingSaveState(String text, int color, Matrix transformMatrix) {
        if (transformMatrix == null) {
            transformMatrix = new Matrix();
        }
        mTextPaint.setColor(color);
        float width = mTextPaint.measureText(text);
        float height = mTextPaint.descent() - mTextPaint.ascent();
        RectF initDisplayRect = new RectF();
        // 这段代码会导致新增的PastingView所在的位置, 每次都是在validateRect的中间, 而不是在屏幕的中间
//        PointF point = new PointF(validateRect.centerX(), validateRect.centerY());
        // 无论photoView是缩放还是平移, 新增的PastingView的初始位置, 永远显示在屏幕的中间
        float centerX = getResources().getDisplayMetrics().widthPixels / 2;
        float centerY = getResources().getDisplayMetrics().heightPixels / 2;
        PointF point = new PointF(centerX, centerY);
        // 屏幕中心点, 通过DrawMatrix的逆矩阵, 计算出变换前的实际位置(相对于图片编辑框左上角的位置(不是相对于屏幕左上角),)
        point = MatrixUtils.mapInvertMatrixPoint(getDrawMatrix(), point);
        MatrixUtils.RectFSchedule(initDisplayRect, point.x, point.y, width, height);
        RectF initTextRect = new RectF();
        initTextRect.set(initDisplayRect);
        // 高亮的白色选中框相对于文本Text的offset, 这里left和top各减了mFocusRectOffset, right和bottom各加了mFocusRectOffset
        MatrixUtils.RectFIncrease(initDisplayRect, mFocusRectOffset, mFocusRectOffset);
        return new TextPastingSaveState(text, color, initTextRect, initDisplayRect, new Matrix(), transformMatrix);
    }

    // 把历史的文本编辑记录画出来
    @Override
    protected void drawPastingState(TextPastingSaveState state, Canvas canvas) {
//        RectF resultTextRect = new RectF();
        Matrix transformMatrix = new Matrix(state.getTransformMatrix());
//        transformMatrix.mapRect(resultTextRect, state.getInitTextRect());
        mTempTextPaint.setTextSize(mTextPaint.getTextSize() * MatrixUtils.getMatrixScale(transformMatrix));
        mTempTextPaint.setColor(state.getTextColor());
//        PointF result = new PointF(resultTextRect.left, resultTextRect.bottom - mTempTextPaint.descent());
//        canvas.drawText(state.getText(), result.x, result.y, mTempTextPaint);

        RectF initDisplayRectF = state.getInitDisplayRect();
        float[] polygonPoint = new float[4];

        float width = mTextPaint.measureText(state.getText());
        float height = mTextPaint.descent() - mTextPaint.ascent();

        // left_middle + (text's height / 2)
        polygonPoint[0] = initDisplayRectF.left + (initDisplayRectF.width() - width) / 2;
        polygonPoint[1] = (initDisplayRectF.top + initDisplayRectF.bottom) / 2 + height / 3;

        // left_middle + (text's height / 2)
        polygonPoint[2] = initDisplayRectF.right;
        polygonPoint[3] = (initDisplayRectF.top + initDisplayRectF.bottom) / 2 + height / 3;

        state.getTransformMatrix().mapPoints(polygonPoint);

        Path path = new Path();
        path.moveTo(polygonPoint[0], polygonPoint[1]);
        path.lineTo(polygonPoint[2], polygonPoint[3]);

        canvas.drawTextOnPath(state.getText(), path, 0, 0, mTempTextPaint);
    }

    @Override
    protected void onPastingDoubleClick(TextPastingSaveState state) {
        if (mCallback != null) {
            mCallback.onLayerViewDoubleClick(this, new InputTextSharableData(state.getId(), state.getText(), state.getTextColor()));
        }
    }
}
