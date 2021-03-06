package robin.com.robinimageeditor.data.savestate;

import android.graphics.Path;

import robin.com.robinimageeditor.layer.mosaic.MosaicMode;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicSaveState extends SaveStateMarker {

    private MosaicMode mode;
    private Path path;
    private float paintStrokeWidth;

    public MosaicSaveState(MosaicMode mode, Path path, float paintStrokeWidth) {
        this.mode = mode;
        this.path = path;
        this.paintStrokeWidth = paintStrokeWidth;
    }

    public MosaicMode getMode() {
        return mode;
    }

    public void setMode(MosaicMode mode) {
        this.mode = mode;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public float getPaintStrokeWidth() {
        return paintStrokeWidth;
    }

    public void setPaintStrokekWidth(float paintStrokekWidth) {
        this.paintStrokeWidth = paintStrokekWidth;
    }
}
