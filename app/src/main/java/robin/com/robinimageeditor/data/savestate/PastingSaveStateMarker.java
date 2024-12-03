package robin.com.robinimageeditor.data.savestate;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by Robin Yang on 1/4/18.
 */

public abstract class PastingSaveStateMarker extends SaveStateMarker {

    protected RectF initDisplayRect;    // 白色的高亮选中框的Rect, 坐标已经是通过逆矩阵计算出来的, 所以直接在canvas上画就行
    protected Matrix transformMatrix;   // 缩放平移旋转改变此值, 一开始都是单位矩阵
    protected Matrix initDisplayMatrix; // 一开始都是单位矩阵

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
