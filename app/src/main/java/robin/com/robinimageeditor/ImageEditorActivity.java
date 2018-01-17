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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import robin.com.robinimageeditor.bean.EditorCacheData;
import robin.com.robinimageeditor.bean.EditorResult;
import robin.com.robinimageeditor.bean.EditorSetup;
import robin.com.robinimageeditor.bean.LayerEditResult;
import robin.com.robinimageeditor.bean.Pair;
import robin.com.robinimageeditor.cache.LayerCache;
import robin.com.robinimageeditor.layer.LayerCacheNode;
import robin.com.robinimageeditor.layer.LayerComposite;
import robin.com.robinimageeditor.layer.LayerViewProvider;
import robin.com.robinimageeditor.layer.MosaicView;
import robin.com.robinimageeditor.layer.RootEditorDelegate;
import robin.com.robinimageeditor.layer.ScrawlView;
import robin.com.robinimageeditor.layer.StickerView;
import robin.com.robinimageeditor.layer.TextPastingView;
import robin.com.robinimageeditor.layer.base.BaseLayerView;
import robin.com.robinimageeditor.layer.photoview.PhotoView;
import robin.com.robinimageeditor.util.EditorCompressUtils;
import robin.com.robinimageeditor.util.Utils;
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
    private static final String intentKey = "editorSetup";

    private EditorSetup mEditorSetup;
    private String mEditorId;
    private String mEditorPath;
    private int mEditorWidth;
    private int mEditorHeight;

    private RootEditorDelegate mRootEditorDelegate;
    private FuncAndActionBarAnimHelper mFuncAndActionBarAnimHelper;
    private FuncHelper mFuncHelper;

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

    private TextView tvComplete;
    private ImageView ivBack;

    private RelativeLayout rltDragDelete;

    public static Intent intent(Context context, EditorSetup editorSetup) {
        Intent intent = new Intent(context, ImageEditorActivity.class);
        intent.putExtra(intentKey, editorSetup);
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
            this.getWindow().setStatusBarColor(Utils.getResourceColor(this, R.color.bg_black));
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
        View view = View.inflate(this, R.layout.activity_image_editor, null);
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
        layerEditorParent = findViewById(R.id.layerEditorParent);
        layerComposite = findViewById(R.id.layerComposite);

        tvComplete = findViewById(R.id.tvComplete);
        ivBack = findViewById(R.id.ivBack);

        rltDragDelete = findViewById(R.id.rltDragDelete);
    }

    private void initData() {
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        EditorSetup editorSetup = (EditorSetup) intent.getSerializableExtra(intentKey);
        if (editorSetup == null) {
            Log.e(TAG, "editorSetup == null");
            finish();
            return;
        }
        mEditorSetup = editorSetup;
        String originalPath = mEditorSetup.getOriginalPath();
        String editorPath = mEditorSetup.getEditorPath();
        if (originalPath == null && editorPath == null) {
            Log.e(TAG, "originalPath,editorPath are both null");
            finish();
            return;
        }
    }

    private void initView() {
        List<EditorMode> functionalModeList = new ArrayList<>();
        functionalModeList.add(EditorMode.ScrawlMode);
        functionalModeList.add(EditorMode.StickerMode);
        functionalModeList.add(EditorMode.TextPastingMode);
        functionalModeList.add(EditorMode.MosaicMode);

        FuncModeToolFragment toolFragment = FuncModeToolFragment.newInstance(functionalModeList);
        getSupportFragmentManager().beginTransaction().add(R.id.flFunc, toolFragment).commit();

        mRootEditorDelegate = new RootEditorDelegate(layerPhotoView, layerEditorParent);
        mFuncAndActionBarAnimHelper = new FuncAndActionBarAnimHelper(actionFrameLayout, toolBar, flFunc, this);
        mFuncHelper = new FuncHelper(this, new DragToDeleteView(rltDragDelete));

        toolFragment.addFuncModeListener(mFuncHelper);
        toolFragment.addFuncModeDetailsListener(mFuncHelper);
        toolFragment.addOnRevokeListener(mFuncHelper);

        // restore
        restoreData();
    }

    private void restoreData() {
        String originalPath = mEditorSetup.getOriginalPath();
        String editorPath = mEditorSetup.getEditorPath();
        HashMap<String, EditorCacheData> cacheData = null;
        if (originalPath != null) {
            mEditorId = originalPath;
            if (editorPath != null) {
                mEditorId += editorPath;
            }
            cacheData = LayerCache.getIntance().getCacheDataById(mEditorId);
        }
        if ((cacheData == null || cacheData.isEmpty()) && editorPath != null) {
            mEditorId = editorPath;
            if (originalPath != null) {
                mEditorId = originalPath + editorPath;
            }
            cacheData = LayerCache.getIntance().getCacheDataById(mEditorId);
            //set up layer cache with ep...
            mEditorPath = editorPath;
        } else {
            //op has extra data or not
            mEditorPath = originalPath;
        }
        if (!new File(mEditorPath).exists()) {
            Toast.makeText(this, "文件不存在！", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Bitmap imageBitmap = EditorCompressUtils.getImageBitmap(mEditorPath);
        layerPhotoView.setImageBitmap(imageBitmap);

        layerPhotoView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                layerMosaicView.setInitializeMatrix(layerPhotoView.getBaseLayoutMatrix());
            }
        });

        layerMosaicView.setupForMosaicView(imageBitmap);
        callChildrenRestoreLayer(layerComposite, cacheData);

        mEditorWidth = imageBitmap.getWidth();
        mEditorHeight = imageBitmap.getHeight();
    }

    private void initActionBarListener() {
        ivBack.setOnClickListener(new View.OnClickListener() {
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
        return mEditorPath + mEditorSetup.getEditor2SavedPath();
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
        String path = mEditorSetup.getEditor2SavedPath();
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
        EditorResult resultData = new EditorResult(mEditorPath, mEditorSetup.getEditorPath(),
                mEditorSetup.getEditor2SavedPath(), editStatus);
        intent.putExtra(String.valueOf(RESULT_OK), resultData);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void supportRecycle() {
        Utils.recycleBitmap(mRootEditorDelegate.getDisplayBitmap());
    }

    private void callChildrenRestoreLayer(ViewGroup parent, HashMap<String, EditorCacheData> cacheData) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View layer = parent.getChildAt(i);

            if (layer instanceof LayerCacheNode) {
                ((LayerCacheNode) layer).restoreLayerData(cacheData);
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
            mPath = strings[0];
            RootEditorDelegate delegate = mProvider.getRootEditorDelegate();

            // draw image data layer by layer
            Bitmap rootBit = delegate.getDisplayBitmap();
            Bitmap compose = Bitmap.createBitmap(layerComposite.getWidth(), layerComposite.getHeight(), Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(compose);
            canvas.drawBitmap(rootBit, delegate.getBaseLayoutMatrix(), null);

            drawChildrenLayer(layerComposite, canvas);

            RectF rect = delegate.getOriginalRect();
            Bitmap result = Bitmap.createBitmap(compose, (int) rect.left, (int) rect.top, (int) rect.width(), (int) rect.height());
            try {
                result.compress(Bitmap.CompressFormat.JPEG, 85, new FileOutputStream(new File(mPath)));

                Utils.recycleBitmap(compose);
                Utils.recycleBitmap(result);
                Utils.recycleBitmap(rootBit);

                // Save cached data.
                HashMap<String, EditorCacheData> cacheData = LayerCache.getIntance().getCacheDataById(mEditorId);
                saveChildrenLayerData(layerComposite, cacheData);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
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

        private void drawChildrenLayer(ViewGroup parent, Canvas canvas) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View layer = parent.getChildAt(i);

                if (layer instanceof BaseLayerView) {
                    LayerEditResult editorResult = ((BaseLayerView) layer).getEditorResult();
                    Matrix supportMatrix = editorResult.getSupportMatrix();
                    Bitmap bitmap = editorResult.getBitmap();
                    if (bitmap != null) {
                        Matrix matrix = new Matrix();
                        matrix.set(supportMatrix);
                        canvas.drawBitmap(bitmap, matrix, null);
                    }
                } else if (layer instanceof ViewGroup) {
                    drawChildrenLayer((ViewGroup) layer, canvas);
                }
            }
        }

        private void saveChildrenLayerData(ViewGroup parent, HashMap<String, EditorCacheData> cacheData) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                View layer = parent.getChildAt(i);

                if (layer instanceof BaseLayerView) {
                    ((BaseLayerView) layer).saveLayerData(cacheData);
                } else if (layer instanceof ViewGroup) {
                    saveChildrenLayerData((ViewGroup) layer, cacheData);
                }
            }
        }
    }
}
