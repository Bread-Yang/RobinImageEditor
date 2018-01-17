package robin.com.robinimageeditor.bean;

import android.graphics.Matrix;
import android.graphics.RectF;

import robin.com.robinimageeditor.view.StickerType;

/**
 * Created by Robin Yang on 1/4/18.
 */

public class StickerSaveState extends PastingSaveStateMarker {

    private StickerType stickerType;
    private int stickerIndex;

    public StickerSaveState(StickerType stickerType, int stickerIndex, RectF initDisplayRect,
                            Matrix initDisplayMatrix, Matrix transformMatrix) {
        super(initDisplayRect, initDisplayMatrix, transformMatrix);
        this.stickerType = stickerType;
        this.stickerIndex = stickerIndex;
    }

    @Override
    public SaveStateMarker deepCopy() {
        SaveStateMarker state = new StickerSaveState(stickerType, stickerIndex,
                new RectF(getInitDisplayRect()), new Matrix(getInitDisplayMatrix()), new Matrix(getTransformMatrix()));
        state.setId(this.getId());
        return state;
    }

    public StickerType getStickerType() {
        return stickerType;
    }

    public int getStickerIndex() {
        return stickerIndex;
    }
}
