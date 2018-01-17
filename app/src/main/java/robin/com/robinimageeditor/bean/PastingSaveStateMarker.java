package robin.com.robinimageeditor.bean;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by Robin Yang on 1/4/18.
 */

public abstract class PastingSaveStateMarker extends SaveStateMarker {

    protected RectF initDisplayRect;
    protected Matrix transformMatrix;
    protected Matrix initDisplayMatrix;

    public PastingSaveStateMarker(RectF initDisplayRect, Matrix initDisplayMatrix, Matrix transformMatrix) {
        this.initDisplayRect = initDisplayRect;
        this.transformMatrix = transformMatrix;
        if (this.transformMatrix == null) {
            this.transformMatrix = new Matrix();
        }
        this.initDisplayMatrix = initDisplayMatrix;
    }

    public RectF getInitDisplayRect() {
        return initDisplayRect;
    }

    public Matrix getTransformMatrix() {
        return transformMatrix;
    }

    public Matrix getInitDisplayMatrix() {
        return initDisplayMatrix;
    }
}
