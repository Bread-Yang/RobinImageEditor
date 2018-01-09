package robin.com.robinimageeditor.view;

import robin.com.robinimageeditor.R;

/**
 * Created by Robin Yang on 1/8/18.
 */

public enum MosaicMode {

    Grid {
        @Override
        int getModeBgResource() {
            return R.drawable.selector_edit_image_traditional_mosaic;
        }
    },

    Blur {
        @Override
        int getModeBgResource() {
            return R.drawable.selector_edit_image_brush_mosaic;
        }
    };

    abstract int getModeBgResource();
}
