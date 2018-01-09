package robin.com.robinimageeditor.layer;

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

import robin.com.robinimageeditor.bean.MosaicSaveState;
import robin.com.robinimageeditor.layer.base.BasePaintLayerView;
import robin.com.robinimageeditor.util.MosaicUtils;
import robin.com.robinimageeditor.util.Utils;
import robin.com.robinimageeditor.view.MosaicMode;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicView extends BasePaintLayerView<MosaicSaveState> {

    private Bitmap mGridMosaicCover;
    private Bitmap mBlurMosaicCover;
    private MosaicMode mMosaicMode;
    private int mLastBitmapId;
    private Paint mMosaicPaint;
    private Xfermode mMosaicPaintMode;

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
        mMosaicPaint.setStrokeWidth(Utils.dp2px(context, 30));
        mMosaicPaintMode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Utils.recycleBitmap(mGridMosaicCover);
        Utils.recycleBitmap(mBlurMosaicCover);
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
            if (drawMosaicLayer(displayCanvas, mMosaicMode, paintPath)) {
                invalidate();
            }
        }
    }

    private boolean drawMosaicLayer(Canvas canvas, MosaicMode mode, Path paintPath) {
        Bitmap cover = getMosaicCover(mode);
        if (cover == null) {
            return false;
        }
        int count = Utils.saveEntireLayer(canvas);
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
            return new MosaicSaveState(mMosaicMode, paintPath);
        }
        return null;
    }

    @Override
    protected void drawAllCachedState(Canvas canvas) {
        Iterator<MosaicSaveState> iterator = saveStateMap.values().iterator();
        while (iterator.hasNext()) {
            MosaicSaveState state = iterator.next();
            drawMosaicLayer(canvas, state.getMode(), state.getPath());
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
                Utils.recycleBitmap(mGridMosaicCover);
                mBlurMosaicCover = MosaicUtils.getGridMosaic(mosaicBitmap);
            }
        } else if (mosaicMode == MosaicMode.Blur) {
            if (sameFromLast) {
                if (mBlurMosaicCover == null) {
                    mBlurMosaicCover = MosaicUtils.getBlurMosaic(mosaicBitmap);
                }
            } else {
                Utils.recycleBitmap(mBlurMosaicCover);
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
}
