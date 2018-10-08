package robin.com.robinimageeditor.bean;

import robin.com.robinimageeditor.view.MosaicMode;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicDetails implements FuncDetailsMarker {

    private MosaicMode mosaicMode;

    private float paintStrokekWidth;

    public MosaicDetails(MosaicMode mosaicMode) {
        this.mosaicMode = mosaicMode;
    }

    public MosaicMode getMosaicMode() {
        return mosaicMode;
    }

    public float getPaintStrokekWidth() {
        return paintStrokekWidth;
    }
}
