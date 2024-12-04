package robin.com.robinimageeditor.layer.scrawl;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.Iterator;

import robin.com.robinimageeditor.data.savestate.ScrawlSaveState;
import robin.com.robinimageeditor.layer.base.BaseLayerView;
import robin.com.robinimageeditor.layer.base.BasePaintLayerView;
import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * Photo scrawl edit layer.
 *
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
        mDrawPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawPaint.setStyle(Paint.Style.STROKE);
        mDrawPaint.setColor(Color.RED);
        mDrawPaint.setAntiAlias(true);
        mDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawPaint.setStrokeCap(Paint.Cap.ROUND);
//        mDrawPaint.setDither(true);
        mDrawPaint.setPathEffect(new CornerPathEffect(10));
        mDrawPaint.setStrokeWidth(MatrixUtils.dp2px(context, 3f));
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
        return new ScrawlSaveState(MatrixUtils.copyPaint(mDrawPaint), paintPath);
    }

    @Override
    protected void drawAllCachedState(Canvas canvas) {
        /**
         * 这里所有历史编辑记录的Path的x,y坐标, 都是在通过getDrawMatrix()的逆矩阵计算, 得到原图在没有做过任何缩放,平移,裁剪操作情况下的x',y'坐标
         * 在{@link BaseLayerView#onDraw}方法调用时,
         * 1.先把所有的path, 通过{@link BaseLayerView#displayCanvas}, 画在没有经过任何变换操作的{@link BaseLayerView#displayBitmap}上
         * 2.再把{@link BaseLayerView#displayBitmap}, 通过canvas.drawBitmap(displayBitmap, getDrawMatrix(), null), 在经过getDrawMatrix()的变换后, 画到canvas上
         */
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
