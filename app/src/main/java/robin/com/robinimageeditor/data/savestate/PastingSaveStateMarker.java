package robin.com.robinimageeditor.data.savestate;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by Robin Yang on 1/4/18.
 */

public abstract class PastingSaveStateMarker extends SaveStateMarker {

    protected RectF initDisplayRect;
    protected Matrix transformMatrix;   // 缩放平移旋转改变此值
    protected Matrix initDisplayMatrix;

    public PastingSaveStateMarker(RectF initDisplayRect, Matrix initDisplayMatrix, Matrix transformMatrix) {
        this.initDisplayRect = initDisplayRect;
        this.transformMatrix = transformMatrix;
        if (this.transformMatrix == null) {
            this.transformMatrix = new Matrix();
        }
        this.initDisplayMatrix = initDisplayMatrix;
        if (this.initDisplayMatrix == null) {
            this.initDisplayMatrix = new Matrix();
        }
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
