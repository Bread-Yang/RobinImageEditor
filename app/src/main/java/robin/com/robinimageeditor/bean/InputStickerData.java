package robin.com.robinimageeditor.bean;

import robin.com.robinimageeditor.view.Sticker;

/**
 * Created by Robin Yang on 1/4/18.
 */

public class InputStickerData implements SharableData {

    private Sticker sticker;
    private int stickerIndex;

    public InputStickerData(Sticker sticker, int stickerIndex) {
        this.sticker = sticker;
        this.stickerIndex = stickerIndex;
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
}
