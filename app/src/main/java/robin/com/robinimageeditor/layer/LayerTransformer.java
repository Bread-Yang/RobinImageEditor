package robin.com.robinimageeditor.layer;

import android.graphics.Matrix;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface LayerTransformer extends GestureDetectorListener {

    void resetEditorSupportMatrix(Matrix matrix);
}
