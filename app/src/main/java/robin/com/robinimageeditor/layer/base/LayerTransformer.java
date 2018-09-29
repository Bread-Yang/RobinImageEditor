package robin.com.robinimageeditor.layer.base;

import android.graphics.Matrix;

import robin.com.robinimageeditor.layer.base.detector.GestureDetectorListener;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface LayerTransformer extends GestureDetectorListener {

    void resetEditorSupportMatrix(Matrix matrix);
}
