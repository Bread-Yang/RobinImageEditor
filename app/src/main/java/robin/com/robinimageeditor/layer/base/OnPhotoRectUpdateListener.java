package robin.com.robinimageeditor.layer.base;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface OnPhotoRectUpdateListener {

    void onPhotoRectUpdate(RectF rect, Matrix matrix);
}
