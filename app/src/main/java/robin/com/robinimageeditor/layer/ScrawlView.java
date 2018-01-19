package robin.com.robinimageeditor.layer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.Iterator;

import robin.com.robinimageeditor.bean.ScrawlSaveState;
import robin.com.robinimageeditor.layer.base.BasePaintLayerView;
import robin.com.robinimageeditor.util.Utils;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class ScrawlView extends BasePaintLayerView<ScrawlSaveState> {

    private Paint mDrawPaint;

    public ScrawlView(Context context) {
        super(context);
    }

    public ScrawlView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrawlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScrawlView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void initSupportView(Context context) {
        super.initSupportView(context);
        mDrawPaint = new Paint();
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setDither(true);
        mDrawPaint.setColor(Color.RED);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setPathEffect(new CornerPathEffect(10));
        mDrawPaint.setStrokeWidth(Utils.dp2px(context, 6f));
    }

    @Override
    protected void drawDragPath(Path paintPath) {
        super.drawDragPath(paintPath);
        if (displayCanvas != null) {
            displayCanvas.drawPath(paintPath, mDrawPaint);
        }
        invalidate();
    }

    @Override
    protected ScrawlSaveState savePathOnFingerUp(Path paintPath) {
        return new ScrawlSaveState(Utils.copyPaint(mDrawPaint), paintPath);
    }

    @Override
    protected void drawAllCachedState(Canvas canvas) {
        Iterator<ScrawlSaveState> iterator = saveStateMap.values().iterator();
        while (iterator.hasNext()) {
            ScrawlSaveState state = iterator.next();
            canvas.drawPath(state.getPath(), state.getPaint());
        }
    }

    public void setPaintColor(int color) {
        mDrawPaint.setColor(color);
    }
}
