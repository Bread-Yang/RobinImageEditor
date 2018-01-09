package robin.com.robinimageeditor.bean;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by Robin Yang on 1/4/18.
 */

public abstract class PastingSaveStateMarker extends SaveStateMarker {

    protected RectF initDisplayRect;
    protected Matrix displayMatrix;
    protected Matrix initEventDisplayMatrix;

    public PastingSaveStateMarker(RectF initDisplayRect, Matrix displayMatrix) {
        this.initDisplayRect = initDisplayRect;
        this.displayMatrix = displayMatrix;
        initEventDisplayMatrix = new Matrix();
    }

    public RectF getInitDisplayRect() {
        return initDisplayRect;
    }

    public Matrix getDisplayMatrix() {
        return displayMatrix;
    }

    public Matrix getInitEventDisplayMatrix() {
        return initEventDisplayMatrix;
    }
}
