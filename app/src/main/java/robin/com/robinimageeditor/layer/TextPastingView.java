package robin.com.robinimageeditor.layer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import robin.com.robinimageeditor.bean.InputTextData;
import robin.com.robinimageeditor.bean.TextPastingSaveState;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.util.Utils;

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
        mFocusRectOffset = Utils.dp2px(context, 10f);
        // textPaint
        mTextPaint = new Paint();
        mTextPaint.setTextSize(Utils.sp2px(context, 25f));
        mTextPaint.setAntiAlias(true);
        mTempTextPaint = Utils.copyPaint(mTextPaint);
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
                displayMatrix = result.getDisplayMatrix();
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

    private TextPastingSaveState initTextPastingSaveState(String text, int color, Matrix matrix) {
        if (matrix == null) {
            matrix = new Matrix();
        }
        mTextPaint.setColor(color);
        float width = mTextPaint.measureText(text);
        float height = mTextPaint.descent() - mTextPaint.ascent();
        RectF initDisplayRect = new RectF();
        PointF point = new PointF(validateRect.centerX(), validateRect.centerY());
        point = Utils.mapInvertMatrixPoint(getDrawMatrix(), point);
        Utils.RectFSchedule(initDisplayRect, point.x, point.y, width, height);
        RectF initTextRect = new RectF();
        initTextRect.set(initDisplayRect);
        Utils.RectFIncrease(initDisplayRect, mFocusRectOffset, mFocusRectOffset);
        return new TextPastingSaveState(text, color, initTextRect, initDisplayRect, matrix);
    }

    @Override
    protected void drawPastingState(TextPastingSaveState state, Canvas canvas) {
        RectF resultTextRect = new RectF();
        Matrix matrix = new Matrix(state.getDisplayMatrix());
        matrix.mapRect(resultTextRect, state.getInitTextRect());
        mTempTextPaint.setTextSize(mTextPaint.getTextSize() * Utils.getMatrixScale(matrix));
        mTempTextPaint.setColor(state.getTextColor());
        PointF result = new PointF(resultTextRect.left, resultTextRect.bottom - mTempTextPaint.descent());
        canvas.drawText(state.getText(), result.x, result.y, mTempTextPaint);
    }

    @Override
    protected void onPastingDoubleClick(TextPastingSaveState state) {
        if (mCallback != null) {
            mCallback.onLayerViewDoubleClick(this, new InputTextData(state.getId(), state.getText(), state.getTextColor()));
        }
    }
}
