package robin.com.robinimageeditor.bean;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Robin Yang on 12/29/17.
 */

public class LayerEditResult implements SharableData{

    Matrix supportMatrix;

    Bitmap bitmap;

    public LayerEditResult(Matrix supportMatrix, Bitmap bitmap) {
        this.supportMatrix = supportMatrix;
        this.bitmap = bitmap;
    }

    public Matrix getSupportMatrix() {
        return supportMatrix;
    }

    public void setSupportMatrix(Matrix supportMatrix) {
        this.supportMatrix = supportMatrix;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
