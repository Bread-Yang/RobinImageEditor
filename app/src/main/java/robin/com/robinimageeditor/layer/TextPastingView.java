package robin.com.robinimageeditor.layer;

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

import robin.com.robinimageeditor.bean.InputTextData;
import robin.com.robinimageeditor.data.savestate.TextPastingSaveState;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.util.MatrixUtils;

/**
 * Created by Robin Yang on 1/9/18.
 */

public class TextPastingView extends BasePastingLayerView<TextPastingSaveState> {

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

    public void onTextPastingChanged(InputTextData data) {
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

    private TextPastingSaveState initTextPastingSaveState(String text, int color, Matrix initMatrix) {
        if (initMatrix == null) {
            initMatrix = new Matrix();
        }
        mTextPaint.setColor(color);
        float width = mTextPaint.measureText(text);
        float height = mTextPaint.descent() - mTextPaint.ascent();
        RectF initDisplayRect = new RectF();
        PointF point = new PointF(validateRect.centerX(), validateRect.centerY());
        point = MatrixUtils.mapInvertMatrixPoint(getDrawMatrix(), point);
        MatrixUtils.RectFSchedule(initDisplayRect, point.x, point.y, width, height);
        RectF initTextRect = new RectF();
        initTextRect.set(initDisplayRect);
        MatrixUtils.RectFIncrease(initDisplayRect, mFocusRectOffset, mFocusRectOffset);
        return new TextPastingSaveState(text, color, initTextRect, initDisplayRect, initMatrix, null);
    }

    @Override
    protected void drawPastingState(TextPastingSaveState state, Canvas canvas) {
        RectF resultTextRect = new RectF();
        Matrix matrix = new Matrix(state.getTransformMatrix());
        matrix.mapRect(resultTextRect, state.getInitTextRect());
        mTempTextPaint.setTextSize(mTextPaint.getTextSize() * MatrixUtils.getMatrixScale(matrix));
        mTempTextPaint.setColor(state.getTextColor());
        PointF result = new PointF(resultTextRect.left, resultTextRect.bottom - mTempTextPaint.descent());
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
            mCallback.onLayerViewDoubleClick(this, new InputTextData(state.getId(), state.getText(), state.getTextColor()));
        }
    }
}
