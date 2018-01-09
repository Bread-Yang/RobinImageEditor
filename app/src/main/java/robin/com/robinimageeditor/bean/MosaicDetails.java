package robin.com.robinimageeditor.bean;

import robin.com.robinimageeditor.view.MosaicMode;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicDetails implements FuncDetailsMarker {

    MosaicMode mosaicMode;

    public MosaicDetails(MosaicMode mosaicMode) {
        this.mosaicMode = mosaicMode;
    }

    public MosaicMode getMosaicMode() {
        return mosaicMode;
    }
}
