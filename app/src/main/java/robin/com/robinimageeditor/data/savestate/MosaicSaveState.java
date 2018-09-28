package robin.com.robinimageeditor.data.savestate;

import android.graphics.Path;

import robin.com.robinimageeditor.view.MosaicMode;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicSaveState extends SaveStateMarker {

    private MosaicMode mode;
    private Path path;

    public MosaicSaveState(MosaicMode mode, Path path) {
        this.mode = mode;
        this.path = path;
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
}
