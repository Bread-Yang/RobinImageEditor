package robin.com.robinimageeditor.bean;

import android.graphics.Paint;
import android.graphics.Path;

import robin.com.robinimageeditor.util.Utils;

/**
 * Created by Robin Yang on 12/29/17.
 */

public class ScrawlSaveState extends SaveStateMarker {

    private Paint paint;
    private Path path;

    public ScrawlSaveState(Paint paint, Path path) {
        this.paint = paint;
        this.path = path;
    }

    @Override
    public SaveStateMarker deepCopy() {
        SaveStateMarker state = new ScrawlSaveState(Utils.copyPaint(paint), path);
        state.setId(this.getId());
        return state;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
