package robin.com.robinimageeditor.data.share;

import robin.com.robinimageeditor.layer.sticker.StickerType;

/**
 * Created by Robin Yang on 1/4/18.
 */

public class InputStickerSharableData implements SharableData {

    private StickerType stickerType;
    private int stickerIndex;

    public InputStickerSharableData(StickerType stickerType, int stickerIndex) {
        this.stickerType = stickerType;
        this.stickerIndex = stickerIndex;
    }

    public StickerType getStickerType() {
        return stickerType;
    }

    public void setStickerType(StickerType stickerType) {
        this.stickerType = stickerType;
    }

    public int getStickerIndex() {
        return stickerIndex;
    }

    public void setStickerIndex(int stickerIndex) {
        this.stickerIndex = stickerIndex;
    }
}
