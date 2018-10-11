package robin.com.robinimageeditor.funcdetail;

import robin.com.robinimageeditor.layer.mosaic.MosaicMode;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicDetails implements FuncDetailsMarker {

    private MosaicMode mosaicMode;

    private float paintStrokekWidth;

    public MosaicDetails(MosaicMode mosaicMode) {
        this.mosaicMode = mosaicMode;
    }

    public MosaicDetails(MosaicMode mosaicMode, float paintStrokekWidth) {
        this.mosaicMode = mosaicMode;
        this.paintStrokekWidth = paintStrokekWidth;
    }

    public MosaicMode getMosaicMode() {
        return mosaicMode;
    }

    public float getPaintStrokekWidth() {
        return paintStrokekWidth;
    }
}
