package robin.com.robinimageeditor.data.savestate;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;

import robin.com.robinimageeditor.layer.base.BaseLayerView;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * Created by Robin Yang on 1/4/18.
 */

public abstract class PastingSaveStateMarker extends SaveStateMarker {

    /**
     * 白色的高亮选中框的Rect
     * 坐标已经是通过逆矩阵计算出来的, 相对于图片编辑框左上角的位置(不是相对于屏幕左上角), 所以直接在canvas上画就行{@link BaseLayerView#displayCanvas}
     */
    protected RectF initDisplayRect;
    protected Matrix transformMatrix;   // 缩放平移旋转改变此值, 一开始都是单位矩阵
    protected Matrix initDisplayMatrix; // 一开始都是单位矩阵, 除非设置了
    protected boolean adjustPointTouch; // 调整按钮是否触碰

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

    public boolean isAdjustPointTouch() {
        return adjustPointTouch;
    }

    public void setAdjustPointTouch(boolean adjustPointTouch) {
        this.adjustPointTouch = adjustPointTouch;
    }

    public RectF getAdjustIconRectF() {
        int padding = BasePastingLayerView.icon_radius;
        return new RectF((initDisplayRect.right - padding), (initDisplayRect.bottom - padding), (initDisplayRect.right + padding), (initDisplayRect.bottom + padding));
    }
}
