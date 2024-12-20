package robin.com.robinimageeditor.layer.mosaic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import java.util.Iterator;

import robin.com.robinimageeditor.data.savestate.MosaicSaveState;
import robin.com.robinimageeditor.layer.base.BaseLayerView;
import robin.com.robinimageeditor.layer.base.BasePaintLayerView;
import robin.com.robinimageeditor.utils.MatrixUtils;
import robin.com.robinimageeditor.utils.MosaicUtils;

/**
 * Created by Robin Yang on 1/8/18.
 */
public class MosaicView extends BasePaintLayerView<MosaicSaveState> {

    private Bitmap mGridMosaicCover;  // 把整张原图变成一格一格
    private Bitmap mBlurMosaicCover;  // 把整张原图变成模糊
    private MosaicMode mMosaicMode;
    private int mLastBitmapId;
    private Paint mMosaicPaint;
    private Xfermode mMosaicPaintMode;

    // 这个initializeMatrix, 是ImageEditorActivity在onLayoutChange回调时,拿到其PhotoView的baseMatrix来设置的
    // 因为马赛克的实现原理是用上面的mGridMosaicCover或者mBlurMosaicCover, 重叠上path, 再把整张bitmap画上去来实现, 所以要算上PhotoView一开始的缩放值
    private Matrix initializeMatrix = new Matrix();

    public MosaicView(Context context) {
        super(context);
    }

    public MosaicView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MosaicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MosaicView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initSupportView(Context context) {
        super.initSupportView(context);
        mMosaicPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMosaicPaint.setStyle(Paint.Style.STROKE);
        mMosaicPaint.setColor(Color.BLACK);
        mMosaicPaint.setAntiAlias(true);
        mMosaicPaint.setStrokeJoin(Paint.Join.ROUND);
        mMosaicPaint.setStrokeCap(Paint.Cap.ROUND);
        mMosaicPaint.setPathEffect(new CornerPathEffect(10));
        mMosaicPaint.setStrokeWidth(MatrixUtils.dp2px(context, 30));
        mMosaicPaintMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        MatrixUtils.recycleBitmap(mGridMosaicCover);
        MatrixUtils.recycleBitmap(mBlurMosaicCover);
    }

    private Bitmap getMosaicCover(MosaicMode mosaicMode) {
        if (mosaicMode == null) {
            return null;
        } else if (mosaicMode == MosaicMode.Grid) {
            return mGridMosaicCover;
        } else if (mosaicMode == MosaicMode.Blur) {
            return mBlurMosaicCover;
        }
        return null;
    }

    @Override
    protected boolean interceptDrag(float x, float y) {
        return !validateRect.contains(x, y);
    }

    @Override
    protected void drawDragPath(Path paintPath) {
        super.drawDragPath(paintPath);
        if (displayCanvas != null) {
            if (drawMosaicLayer(displayCanvas, mMosaicMode, paintPath, mMosaicPaint.getStrokeWidth())) {
                invalidate();
            }
        }
    }

    private boolean drawMosaicLayer(Canvas canvas, MosaicMode mode, Path paintPath, float strokeWidth) {
        Bitmap cover = getMosaicCover(mode);
        if (cover == null) {
            return false;
        }
        int count = MatrixUtils.saveEntireLayer(canvas);
        mMosaicPaint.setStrokeWidth(strokeWidth);
        canvas.drawPath(paintPath, mMosaicPaint);
        mMosaicPaint.setXfermode(mMosaicPaintMode);
        canvas.drawBitmap(cover, initializeMatrix, mMosaicPaint); // 显示和path的交集
        mMosaicPaint.setXfermode(null);
        canvas.restoreToCount(count);
        return true;
    }

    @Override
    protected MosaicSaveState savePathOnFingerUp(Path paintPath) {
        if (mMosaicMode != null) {
            return new MosaicSaveState(mMosaicMode, paintPath, mMosaicPaint.getStrokeWidth());
        }
        return null;
    }

    @Override
    protected void drawAllCachedState(Canvas canvas) {
        /**
         * 这里重画的所有历史编辑记录的Path的x,y坐标, 都是在通过getDrawMatrix()的逆矩阵计算, 得到原图在没有做过任何缩放,平移,裁剪操作情况下的x',y'坐标
         * 在{@link BaseLayerView#onDraw}方法调用时,
         * 1.先把所有的path, 通过{@link BaseLayerView#displayCanvas}, 画在没有经过任何变换操作的{@link BaseLayerView#displayBitmap}上
         * 2.再把{@link BaseLayerView#displayBitmap}, 通过canvas.drawBitmap(displayBitmap, getDrawMatrix(), null), 在经过getDrawMatrix()的变换后, 画到canvas上
         */
        Iterator<MosaicSaveState> iterator = saveStateMap.values().iterator();
        while (iterator.hasNext()) {
            MosaicSaveState state = iterator.next();
            drawMosaicLayer(canvas, state.getMode(), state.getPath(), state.getPaintStrokeWidth());
        }
    }

    public void setMosaicMode(MosaicMode mosaicMode, Bitmap mosaicBitmap) {
        mMosaicMode = mosaicMode;
        if (mosaicBitmap == null) {
            return;
        }
        int bitmapId = mosaicBitmap.hashCode();
        boolean sameFromLast = (mLastBitmapId == bitmapId);
        if (mosaicMode == MosaicMode.Grid) {
            if (sameFromLast) {
                if (mGridMosaicCover == null) {
                    mGridMosaicCover = MosaicUtils.getGridMosaic(mosaicBitmap);
                }
            } else {
                MatrixUtils.recycleBitmap(mGridMosaicCover);
                mBlurMosaicCover = MosaicUtils.getGridMosaic(mosaicBitmap);
            }
        } else if (mosaicMode == MosaicMode.Blur) {
            if (sameFromLast) {
                if (mBlurMosaicCover == null) {
                    mBlurMosaicCover = MosaicUtils.getBlurMosaic(mosaicBitmap);
                }
            } else {
                MatrixUtils.recycleBitmap(mBlurMosaicCover);
                mBlurMosaicCover = MosaicUtils.getBlurMosaic(mosaicBitmap);
            }
        }
        mLastBitmapId = bitmapId;
    }

    public void setupForMosaicView(Bitmap mosaicBitmap) {
        mBlurMosaicCover = MosaicUtils.getBlurMosaic(mosaicBitmap);
        mGridMosaicCover = MosaicUtils.getGridMosaic(mosaicBitmap);
    }

    public void setInitializeMatrix(Matrix initializeMatrix) {
        this.initializeMatrix = initializeMatrix;
    }

    public void setPaintStrokeWidth(float width) {
        mMosaicPaint.setStrokeWidth(width);
    }
}
