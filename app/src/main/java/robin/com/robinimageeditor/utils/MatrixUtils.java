package robin.com.robinimageeditor.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * |  MScale_X   MSkew_X   MTrans_X  |
 * |  MSkew_Y    MScale_Y  MTrans_Y  |
 * |  MPersp_0   MPersp_1  MPersp_2  |
 *
 * Created by Robin Yang on 12/28/17.
 */

public class MatrixUtils {

    private static int randomId = 0;

    //region context methods
    public static int dp2px(Context context, float dp) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int sp2px(Context context, float sp) {
        float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }

    public static int getResourceColor(Context context, int resId) {
        return context.getResources().getColor(resId);
    }

    public static String getResourceString(Context context, int resId) {
        return context.getResources().getString(resId);
    }
    //endregion

    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }

    public static String randomId() {
        randomId++;
        return String.valueOf(randomId);
    }

    public static Paint copyPaint(Paint copyPaint) {
        Paint paint = new Paint();
        paint.setColor(copyPaint.getColor());
        paint.setAntiAlias(copyPaint.isAntiAlias());
        paint.setStrokeJoin(copyPaint.getStrokeJoin());
        paint.setStrokeCap(copyPaint.getStrokeCap());
        paint.setStyle(copyPaint.getStyle());
        paint.setStrokeWidth(copyPaint.getStrokeWidth());
        return paint;
    }

    public static void checkZoomLevels(float minZoom, float midZoom, float maxZoom) {
        if (minZoom >= midZoom) {
            throw new IllegalArgumentException(
                    "Minimum zoom has to be less than Medium zoom. Call setMinimumZoom() with a more appropriate value");
        } else if (midZoom >= maxZoom) {
            throw new IllegalArgumentException(
                    "Medium zoom has to be less than Maximum zoom. Call setMaximumZoom() with a more appropriate value");
        }
    }

    public static boolean hasDrawable(ImageView imageView) {
        return imageView.getDrawable() != null;
    }

    public static boolean isSupportedScaleType(ImageView.ScaleType scaleType) {
        if (scaleType == null) {
            return false;
        }
        switch (scaleType) {
            case MATRIX:
                throw new IllegalStateException("Matrix scale type is not supported");
        }
        return true;
    }

    public static void changeSelectedStatus(ViewGroup viewGroup, int position) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            viewGroup.getChildAt(i).setSelected(i == position);
        }
    }

    //region activity methods
    public static void showStatusBar(Activity activity) {
        fullScreen(false, activity);
    }

    public static void hideStatusBar(Activity activity) {
        fullScreen(true, activity);
    }

    private static void fullScreen(boolean enable, Activity activity) {
        if (Build.VERSION.SDK_INT >= 19) {
            int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            View decorView = activity.getWindow().getDecorView();
            int systemUiVisibility = decorView.getSystemUiVisibility();
            if (enable) {
                systemUiVisibility = systemUiVisibility | flags;
            } else {
                systemUiVisibility = systemUiVisibility & (~flags & 0xff);
            }
            decorView.setSystemUiVisibility(systemUiVisibility);
        }
    }
    //endregion

    //region rect extend methods
    public static void RectFIncrease(RectF rectF, float dx, float dy) {
        rectF.left -= dx;
        rectF.top -= dx;
        rectF.right += dy;
        rectF.bottom += dy;
    }

    public static void RectFSchedule(RectF rectF, float centerX, float centerY, float width, float height) {
        rectF.left = centerX - width / 2;
        rectF.top = centerY - height / 2;
        rectF.right = centerX + width / 2;
        rectF.bottom = centerY + height / 2;
    }

    public static void RectFSetInt(RectF rectF, int left, int top, int right, int bottom) {
        rectF.left = left;
        rectF.top = top;
        rectF.right = right;
        rectF.bottom = bottom;
    }
    //endregion

    //region matrix extend methods
    public static float getMatrixValue(Matrix matrix, int whichValue) {
        float[] matrixValues = new float[9];
        matrix.getValues(matrixValues);
        return matrixValues[whichValue];
    }

    public static float getMatrixScale(Matrix matrix) {
        return (float) Math.sqrt((Math.pow(getMatrixValue(matrix, Matrix.MSCALE_X), 2.0) + Math.pow(getMatrixValue(matrix, Matrix.MSKEW_Y), 2.0)));
    }

    public static float getMatrixDegree(Matrix matrix) {
        return -Math.round(Math.atan2(getMatrixValue(matrix, Matrix.MSKEW_X), getMatrixValue(matrix, Matrix.MSCALE_X)) * (180 / Math.PI));
    }

    public static float getMatrixTransX(Matrix matrix) {
        return getMatrixValue(matrix, Matrix.MTRANS_X);
    }

    public static float getMatrixTransY(Matrix matrix) {
        return getMatrixValue(matrix, Matrix.MTRANS_Y);
    }

    public static PointF mapInvertMatrixPoint(Matrix matrix, PointF point) {
        Matrix invert = new Matrix();
        matrix.invert(invert);
        float[] src = new float[]{point.x, point.y};
        float[] dst = new float[2];
        invert.mapPoints(dst, src);
        return new PointF(dst[0], dst[1]);
    }

    public static float[] mapInvertMatrixTranslate(Matrix matrix, float dx, float dy) {
        Matrix tempMatrix = new Matrix();
        Matrix invertMatrix = new Matrix();
        tempMatrix.set(matrix);
        tempMatrix.invert(invertMatrix);
        float startX = MatrixUtils.getMatrixTransX(invertMatrix);
        float startY = MatrixUtils.getMatrixTransY(invertMatrix);
        tempMatrix.postTranslate(dx, dy);
        invertMatrix.reset();
        tempMatrix.invert(invertMatrix);
        return new float[]{startX - getMatrixTransX(invertMatrix), startY - getMatrixTransY(invertMatrix)};
    }

    public static float[] mapInvertMatrixScale(Matrix matrix, float scaleX, float scaleY) {
        Matrix tempMatrix = new Matrix();
        Matrix invertMatrix = new Matrix();
        tempMatrix.set(matrix);
        tempMatrix.invert(invertMatrix);
        float startScaleX = MatrixUtils.getMatrixScale(invertMatrix);
        float startScaleY = MatrixUtils.getMatrixScale(invertMatrix);
        tempMatrix.postScale(scaleX, scaleY);
        invertMatrix.reset();
        tempMatrix.invert(invertMatrix);
        return new float[]{startScaleX / getMatrixScale(invertMatrix), startScaleY / getMatrixScale(invertMatrix)};
    }
    //endregion

    //region canvas extend methods
    public static int saveEntireLayer(Canvas canvas) {
        return canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
    }
    //endregion
}
