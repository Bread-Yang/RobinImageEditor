package robin.com.robinimageeditor.layer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import robin.com.robinimageeditor.bean.InputStickerData;
import robin.com.robinimageeditor.bean.StickerSaveState;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.util.StickerUtils;
import robin.com.robinimageeditor.util.Utils;
import robin.com.robinimageeditor.view.StickerType;

/**
 * Created by Robin Yang on 1/4/18.
 */

public class StickerView extends BasePastingLayerView<StickerSaveState> {

    private float mFocusRectOffset;

    public StickerView(Context context) {
        super(context);
    }

    public StickerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public StickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StickerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initSupportView(Context context) {
        super.initSupportView(context);
        mFocusRectOffset = Utils.dp2px(context, 10f);
    }

    public void onStickerPastingChanged(InputStickerData data) {
        addStickerPasting(data.getStickerIndex(), data.getStickerType());
    }

    private void addStickerPasting(int stickerIndex, StickerType stickerType) {
        genDisplayCanvas();
        StickerSaveState state = initStickerSaveState(stickerIndex, stickerType, null);
        if (state == null) {
            return;
        }
        saveStateMap.put(state.getId(), state);
        currentPastingState = state;
        redrawAllCache();
        hideExtraValidateRect();
    }

    private StickerSaveState initStickerSaveState(int stickerIndex, StickerType stickerType, Matrix matrix) {
        if (matrix == null) {
            matrix = new Matrix();
        }

        Bitmap bitmap = StickerUtils.getInstance().getStickerBitmap(getContext(), stickerType, stickerIndex);
        if (bitmap == null) {
            return null;
        }
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        RectF initDisplayRect = new RectF();
        PointF point = new PointF(validateRect.centerX(), validateRect.centerY());
        point = Utils.mapInvertMatrixPoint(getDrawMatrix(), point);  // 图片未Matrix变换前的中点
        Utils.RectFSchedule(initDisplayRect, point.x, point.y, width, height);
        RectF initTextRect = new RectF();
        initTextRect.set(initDisplayRect);
        Utils.RectFIncrease(initDisplayRect, mFocusRectOffset, mFocusRectOffset);
        return new StickerSaveState(stickerType, stickerIndex, initDisplayRect, matrix);
    }

    @Override
    protected void onPastingDoubleClick(StickerSaveState state) {

    }

    @Override
    protected void drawPastingState(StickerSaveState state, Canvas canvas) {
        Bitmap result = StickerUtils.getInstance().getStickerBitmap(getContext(), state.getStickerType(), state.getStickerIndex());
        if (result == null) {
            return;
        }
        RectF resultStickerRect = new RectF();
        Matrix matrix = new Matrix(state.getDisplayMatrix());
        matrix.mapRect(resultStickerRect, state.getInitDisplayRect());
        canvas.drawBitmap(result, null, resultStickerRect, null);
//        canvas.drawBitmap(result, matrix, null);
    }
}
