package robin.com.robinimageeditor.layer.crop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.util.ArrayMap;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import robin.com.robinimageeditor.data.savestate.CropSaveState;
import robin.com.robinimageeditor.editcache.LayerEditCache;
import robin.com.robinimageeditor.data.Pair;
import robin.com.robinimageeditor.data.savestate.SaveStateMarker;
import robin.com.robinimageeditor.layer.LayerComposite;
import robin.com.robinimageeditor.layer.RootEditorDelegate;
import robin.com.robinimageeditor.layer.base.LayerCacheNode;
import robin.com.robinimageeditor.layer.base.LayerViewProvider;
import robin.com.robinimageeditor.utils.MatrixUtils;
import robin.com.robinimageeditor.view.FuncAndActionBarAnimHelper;

/**
 * Created by Robin Yang on 1/17/18.
 */

public class CropHelper implements CropDetailsView.OnCropOperationListener, LayerCacheNode {

    private CropView mCropView;
    private CropDetailsView mCropDetailsView;
    private LayerViewProvider mProvider;
    private CropSaveState mCropSaveState;
    private float mCropScaleRatio = 0f;
    private RootEditorDelegate mRootEditorDelegate;
    private FuncAndActionBarAnimHelper mFuncAndActionBarAnimHelper;
    private LayerComposite mLayerComposite;
    private ArrayMap mSavedStateMap = new ArrayMap<String, CropSaveState>();

    public CropHelper(CropView cropView, CropDetailsView cropDetailsView, LayerViewProvider provider) {
        this.mCropView = cropView;
        this.mCropDetailsView = cropDetailsView;
        this.mProvider = provider;
        init();
    }

    private void init() {
        mRootEditorDelegate = mProvider.getRootEditorDelegate();
        mFuncAndActionBarAnimHelper = mProvider.getFuncAndActionBarAnimHelper();
        mLayerComposite = mProvider.getLayerCompositeView();

        mCropView.setOnCropViewUpdatedListener(new CropView.OnCropViewUpdatedListener() {
            @Override
            public void onCropViewUpdated() {
                mCropDetailsView.setRestoreTextStatus(true);
            }
        });
        mCropDetailsView.setCropListener(this);
    }

    @Override
    public void onCropRotation(float degree) {
        mRootEditorDelegate.setRotationBy(degree);
    }

    @Override
    public void onCropCancel() {
        closeCropDetails();
    }

    @Override
    public void onCropConfirm() {
        if (mCropView.isCropWindowEdit() || mCropSaveState != null) {
            Matrix lastMatrix = mRootEditorDelegate.getSupportMatrix();
            Bitmap lastBitmap = mRootEditorDelegate.getDisplayBitmap();  // 这里拿到的bitmap始终是最开始显示那张(Original photo),不是裁剪后的图片
            RectF lastDisplayRect = mRootEditorDelegate.getDisplayingRect();
            Matrix lastBaseMatrix = mRootEditorDelegate.getBaseLayoutMatrix();
            RectF lastCropRect = mCropView.getCropRect();
            if (mCropSaveState != null) {
                mCropSaveState.setCropRect(lastCropRect);
                mCropSaveState.setLastDisplayRectF(lastDisplayRect);
                mCropSaveState.setSupportMatrix(lastMatrix);
            }
            if (mCropSaveState == null && lastBitmap != null) {
                mCropSaveState = new CropSaveState(lastBitmap, lastDisplayRect, lastBaseMatrix,
                        lastMatrix, lastCropRect, mCropScaleRatio);
            }
            Bitmap cropBitmap = getCropBitmap(lastCropRect, mCropSaveState.getOriginalBitmap(),
                    mCropSaveState.getSupportMatrix(), mCropSaveState.getLastDisplayRectF());
            if (mCropSaveState.getCropBitmap() != cropBitmap) {
                MatrixUtils.recycleBitmap(mCropSaveState.getCropBitmap());
                mCropSaveState.setCropBitmap(cropBitmap);
            }
            if (cropBitmap == mCropSaveState.getOriginalBitmap()) {
                mCropSaveState.setCropBitmap(null);
                mCropSaveState = null;
                //mRootEditorDelegate.setDisplayBitmap(it.originalBitmap)
            }
        }
        closeCropDetails();
    }

    @Override
    public void onCropRestore() {
        force2SetupCropView();
    }

    public void showCropDetails() {
        mFuncAndActionBarAnimHelper.setInterceptDirtyAnimation(true);
        mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(false, new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                showOrHideCropViewDetails(true);
                setupCropView();
            }
        });
    }

    private void closeCropDetails() {
        mFuncAndActionBarAnimHelper.setInterceptDirtyAnimation(false);
        showOrHideCropViewDetails(false);
        mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(true);
        closeCropView();
    }

    // open cropView
    private void setupCropView() {
        //restore last cropSavedState
        if (mCropSaveState != null) {
            Bitmap showingBitmap = mRootEditorDelegate.getDisplayBitmap();
            if (mCropSaveState.getCropBitmap() != showingBitmap) {
                if (mCropSaveState.getCropBitmap() != null) {
                    mCropSaveState.getCropBitmap().recycle();
                }
                mCropSaveState.setCropBitmap(showingBitmap);
            }
            // 1.reset support matrix
            mRootEditorDelegate.resetEditorSupportMatrix(new Matrix());
            // 2.set cropView's bitmap with original Bitmap
            mRootEditorDelegate.setDisplayBitmap(mCropSaveState.getOriginalBitmap());
            // 3.set support matrix again,and set up crop rect
            mRootEditorDelegate.getRooView().addOnLayoutChangeListener(new LayerImageOnLayoutChangeListener());
            mCropDetailsView.setRestoreTextStatus(true);
        }

        //force2SetCropView
        if (mCropSaveState == null) {
            force2SetupCropView();
        }

        //other layer do not handle touch event.
        mLayerComposite.setHandlingEvent(false);
    }

    private void force2SetupCropView() {
        //get proper scale ration and start set up crop rect.
        if (mCropScaleRatio <= 0) {
            mCropDetailsView.getView().post(new Runnable() {
                @Override
                public void run() {
                    mCropScaleRatio = getCropRatio(mCropDetailsView.getView().getHeight());
                    initSetupViewWithScale();
                }
            });
        } else {
            initSetupViewWithScale();
        }
    }

    private void initSetupViewWithScale() {
        //1.reset minScale and setScale
        mRootEditorDelegate.resetMinScale(mCropScaleRatio);
        mRootEditorDelegate.setScale(mCropScaleRatio, false);
        if (mCropView.getLastRotateDegree() != 0) {
            mRootEditorDelegate.setRotationBy(-mCropView.getLastRotateDegree());   // 将旋转还原为0
        }
        //2.update crop drawing rect
        RectF rect = mRootEditorDelegate.getDisplayingRect();
        mCropView.setupCropRect(rect);
        mCropView.updateCropMaxSize(rect.width(), rect.height());
        mCropDetailsView.setRestoreTextStatus(false);
    }

    private float getCropRatio(int cropDetailsHeight) {
        Pair<Integer, Integer> screenPair = mProvider.getScreenSizeInfo();
        float editorHeight = mRootEditorDelegate.getDisplayingRect().height();
        int maxEditorH = screenPair.getSecond() - 2 * cropDetailsHeight;
        float scaleRatio = maxEditorH * 1.0f / editorHeight;
        if (scaleRatio > 0.95f) {
            return 0.95f;
        } else {
            return scaleRatio;
        }
    }

    private void closeCropView() {
        //1.clear crop drawing cache
        mCropView.clearDrawingRect();
        //2.reset min scale
        mRootEditorDelegate.resetMinScale(1.0f);
        if (mCropSaveState != null) {
            CropSaveState state = mCropSaveState;
            if (mCropSaveState.getCropBitmap() != null) {
                //3.reset editor support matrix and showing the cropped bitmap
                resetEditorSupportMatrix(state);
                mRootEditorDelegate.setDisplayBitmap(mCropSaveState.getCropBitmap());
            }
        }
        //4.no saved state just release scale
        if (mCropSaveState == null) {
            mRootEditorDelegate.setScale(1.0f, false);
        }
        mLayerComposite.setHandlingEvent(true);
    }

    public void resetEditorSupportMatrix(CropSaveState state) {
        /*convert rootLayer display matrix 2 supportMatrix <Fit-center>*/
        RectF viewRect = new RectF(0f, 0f, mRootEditorDelegate.getRooView().getWidth(),
                mRootEditorDelegate.getRooView().getHeight());
        Matrix realMatrix = mapCropRect2FitCenter(state.getCropRect(), viewRect);
        state.setCropFitCenterMatrix(realMatrix);
        Matrix editorMatrix = new Matrix();
        editorMatrix.postConcat(state.getSupportMatrix());
        editorMatrix.postConcat(realMatrix);
        mRootEditorDelegate.resetEditorSupportMatrix(editorMatrix);
    }

    private void showOrHideCropViewDetails(boolean show) {
        mCropDetailsView.showOrHide(show);
    }

    private Matrix mapCropRect2FitCenter(RectF lastCropRectF, RectF viewRectF) {
        Matrix matrix = new Matrix();
        matrix.setRectToRect(lastCropRectF, viewRectF, Matrix.ScaleToFit.CENTER);
        return matrix;
    }

    public CropSaveState getCropSaveState() {
        if (mCropSaveState != null) {
            return mCropSaveState;
        }
        return null;
    }

    @Override
    public void saveLayerEditData(HashMap<String, LayerEditCache> cacheDataHashMap) {
        String cropLayerTag = getLayerTag();
        if (mCropSaveState != null) {
            mCropSaveState.reset();
            mSavedStateMap.put(cropLayerTag, mCropSaveState);
            cacheDataHashMap.put(cropLayerTag, new LayerEditCache(new ArrayMap<String, SaveStateMarker>(mSavedStateMap)));
        }
        if (mCropSaveState == null) {
            cacheDataHashMap.remove(cropLayerTag);
        }
    }

    @Override
    public void restoreLayerEditData(HashMap<String, LayerEditCache> cacheDataHashMap) {
        String cropLayerTag = getLayerTag();
        LayerEditCache cachedData = cacheDataHashMap.get(cropLayerTag);
        if (cachedData != null) {
            Map<String, SaveStateMarker> layerCache = cachedData.getLayerCache();
            if (layerCache.size() != 0) {
                SaveStateMarker result = layerCache.get(cropLayerTag);
                mCropSaveState = (CropSaveState) result.deepCopy();
            }
        }
    }

    public Bitmap restoreBitmapByCropSaveState(Bitmap originalBitmap) {
        Bitmap cropBitmap = null;
        if (mCropSaveState != null) {
            mCropSaveState.setOriginalBitmap(originalBitmap);
            cropBitmap = getCropBitmap(mCropSaveState.getCropRect(), mCropSaveState.getOriginalBitmap(),
                    mCropSaveState.getSupportMatrix(), mCropSaveState.getLastDisplayRectF());
            //resetEditorSupportMatrix(it)
            mCropScaleRatio = mCropSaveState.getOriginalCropRation();
        }
        if (cropBitmap == null) {
            mCropSaveState = null;
            mCropScaleRatio = 0f;
            return originalBitmap;
        }
        return cropBitmap;
    }

    /*getCropBitmap....*/
    public Bitmap getCropBitmap(RectF cropRect, Bitmap source, Matrix supportMatrix, RectF displayRect) {
        Bitmap rotated = getRotatedBitmap(source, supportMatrix);
        Rect realCropRect = calcCropRect(source.getWidth(), source.getHeight(), cropRect, supportMatrix, displayRect);
        if (realCropRect == null) {
            return null;
        }
        Bitmap cropped = Bitmap.createBitmap(
                rotated,
                realCropRect.left,
                realCropRect.top,
                realCropRect.width(),
                realCropRect.height(),
                null,
                false
        );
        if (rotated != cropped && rotated != source) {
            rotated.recycle();
        }
        return cropped;
    }

    private Rect calcCropRect(int originalImageWidth, int originalImageHeight, RectF cropRect, Matrix supportMatrix, RectF displayRect) {
        RectF mImageRect = displayRect;
        if (mImageRect == null) {
            return null;
        }
        float mAngle = getRotateDegree(supportMatrix);
        float scaleToOriginal = getRotatedWidth(mAngle, originalImageWidth, originalImageHeight) / mImageRect.width();
        float offsetX = mImageRect.left * scaleToOriginal;
        float offsetY = mImageRect.top * scaleToOriginal;
        int left = Math.round(cropRect.left * scaleToOriginal - offsetX);
        int top = Math.round(cropRect.top * scaleToOriginal - offsetY);
        int right = Math.round(cropRect.right * scaleToOriginal - offsetX);
        int bottom = Math.round(cropRect.bottom * scaleToOriginal - offsetY);
        int imageW = Math.round(getRotatedWidth(mAngle, originalImageWidth, originalImageHeight));
        int imageH = Math.round(getRotatedHeight(mAngle, originalImageWidth, originalImageHeight));
        return new Rect(Math.max(left, 0), Math.max(top, 0), Math.min(right, imageW),
                Math.min(bottom, imageH));
    }

    private Bitmap getRotatedBitmap(Bitmap bitmap, Matrix matrix) {
        Matrix rotateMatrix = new Matrix();
        rotateMatrix.setRotate(getRotateDegree(matrix), bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                rotateMatrix, true);
    }

    private float getRotatedWidth(float angle, float width, float height) {
        if (angle % 180 == 0f) {
            return width;
        } else {
            return height;
        }
    }

    private float getRotatedHeight(float angle, float width, float height) {
        if (angle % 180 == 0f) {
            return height;
        } else {
            return width;
        }
    }

    private float getRotateDegree(Matrix matrix) {
        return MatrixUtils.getMatrixDegree(matrix);
    }

    @Override
    public String getLayerTag() {
        return this.getClass().getSimpleName();
    }

    private class LayerImageOnLayoutChangeListener implements View.OnLayoutChangeListener {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (mCropSaveState != null) {
                mCropView.setupCropRect(mCropSaveState.getCropRect());
                Matrix matrix = new Matrix();
                matrix.set(mCropSaveState.getSupportMatrix());
                //matrix.postConcat(MatrixUtils.getInvertMatrix(it.cropFitCenterMatrix))
                mRootEditorDelegate.setSupportMatrix(matrix);
            }
            mRootEditorDelegate.getRooView().removeOnLayoutChangeListener(this);
        }
    }
}
