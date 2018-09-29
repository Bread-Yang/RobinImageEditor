package robin.com.robinimageeditor.layer.base.detector;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface GestureDetectorListener {

    void onFingerDown(float downX,float downY);

    void onFingerUp(float upX, float upY);

    void onFingerCancel();

    void onDrag(float dx, float dy, float x, float y, boolean rootLayer);

    void onFling(float startX, float startY, float velocityX, float velocityY, boolean rootLayer);

    void cancelFling(boolean rootLayer);

    void onScale(float scaleFactor, float focusX, float focusY, boolean rootLayer);

    void onRotate(float rotateDegree, float focusX, float focusY, boolean rootLayer);
}
