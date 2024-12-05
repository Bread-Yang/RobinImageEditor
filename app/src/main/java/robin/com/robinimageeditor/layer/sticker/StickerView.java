package robin.com.robinimageeditor.layer.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import robin.com.robinimageeditor.data.share.InputStickerSharableData;
import robin.com.robinimageeditor.data.savestate.StickerSaveState;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.utils.MatrixUtils;
import robin.com.robinimageeditor.utils.StickerUtils;

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
        mFocusRectOffset = MatrixUtils.dp2px(context, 10f);
    }

    public void onStickerPastingChanged(InputStickerSharableData data) {
        addStickerPasting(data.getStickerIndex(), data.getStickerType());
    }

    private void addStickerPasting(int stickerIndex, StickerType stickerType) {
        genDisplayCanvas();
        StickerSaveState state = initStickerSaveState(stickerIndex, stickerType);
        if (state == null) {
            return;
        }
        saveStateMap.put(state.getId(), state);
        currentPastingState = state;
        redrawAllCache();
        hideExtraValidateRect();
    }

    private StickerSaveState initStickerSaveState(int stickerIndex, StickerType stickerType) {
        Matrix initMatrix = new Matrix();

        Bitmap bitmap = StickerUtils.getInstance().getStickerBitmap(getContext(), stickerType, stickerIndex);
        if (bitmap == null) {
            return null;
        }
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        RectF initDisplayRect = new RectF();
        PointF point = new PointF(validateRect.centerX(), validateRect.centerY());
        point = MatrixUtils.mapInvertMatrixPoint(getDrawMatrix(), point);  // 图片在做Matrix变换前的中点
        MatrixUtils.RectFSchedule(initDisplayRect, point.x, point.y, width, height);
        MatrixUtils.RectFIncrease(initDisplayRect, mFocusRectOffset, mFocusRectOffset);

        float translateX = initDisplayRect.left;
        float translateY = initDisplayRect.top;
        float scaleX = initDisplayRect.width() / width;
        float scaleY = initDisplayRect.height() / height;

        initMatrix.postScale(scaleX, scaleY);
        initMatrix.postTranslate(translateX, translateY);

        return new StickerSaveState(stickerType, stickerIndex, initDisplayRect, initMatrix, null);
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
//        RectF resultStickerRect = new RectF();
//        Matrix matrix = new Matrix(state.getTransformMatrix());
//        matrix.mapRect(resultStickerRect, state.getInitDisplayRect());
//        canvas.drawBitmap(result, null, resultStickerRect, null);


        Matrix matrix = new Matrix(state.getInitDisplayMatrix());
        matrix.postConcat(state.getTransformMatrix());
        canvas.drawBitmap(result, matrix, null);
    }
}
