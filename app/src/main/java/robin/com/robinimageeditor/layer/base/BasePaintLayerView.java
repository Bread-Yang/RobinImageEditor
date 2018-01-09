package robin.com.robinimageeditor.layer.base;

import android.content.Context;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import robin.com.robinimageeditor.bean.SaveStateMarker;
import robin.com.robinimageeditor.util.Utils;

/**
 * ## Base paintingLayerView  for [ScrawlView] and [MosaicView]
 *  It's hold move path[paintPath] for user's finger move
 *
 * Created by Robin Yang on 12/29/17.
 */
public abstract class BasePaintLayerView<T extends SaveStateMarker> extends BaseLayerView<T> {

    protected Path paintPath;
    protected boolean currentPathValidate;

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
        paintPath = new Path();
        PointF result = Utils.mapInvertMatrixPoint(getDrawMatrix(), new PointF(downX, downY));
        if (paintPath != null) {
            paintPath.moveTo(result.x, result.y);
        }
    }

    @Override
    public void onDrag(float dx, float dy, float x, float y, boolean rootLayer) {
        if (!rootLayer) {
            if (paintPath != null) {
                PointF result = Utils.mapInvertMatrixPoint(getDrawMatrix(), new PointF(x, y));
                if (!interceptDrag(x, y)) {
                    paintPath.lineTo(result.x, result.y);
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
