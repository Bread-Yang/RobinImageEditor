package robin.com.robinimageeditor.layer.base;

import android.content.Context;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import robin.com.robinimageeditor.data.savestate.SaveStateMarker;
import robin.com.robinimageeditor.layer.mosaic.MosaicView;
import robin.com.robinimageeditor.layer.scrawl.ScrawlView;
import robin.com.robinimageeditor.utils.MatrixUtils;
import robin.com.robinimageeditor.view.ActionFrameLayout;

/**
 * Base painting LayerView for {@link ScrawlView} and {@link MosaicView}
 * It's hold move path{@link paintPath} for user's finger move
 * <p>
 * Created by Robin Yang on 12/29/17.
 */
public abstract class BasePaintLayerView<T extends SaveStateMarker> extends BaseLayerView<T> {

    /**
     * When finger down, new a Path to it, when finger up, save this path and null it.
     */
    protected Path paintPath;
    /**
     * Decide whether save the {@link paintPath} to {@link saveStateMap}, When finger move, set it true.
     */
    protected boolean currentPathValidate;
    private float mLastX, mLastY;

    public BasePaintLayerView(Context context) {
        super(context);
    }

    public BasePaintLayerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePaintLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BasePaintLayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean checkInterceptedOnTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            if (!validateRect.contains(event.getX(), event.getY())) {
                return false;
            }
        }
        return super.checkInterceptedOnTouchEvent(event);
    }

    @Override
    public void onFingerDown(float downX, float downY) {
        super.onFingerDown(downX, downY);
        genDisplayCanvas();
        PointF result = MatrixUtils.mapInvertMatrixPoint(getDrawMatrix(), new PointF(downX, downY));

        mLastX = result.x;
        mLastY = result.y;
    }

    @Override
    public void onDrag(float dx, float dy, float x, float y, boolean rootLayer) {
        if (!rootLayer) {
            PointF result = MatrixUtils.mapInvertMatrixPoint(getDrawMatrix(), new PointF(x, y));

            if (paintPath == null) {
                float distanceX = Math.abs(result.x - mLastX);
                float distanceY = Math.abs(result.y - mLastY);

                double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

                if (distance >= 8) {
                    paintPath = new Path();
                    paintPath.moveTo(mLastX, mLastY);
                }
            }

            if (paintPath != null) {
                if (!interceptDrag(x, y)) {
//                    paintPath.lineTo(result.x, result.y);
                    paintPath.quadTo(mLastX, mLastY, (result.x + mLastX) / 2, (result.y + mLastY) / 2);
                    mLastX = result.x;
                    mLastY = result.y;
                    currentPathValidate = true;
                    drawDragPath(paintPath);
                }
            }
        }
    }

    private void upOrCancelFinger() {
        if (paintPath != null) {
            if (currentPathValidate) {
                T result = savePathOnFingerUp(paintPath);
                if (result != null) {
                    saveStateMap.put(result.getId(), result);
                }
            }
        }
        paintPath = null;
        currentPathValidate = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 在Scrawl模式或者Mosaic模式时，当多点触控触发时，不消费事件(这样可以让PhotoView放大缩小)
        if (ActionFrameLayout.sIsMultipleTouching) {
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onFingerCancel() {
        super.onFingerCancel();
        upOrCancelFinger();
    }

    @Override
    public void onFingerUp(float upX, float upY) {
        super.onFingerUp(upX, upY);
        upOrCancelFinger();
    }

    @Override
    public void revoke() {
        if (saveStateMap.size() > 0) {
            saveStateMap.removeAt(saveStateMap.size() - 1);
            redrawAllCache();
        }
    }

    protected void drawDragPath(Path paintPath) {

    }

    protected boolean interceptDrag(float x, float y) {
        return false;
    }

    protected abstract T savePathOnFingerUp(Path paintPath);
}
