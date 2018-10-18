package robin.com.robinimageeditor.layer.base;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

import robin.com.robinimageeditor.layer.base.detector.GestureDetectorListener;

/**
 * Root node marker
 * Created by Robin Yang on 12/29/17.
 */

public interface RootNode<RootView> {

    void addOnMatrixChangeListener(OnPhotoRectUpdateListener listener);

    void addGestureDetectorListener(GestureDetectorListener listener);

    void setRotationBy(float degree);

    void setRotationTo(float degree);

    void resetMinScale(float minScale);

    void resetMaxScale(float maxScale);

    void setScale(float scale, boolean animate);

    void setSupportMatrix(Matrix matrix);

    void setDisplayBitmap(Bitmap bitmap);

    Matrix getSupportMatrix();

    Bitmap getDisplayBitmap();

    RootView getRooView();

    RectF getDisplayingRect();

    Matrix getDisplayMatrix();

    Matrix getBaseLayoutMatrix();

    RectF getOriginalRect();
}
