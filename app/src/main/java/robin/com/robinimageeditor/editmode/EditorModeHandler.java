package robin.com.robinimageeditor.editmode;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface EditorModeHandler {

    void handleScrawlMode(boolean selected);

    void handleStickerMode(boolean selected);

    void handleMosaicMode(boolean selected);

    void handleTextPastingMode(boolean selected);

    void handleCropMode(boolean selected);
}
