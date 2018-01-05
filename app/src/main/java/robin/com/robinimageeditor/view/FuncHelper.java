package robin.com.robinimageeditor.view;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import robin.com.robinimageeditor.LayerViewProvider;
import robin.com.robinimageeditor.bean.FuncDetailsMarker;
import robin.com.robinimageeditor.bean.InputStickerData;
import robin.com.robinimageeditor.bean.ScrawlDetails;
import robin.com.robinimageeditor.layer.BaseLayerView;
import robin.com.robinimageeditor.layer.ScrawlView;
import robin.com.robinimageeditor.layer.StickerView;

/**
 * Created by Robin Yang on 1/3/18.
 */

public class FuncHelper implements FuncModeListener, FuncDetailsListener, OnRevokeListener {

    private LayerViewProvider mProvider;
    private Context mContext;
    private FuncAndActionBarAnimHelper mFuncAndActionBarAnimHelper;
    private StickerDetailsView mStickerDetailsView;
    private boolean mStickerDetailsShowing;

    public FuncHelper(LayerViewProvider mProvider) {
        this.mProvider = mProvider;
        init();
    }

    private void init() {
        mContext = mProvider.getActivityContext();
        mFuncAndActionBarAnimHelper = mProvider.getFuncAndActionBarAnimHelper();
    }

    private void enableOrDisableEditorMode(EditorMode editorMode, boolean enable) {
        View view = mProvider.findLayerByEditorMode(editorMode);
        if (view instanceof BaseLayerView) {
            ((BaseLayerView) view).setLayerInEditMode(enable);
        }
    }

    private void setScrawlDetails(ScrawlDetails details) {
        ((ScrawlView) mProvider.findLayerByEditorMode(EditorMode.ScrawlMode)).setPaintColor(details.getColor());
    }

    @Override
    public void onFuncModeSelected(EditorMode editorMode) {
        switch (editorMode) {
            case ScrawlMode:
                enableOrDisableEditorMode(editorMode, true);
                break;
            case StickerMode:
                go2StickerPanel();
                break;
        }
    }

    @Override
    public void onFuncModeUnselected(EditorMode editorMode) {
        switch (editorMode) {
            case ScrawlMode:
                enableOrDisableEditorMode(editorMode, false);
                break;
        }
    }

    @Override
    public void onReceiveDetails(EditorMode editorMode, FuncDetailsMarker funcdetailsMarker) {
        switch (editorMode) {
            case ScrawlMode:
                setScrawlDetails((ScrawlDetails) funcdetailsMarker);
                break;
        }
    }

    @Override
    public void revoke(EditorMode editorMode) {
        View view = mProvider.findLayerByEditorMode(editorMode);
        if (view instanceof BaseLayerView) {
            ((BaseLayerView) view).revoke();
        }
    }

    private void go2StickerPanel() {
//        mFuncAndActionBarAnimHelper.
        if (mStickerDetailsView == null) {
            mStickerDetailsView = new StickerDetailsView(mContext);
            mStickerDetailsView.setOnStickerClickListener(new StickerDetailsView.OnStickerClickResult() {
                @Override
                public void onResult(InputStickerData stickerData) {
                    ((StickerView) mProvider.findLayerByEditorMode(EditorMode.StickerMode))
                            .onStickerPastingChanged(stickerData);
                    closeStickerPanel();
                }
            });
//            mFuncAndActionBarAnimHelper.addFunBarAnimateListener(new FuncAndActionBarAnimHelper.OnFunBarAnimationListener() {
//                @Override
//                 public void onFunBarAnimate(boolean show) {
//                    if (show && mStickerDetailsShowing) {
//                        hideStickerPanel();
//                    }
//                }
//            });
        }
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        ((ViewGroup) ((Activity) mContext).getWindow().getDecorView()).addView(mStickerDetailsView, layoutParams);
        mStickerDetailsShowing = true;
    }

    private void closeStickerPanel() {
//        mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(true);
        hideStickerPanel();
    }

    private void hideStickerPanel() {
        if (mStickerDetailsView != null) {
            ((ViewGroup) ((Activity) mContext).getWindow().getDecorView()).removeView(mStickerDetailsView);
            mStickerDetailsShowing = false;
        }
    }
}
