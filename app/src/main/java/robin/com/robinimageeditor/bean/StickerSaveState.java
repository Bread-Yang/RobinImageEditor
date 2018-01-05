package robin.com.robinimageeditor.bean;

import android.graphics.Matrix;
import android.graphics.RectF;

import robin.com.robinimageeditor.view.Sticker;

/**
 * Created by Robin Yang on 1/4/18.
 */

public class StickerSaveState extends PastingSaveStateMarker {

    private Sticker sticker;
    private int stickerIndex;
    private RectF initDisplay;
    private Matrix display;

    public StickerSaveState(Sticker sticker, int stickerIndex, RectF initDisplayRect, Matrix displayMatrix) {
        super(initDisplayRect, displayMatrix);
        this.sticker = sticker;
        this.stickerIndex = stickerIndex;
        this.initDisplay = initDisplayRect;
        this.display = displayMatrix;
    }

    public Sticker getSticker() {
        return sticker;
    }

    public void setSticker(Sticker sticker) {
        this.sticker = sticker;
    }

    public int getStickerIndex() {
        return stickerIndex;
    }

    public void setStickerIndex(int stickerIndex) {
        this.stickerIndex = stickerIndex;
    }

    public RectF getInitDisplay() {
        return initDisplay;
    }

    public void setInitDisplay(RectF initDisplay) {
        this.initDisplay = initDisplay;
    }

    public Matrix getDisplay() {
        return display;
    }

    public void setDisplay(Matrix display) {
        this.display = display;
    }
}
