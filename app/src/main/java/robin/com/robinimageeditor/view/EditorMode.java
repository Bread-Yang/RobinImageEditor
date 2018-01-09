package robin.com.robinimageeditor.view;

import robin.com.robinimageeditor.R;

/**
 * Created by Robin Yang on 12/28/17.
 */

public enum EditorMode {

    ScrawlMode {

        @Override
        int getModeBgResource() {
            return R.drawable.selector_edit_image_pen_tool;
        }

        @Override
        void onHandle(boolean selected, EditorModeHandler handler) {
            handler.handleScrawlMode(selected);
        }

        @Override
        boolean canPersistMode() {
            return true;
        }
    },

    StickerMode {
        @Override
        int getModeBgResource() {
            return R.drawable.selector_edit_image_emotion_tool;
        }

        @Override
        void onHandle(boolean selected, EditorModeHandler handler) {
            handler.handleStickerMode(selected);
        }

        @Override
        boolean canPersistMode() {
            return false;
        }
    },

    MosaicMode {
        @Override
        int getModeBgResource() {
            return R.drawable.selector_edit_image_mosaic_tool;
        }

        @Override
        void onHandle(boolean selected, EditorModeHandler handler) {
            handler.handleMosaicMode(selected);
        }

        @Override
        boolean canPersistMode() {
            return true;
        }
    },

    TextPastingMode {
        @Override
        int getModeBgResource() {
            return R.drawable.selector_edit_image_text_tool;
        }

        @Override
        void onHandle(boolean selected, EditorModeHandler handler) {
            handler.handleTextPastingMode(selected);
        }

        @Override
        boolean canPersistMode() {
            return false;
        }
    }
    ;

    abstract boolean canPersistMode();

    abstract int getModeBgResource();

    abstract void onHandle(boolean selected, EditorModeHandler handler);
}
