package robin.com.robinimageeditor.data.savestate;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * Created by Robin Yang on 1/17/18.
 */

public class CropSaveState extends SaveStateMarker {

    private Bitmap originalBitmap;
    private RectF lastDisplayRectF;
    private Matrix originalMatrix;
    private Matrix supportMatrix;
    private RectF cropRect;
    private float originalCropRation;
    private Bitmap cropBitmap;
    private Matrix cropFitCenterMatrix = new Matrix();

    public CropSaveState(Bitmap originalBitmap, RectF lastDisplayRectF, Matrix originalMatrix,
                         Matrix supportMatrix, RectF cropRect, float originalCropRation) {
        this.originalBitmap = originalBitmap;
        this.lastDisplayRectF = lastDisplayRectF;
        this.originalMatrix = originalMatrix;
        this.supportMatrix = supportMatrix;
        this.cropRect = cropRect;
        this.originalCropRation = originalCropRation;
    }

    @Override
    public void reset() {
        MatrixUtils.recycleBitmap(originalBitmap);
        MatrixUtils.recycleBitmap(cropBitmap);
        cropFitCenterMatrix.reset();
    }

    @Override
    public SaveStateMarker deepCopy() {
        CropSaveState state = new CropSaveState(originalBitmap, new RectF(lastDisplayRectF),
                new Matrix(originalMatrix), new Matrix(supportMatrix), new RectF(cropRect), originalCropRation);
        state.setId(getId());
        return state;
    }

    public Bitmap getOriginalBitmap() {
        return originalBitmap;
    }

    public void setOriginalBitmap(Bitmap originalBitmap) {
        this.originalBitmap = originalBitmap;
    }

    public RectF getLastDisplayRectF() {
        return lastDisplayRectF;
    }

    public void setLastDisplayRectF(RectF lastDisplayRectF) {
        this.lastDisplayRectF = lastDisplayRectF;
    }

    public Matrix getOriginalMatrix() {
        return originalMatrix;
    }

    public void setOriginalMatrix(Matrix originalMatrix) {
        this.originalMatrix = originalMatrix;
    }

    public Matrix getSupportMatrix() {
        return supportMatrix;
    }

    public void setSupportMatrix(Matrix supportMatrix) {
        this.supportMatrix = supportMatrix;
    }

    public RectF getCropRect() {
        return cropRect;
    }

    public void setCropRect(RectF cropRect) {
        this.cropRect = cropRect;
    }

    public float getOriginalCropRation() {
        return originalCropRation;
    }

    public void setOriginalCropRation(float originalCropRation) {
        this.originalCropRation = originalCropRation;
    }

    public Bitmap getCropBitmap() {
        return cropBitmap;
    }

    public void setCropBitmap(Bitmap cropBitmap) {
        this.cropBitmap = cropBitmap;
    }

    public Matrix getCropFitCenterMatrix() {
        return cropFitCenterMatrix;
    }

    public void setCropFitCenterMatrix(Matrix cropFitCenterMatrix) {
        this.cropFitCenterMatrix = cropFitCenterMatrix;
    }
}
