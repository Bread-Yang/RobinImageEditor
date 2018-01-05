package robin.com.robinimageeditor.bean;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by Robin Yang on 1/4/18.
 */

public abstract class PastingSaveStateMarker extends SaveStateMarker {

    private RectF initDisplayRect;
    private Matrix displayMatrix;
    private Matrix initEventDisplayMatrix;

    public PastingSaveStateMarker(RectF initDisplayRect, Matrix displayMatrix) {
        this.initDisplayRect = initDisplayRect;
        this.displayMatrix = displayMatrix;
        initEventDisplayMatrix = new Matrix();
    }

    public RectF getInitDisplayRect() {
        return initDisplayRect;
    }

    public void setInitDisplayRect(RectF initDisplayRect) {
        this.initDisplayRect = initDisplayRect;
    }

    public Matrix getDisplayMatrix() {
        return displayMatrix;
    }

    public void setDisplayMatrix(Matrix displayMatrix) {
        this.displayMatrix = displayMatrix;
    }

    public Matrix getInitEventDisplayMatrix() {
        return initEventDisplayMatrix;
    }

    public void setInitEventDisplayMatrix(Matrix initEventDisplayMatrix) {
        this.initEventDisplayMatrix = initEventDisplayMatrix;
    }
}
