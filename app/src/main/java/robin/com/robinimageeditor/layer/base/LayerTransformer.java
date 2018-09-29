package robin.com.robinimageeditor.layer;

import android.graphics.Matrix;

import robin.com.robinimageeditor.layer.detector.GestureDetectorListener;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface LayerTransformer extends GestureDetectorListener {

    void resetEditorSupportMatrix(Matrix matrix);
}
