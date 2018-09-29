package robin.com.robinimageeditor.layer;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import robin.com.robinimageeditor.layer.base.LayerTransformer;
import robin.com.robinimageeditor.layer.base.OnPhotoRectUpdateListener;
import robin.com.robinimageeditor.layer.base.detector.GestureDetectorListener;

/**
 * Called by {@link rootNode}{@link robin.com.robinimageeditor.layer.photoview.PhotoView}, when {@link rootNode} operate,
 * change the {@link delegateParent}'s child {@link robin.com.robinimageeditor.layer.base.BaseLayerView}'s rect and matrix
 * {@link RootEditorDelegate#onPhotoRectUpdate} accordingly.
 *
 * Created by Robin Yang on 1/2/18.
 */

public class RootEditorDelegate implements RootNode<ImageView>, LayerTransformer, OnPhotoRectUpdateListener {

    private RootNode<ImageView> rootNode;
    private ViewGroup delegateParent;

    public RootEditorDelegate(RootNode<ImageView> rootNode, ViewGroup delegateParent) {
        this.rootNode = rootNode;
        this.delegateParent = delegateParent;

        addGestureDetectorListener(this);
        addOnMatrixChangeListener(this);
    }

    /**
     * Called by {@link robin.com.robinimageeditor.layer.photoview.PhotoViewAttacher#setImageViewMatrix}
     * @param rect
     * @param matrix
     */
    @Override
    public void onPhotoRectUpdate(RectF rect, Matrix matrix) {
        callChildrenRectUpdate(delegateParent, rect, matrix);
    }

    @Override
    public void onDrag(float dx, float dy, float x, float y, boolean rootLayer) {
        callChildrenOnDrag(delegateParent, dx, dy, x, y, true);
    }

    @Override
    public void onScale(float scaleFactor, float focusX, float focusY, boolean rootLayer) {
        callChildrenOnScale(delegateParent, scaleFactor, focusX, focusY, true);
    }

    @Override
    public void resetEditorSupportMatrix(Matrix matrix) {
        callChildrenResetSupportMatrix(delegateParent, matrix);
    }

    @Override
    public void setRotationBy(float degree) {
        rootNode.setRotationBy(degree);
    }

    @Override
    public void resetMinScale(float minScale) {
        rootNode.resetMinScale(minScale);
    }

    @Override
    public void resetMaxScale(float maxScale) {
        rootNode.resetMaxScale(maxScale);
    }

    @Override
    public void setScale(float scale, boolean animate) {
        rootNode.setScale(scale, animate);
    }

    @Override
    public void addOnMatrixChangeListener(OnPhotoRectUpdateListener listener) {
        rootNode.addOnMatrixChangeListener(listener);
    }

    @Override
    public void addGestureDetectorListener(GestureDetectorListener listener) {
        rootNode.addGestureDetectorListener(listener);
    }

    @Override
    public Matrix getSupportMatrix() {
        return rootNode.getSupportMatrix();
    }

    @Override
    public Bitmap getDisplayBitmap() {
        return rootNode.getDisplayBitmap();
    }

    @Override
    public void setSupportMatrix(Matrix matrix) {
        rootNode.setSupportMatrix(matrix);
    }

    @Override
    public void setDisplayBitmap(Bitmap bitmap) {
        rootNode.setDisplayBitmap(bitmap);
    }

    @Override
    public ImageView getRooView() {
        return rootNode.getRooView();
    }

    @Override
    public RectF getDisplayingRect() {
        return rootNode.getDisplayingRect();
    }

    @Override
    public Matrix getDisplayMatrix() {
        return rootNode.getDisplayMatrix();
    }

    @Override
    public Matrix getBaseLayoutMatrix() {
        return rootNode.getBaseLayoutMatrix();
    }

    @Override
    public RectF getOriginalRect() {
        return rootNode.getOriginalRect();
    }

    @Override
    public void onFingerDown(float downX, float downY) {

    }

    @Override
    public void onFingerUp(float upX, float upY) {

    }

    @Override
    public void onFingerCancel() {

    }

    @Override
    public void onFling(float startX, float startY, float velocityX, float velocityY, boolean rootLayer) {

    }

    @Override
    public void cancelFling(boolean rootLayer) {

    }

    @Override
    public void onRotate(float rotateDegree, float focusX, float focusY, boolean rootLayer) {

    }

    private void callChildrenRectUpdate(ViewGroup parent, RectF rect, Matrix matrix) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View layer = parent.getChildAt(i);

            if (layer instanceof OnPhotoRectUpdateListener) {
                ((OnPhotoRectUpdateListener)layer).onPhotoRectUpdate(rect, matrix);
            } else if (layer instanceof  ViewGroup) {
                callChildrenRectUpdate((ViewGroup) layer, rect, matrix);
            }
        }
    }

    private void callChildrenOnDrag(ViewGroup parent, float dx, float dy, float x, float y, boolean rootLayer) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View layer = parent.getChildAt(i);

            if (layer instanceof LayerTransformer) {
                ((LayerTransformer)layer).onDrag(dx, dy, x, y, true);
            } else if (layer instanceof  ViewGroup) {
                callChildrenOnDrag((ViewGroup) layer, dx, dy, x, y, true);
            }
        }
    }

    private void callChildrenOnScale(ViewGroup parent, float scaleFactor, float focusX, float focusY, boolean rootLayer) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View layer = parent.getChildAt(i);

            if (layer instanceof LayerTransformer) {
                ((LayerTransformer)layer).onScale(scaleFactor, focusX, focusY, true);
            } else if (layer instanceof  ViewGroup) {
                callChildrenOnScale((ViewGroup) layer, scaleFactor, focusX, focusY, true);
            }
        }
    }

    private void callChildrenResetSupportMatrix(ViewGroup parent, Matrix matrix) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View layer = parent.getChildAt(i);

            if (layer instanceof LayerTransformer) {
                ((LayerTransformer)layer).resetEditorSupportMatrix(matrix);
            } else if (layer instanceof  ViewGroup) {
                callChildrenResetSupportMatrix((ViewGroup) layer, matrix);
            }
        }
    }
}
