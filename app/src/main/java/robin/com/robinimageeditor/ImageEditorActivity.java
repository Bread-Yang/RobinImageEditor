package robin.com.robinimageeditor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import robin.com.robinimageeditor.bean.Pair;
import robin.com.robinimageeditor.data.savestate.CropSaveState;
import robin.com.robinimageeditor.data.share.EditorPathSetup;
import robin.com.robinimageeditor.data.share.EditorResult;
import robin.com.robinimageeditor.data.share.LayerEditResult;
import robin.com.robinimageeditor.editcache.EditorCacheData;
import robin.com.robinimageeditor.editcache.PhotoEditCache;
import robin.com.robinimageeditor.layer.CropDetailsView;
import robin.com.robinimageeditor.layer.CropHelper;
import robin.com.robinimageeditor.layer.CropView;
import robin.com.robinimageeditor.layer.LayerComposite;
import robin.com.robinimageeditor.layer.LayerViewProvider;
import robin.com.robinimageeditor.layer.MosaicView;
import robin.com.robinimageeditor.layer.RootEditorDelegate;
import robin.com.robinimageeditor.layer.ScrawlView;
import robin.com.robinimageeditor.layer.StickerView;
import robin.com.robinimageeditor.layer.TextPastingView;
import robin.com.robinimageeditor.layer.base.BaseLayerView;
import robin.com.robinimageeditor.layer.base.LayerCacheNode;
import robin.com.robinimageeditor.layer.photoview.PhotoView;
import robin.com.robinimageeditor.util.EditorCompressUtils;
import robin.com.robinimageeditor.util.MatrixUtils;
import robin.com.robinimageeditor.view.ActionFrameLayout;
import robin.com.robinimageeditor.view.DragToDeleteView;
import robin.com.robinimageeditor.view.EditorMode;
import robin.com.robinimageeditor.view.FuncAndActionBarAnimHelper;
import robin.com.robinimageeditor.view.FuncHelper;
import robin.com.robinimageeditor.view.FuncModeToolFragment;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class ImageEditorActivity extends AppCompatActivity implements LayerViewProvider {

    private static final String TAG = "ImageEditorActivity";
    private static final String intentKey = "editorPathSetup";

    private EditorPathSetup mEditorPathSetup;
    private String mEditorId;
    private String mOriginalImageUrl;
    private int mEditorWidth;
    private int mEditorHeight;

    private RootEditorDelegate mRootEditorDelegate;
    private FuncAndActionBarAnimHelper mFuncAndActionBarAnimHelper;
    private FuncHelper mFuncHelper;
    private CropHelper mCropHelper;

    private ImageComposeTask imageComposeTask;

    private ActionFrameLayout actionFrameLayout;
    private View toolBar;
    private View flFunc;

    private PhotoView layerPhotoView;
    private FrameLayout layerEditorParent;
    private LayerComposite layerComposite;
    private MosaicView layerMosaicView;
    private ScrawlView layerScrawlView;
    private StickerView layerStickerView;
    private TextPastingView layerTextPastingView;
    private CropView layerCropView;

    private View layoutCropDetails;

    private TextView tvComplete;
    private TextView tvCancel;

    private RelativeLayout rltDragDelete;

    public static Intent intent(Context context, EditorPathSetup editorPathSetup) {
        Intent intent = new Intent(context, ImageEditorActivity.class);
        intent.putExtra(intentKey, editorPathSetup);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRootView();
        initData();
        initView();
        initActionBarListener();
    }

    private void initRootView() {
        if (Build.VERSION.SDK_INT >= 21) {
            this.getWindow().setStatusBarColor(MatrixUtils.getResourceColor(this, R.color.bg_black));
        }
        // transparent necessary
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        // flag necessary
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        ColorDrawable mBackgroundColor = new ColorDrawable(Color.BLACK);
        View view = View.inflate(this, R.layout.image_editor_activity, null);
        if (Build.VERSION.SDK_INT >= 16) {
            view.setBackground(mBackgroundColor);
        } else {
            view.setBackgroundDrawable(mBackgroundColor);
        }
        setContentView(view);

        actionFrameLayout = findViewById(R.id.actionFrameLayout);
        toolBar = findViewById(R.id.toolBar);
        flFunc = findViewById(R.id.flFunc);

        layerPhotoView = findViewById(R.id.layerPhotoView);
        layerScrawlView = findViewById(R.id.layerScrawlView);
        layerStickerView = findViewById(R.id.layerStickerView);
        layerTextPastingView = findViewById(R.id.layerTextPastingView);
        layerMosaicView = findViewById(R.id.layerMosaicView);
        layerCropView = findViewById(R.id.layerCropView);
        layerEditorParent = findViewById(R.id.layerEditorParent);
        layerComposite = findViewById(R.id.layerComposite);

        layoutCropDetails = findViewById(R.id.cropDetailsLayout);

        tvComplete = findViewById(R.id.tvComplete);
        tvCancel = findViewById(R.id.tvCancel);

        rltDragDelete = findViewById(R.id.rlDragDelete);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        EditorPathSetup editorPathSetup = (EditorPathSetup) intent.getSerializableExtra(intentKey);
        if (editorPathSetup == null) {
            Log.e(TAG, "editorPathSetup == null");
            finish();
            return;
        }
        mEditorPathSetup = editorPathSetup;
        String originalImageUrl = mEditorPathSetup.getOriginalImageUrl();
        String editedImageUrl = mEditorPathSetup.getEditedImageUrl();
        if (originalImageUrl == null && editedImageUrl == null) {
            Log.e(TAG, "originalImageUrl,editedImageUrl are both null");
            finish();
            return;
        }
    }

    private void initView() {
        List<EditorMode> functionalModeList = new ArrayList<>();
        functionalModeList.add(EditorMode.ScrawlMode);
//        functionalModeList.add(EditorMode.StickerMode);
        functionalModeList.add(EditorMode.MosaicMode);
        functionalModeList.add(EditorMode.TextPastingMode);
        functionalModeList.add(EditorMode.CropMode);

        FuncModeToolFragment toolFragment = FuncModeToolFragment.newInstance(functionalModeList);
        getSupportFragmentManager().beginTransaction().add(R.id.flFunc, toolFragment).commit();

        mRootEditorDelegate = new RootEditorDelegate(layerPhotoView, layerEditorParent);
        mFuncAndActionBarAnimHelper = new FuncAndActionBarAnimHelper(actionFrameLayout, toolBar, flFunc, this);
        mCropHelper = new CropHelper(layerCropView, new CropDetailsView(layoutCropDetails), this);
        mFuncHelper = new FuncHelper(this, new DragToDeleteView(rltDragDelete));

        toolFragment.addFuncModeListener(mFuncHelper);
        toolFragment.addFuncModeDetailsListener(mFuncHelper);
        toolFragment.addOnRevokeListener(mFuncHelper);

        // restore
        restoreData();
    }

    private void restoreData() {
        String originalImageUrl = mEditorPathSetup.getOriginalImageUrl();
        String editedImageUrl = mEditorPathSetup.getEditedImageUrl();
        HashMap<String, EditorCacheData> editCacheData = null;
        if (originalImageUrl != null) {
            mEditorId = originalImageUrl;
            if (editedImageUrl != null) {
                // key是原图片Url和编辑后图片Url的结合
                mEditorId += editedImageUrl;
            }
            editCacheData = PhotoEditCache.getIntance().getEditCacheDataByImageUrl(mEditorId);
        }
        if ((editCacheData == null || editCacheData.isEmpty()) && editedImageUrl != null) {
            mEditorId = editedImageUrl;
            if (originalImageUrl != null) {
                mEditorId = originalImageUrl + editedImageUrl;
            }
            editCacheData = PhotoEditCache.getIntance().getEditCacheDataByImageUrl(mEditorId);
            //set up layer cache with ep...
            mOriginalImageUrl = editedImageUrl;
        } else {
            //op has extra data or not
            mOriginalImageUrl = originalImageUrl;
        }
        if (!new File(mOriginalImageUrl).exists()) {
            Toast.makeText(this, "文件不存在！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Bitmap imageBitmap = EditorCompressUtils.getImageCompressBitmap(mOriginalImageUrl);

        mCropHelper.restoreLayerEditData(editCacheData);
        Bitmap cropBitmap = mCropHelper.restoreBitmapByCropSaveState(imageBitmap);
        layerPhotoView.setImageBitmap(cropBitmap);

//        layerPhotoView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
//            @Override
//            public void onLayoutChange(View v, int left, int top, int right, int bottom,
//                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                layerMosaicView.setInitializeMatrix(layerPhotoView.getBaseLayoutMatrix());
//            }
//        });

        CropSaveState cropState = mCropHelper.getCropSaveState();
        layerPhotoView.addOnLayoutChangeListener(new LayerImageOnLayoutChangeListener(cropState));

        layerMosaicView.setupForMosaicView(imageBitmap);
        callChildrenRestoreLayer(layerComposite, editCacheData);

        mEditorWidth = imageBitmap.getWidth();
        mEditorHeight = imageBitmap.getHeight();
    }

    private void initActionBarListener() {
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageComposeResult(false);
            }
        });

        tvComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageCompose();
            }
        });
    }

    @Override
    public View findLayerByEditorMode(EditorMode editorMode) {
        switch (editorMode) {
            case ScrawlMode:
                return layerScrawlView;
            case StickerMode:
                return layerStickerView;
            case TextPastingMode:
                return layerTextPastingView;
            case MosaicMode:
                return layerMosaicView;
            case CropMode:
                return layerCropView;
        }
        return null;
    }

    @Override
    public Context getActivityContext() {
        return this;
    }

    @Override
    public FuncAndActionBarAnimHelper getFuncAndActionBarAnimHelper() {
        return mFuncAndActionBarAnimHelper;
    }

    @Override
    public CropHelper getCropHelper() {
        return mCropHelper;
    }

    @Override
    public RootEditorDelegate getRootEditorDelegate() {
        return mRootEditorDelegate;
    }

    @Override
    public LayerComposite getLayerCompositeView() {
        return layerComposite;
    }

    @Override
    public String getSetupEditorId() {
        return mEditorId;
    }

    @Override
    public String getResultEditorId() {
        return mOriginalImageUrl + mEditorPathSetup.getEditor2SavedPath();
    }

    @Override
    public Pair<Integer, Integer> getEditorSizeInfo() {
        return new Pair(mEditorWidth, mEditorHeight);
    }

    @Override
    public Pair<Integer, Integer> getScreenSizeInfo() {
        int screeWidth = getResources().getDisplayMetrics().widthPixels;
        int screeHeight = getResources().getDisplayMetrics().heightPixels;
        return new Pair(screeWidth, screeHeight);
    }

    private void imageCompose() {
        String path = mEditorPathSetup.getEditor2SavedPath();
        File parentFile = new File(path).getParentFile();
        parentFile.mkdirs();
        if (imageComposeTask != null) {
            imageComposeTask.cancel(true);
        }
        imageComposeTask = new ImageComposeTask(this);
        imageComposeTask.execute(path);
    }

    private void onImageComposeResult(boolean editStatus) {
        supportRecycle();
        Intent intent = new Intent();
        EditorResult resultData = new EditorResult(mOriginalImageUrl, mEditorPathSetup.getEditedImageUrl(),
                mEditorPathSetup.getEditor2SavedPath(), editStatus);
        intent.putExtra(String.valueOf(RESULT_OK), resultData);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void supportRecycle() {
        MatrixUtils.recycleBitmap(mRootEditorDelegate.getDisplayBitmap());
    }

    private void callChildrenRestoreLayer(ViewGroup parent, HashMap<String, EditorCacheData> cacheData) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View layer = parent.getChildAt(i);

            if (layer instanceof LayerCacheNode) {
                ((LayerCacheNode) layer).restoreLayerEditData(cacheData);
            } else if (layer instanceof ViewGroup) {
                callChildrenRestoreLayer((ViewGroup) layer, cacheData);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFuncHelper.onActivityResult(requestCode, resultCode, data);
    }

    class LayerImageOnLayoutChangeListener implements View.OnLayoutChangeListener {

        private CropSaveState state;

        public LayerImageOnLayoutChangeListener(CropSaveState state) {
            this.state = state;
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            Matrix matrix = null;
            if (state != null) {
                matrix = state.getOriginalMatrix();
            }
            if (matrix == null) {
                matrix = layerPhotoView.getBaseLayoutMatrix();
            }
            if (state != null) {
                mCropHelper.resetEditorSupportMatrix(state);
            }
            layerMosaicView.setInitializeMatrix(matrix);
            layerPhotoView.removeOnLayoutChangeListener(this);
        }
    }

    /**
     * AsyncTask for image Compose
     */
    class ImageComposeTask extends AsyncTask<String, Void, Boolean> {

        private LayerViewProvider mProvider;
        private ProgressDialog mDialog;
        private String mPath;
        private LayerComposite layerComposite;
        private String mEditorId;

        public ImageComposeTask(LayerViewProvider provider) {
            this.mProvider = provider;
            init();
        }

        private void init() {
            layerComposite = mProvider.getLayerCompositeView();
            mEditorId = mProvider.getResultEditorId();

            mDialog = new ProgressDialog(mProvider.getActivityContext());
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage(mProvider.getActivityContext().getResources().getString(R.string.editor_handle));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {

            // 将编辑效果compose到layerComposite宽高相同大小的bitmap上
            mPath = strings[0];
            RootEditorDelegate delegate = mProvider.getRootEditorDelegate();

            // draw image data layer by layer
            Bitmap rootBitmap = delegate.getDisplayBitmap();
            Bitmap composeBitmap = Bitmap.createBitmap(layerComposite.getWidth(), layerComposite.getHeight(), Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(composeBitmap);
            canvas.drawBitmap(rootBitmap, delegate.getBaseLayoutMatrix(), null);

            drawChildrenLayer(null, layerComposite, canvas);

            RectF rect = delegate.getOriginalRect();
            Bitmap resultBitmap = Bitmap.createBitmap(composeBitmap, (int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height());
            try {
                resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(mPath)));

                MatrixUtils.recycleBitmap(composeBitmap);
                MatrixUtils.recycleBitmap(resultBitmap);
                MatrixUtils.recycleBitmap(rootBitmap);

                // Save cached data.
                HashMap<String, EditorCacheData> cacheData = PhotoEditCache.getIntance().getEditCacheDataByImageUrl(mEditorId);
                saveChildrenLayerData(layerComposite, cacheData);
                mProvider.getCropHelper().saveLayerEditData(cacheData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            // 将编辑效果compose到编辑原图片宽高相同大小的bitmap上
//            mPath = strings[0];
//            RootEditorDelegate delegate = mProvider.getRootEditorDelegate();
//
//            // draw image data layer by layer
//            Bitmap rootBitmap = BitmapFactory.decodeFile(mOriginalImageUrl);
//            float scale = 1.0f / EditorCompressUtils.computeSampleSize(mOriginalImageUrl);
////            Bitmap composeBitmap = Bitmap.createBitmap(layerComposite.getWidth(), layerComposite.getHeight(), Bitmap.Config.RGB_565);
//            Bitmap composeBitmap = Bitmap.createBitmap(rootBitmap.getWidth(), rootBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//
//            Canvas canvas = new Canvas(composeBitmap);
//            canvas.drawBitmap(rootBitmap, 0, 0, null);
//
//            Matrix rootBitmapMatrix = new Matrix();
//            rootBitmapMatrix.postScale(scale, scale);
//            rootBitmapMatrix.postConcat(delegate.getBaseLayoutMatrix());
//
//            Matrix invertMatrix = new Matrix();
//            rootBitmapMatrix.invert(invertMatrix);
//
////            Bitmap rootBitmap = delegate.getDisplayBitmap();
////            canvas.drawBitmap(rootBitmap, delegate.getBaseLayoutMatrix(), null);
//
//            drawChildrenLayer(invertMatrix, layerComposite, canvas);
//
////            RectF rect = delegate.getOriginalRect();
////            Bitmap resultBitmap = Bitmap.createBitmap(composeBitmap, (int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height());
//            try {
//                composeBitmap.compress(Bitmap.CompressFormat.JPEG, 100, new FileOutputStream(new File(mPath)));
//
//                MatrixUtils.recycleBitmap(composeBitmap);
////                MatrixUtils.recycleBitmap(resultBitmap);
//                MatrixUtils.recycleBitmap(rootBitmap);
//
//                // Save cached data.
//                HashMap<String, EditorCacheData> cacheData = PhotoEditCache.getIntance().getEditCacheDataByImageUrl(mEditorId);
//                saveChildrenLayerData(layerComposite, cacheData);
//                mProvider.getCropHelper().saveLayerEditData(cacheData);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }

            return true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDialog.dismiss();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.e(TAG, "ImageComposeTask:保存路径 : " + mPath);
            mDialog.dismiss();
            onImageComposeResult(result);
        }

        private void drawChildrenLayer(Matrix inverMatrix, ViewGroup parent, Canvas canvas) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View layer = parent.getChildAt(i);

                if (layer instanceof BaseLayerView) {
                    LayerEditResult editorResult = ((BaseLayerView) layer).getEditorResult();
                    Matrix supportMatrix = editorResult.getSupportMatrix();
                    Bitmap bitmap = editorResult.getBitmap();
                    if (bitmap != null) {
                        if (inverMatrix != null) {
                            supportMatrix.preConcat(inverMatrix);
                        }

                        canvas.drawBitmap(bitmap, supportMatrix, null);
                    }
                } else if (layer instanceof ViewGroup) {
                    drawChildrenLayer(inverMatrix, (ViewGroup) layer, canvas);
                }
            }
        }

        private void saveChildrenLayerData(ViewGroup parent, HashMap<String, EditorCacheData> cacheData) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View layer = parent.getChildAt(i);

                if (layer instanceof BaseLayerView) {
                    ((BaseLayerView) layer).saveLayerEditData(cacheData);
                } else if (layer instanceof ViewGroup) {
                    saveChildrenLayerData((ViewGroup) layer, cacheData);
                }
            }
        }
    }
}
