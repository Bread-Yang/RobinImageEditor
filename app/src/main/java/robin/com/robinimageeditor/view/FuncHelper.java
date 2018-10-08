package robin.com.robinimageeditor.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import robin.com.robinimageeditor.EditorTextInputActivity;
import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.bean.FuncDetailsMarker;
import robin.com.robinimageeditor.bean.InputStickerData;
import robin.com.robinimageeditor.bean.InputTextData;
import robin.com.robinimageeditor.bean.MosaicDetails;
import robin.com.robinimageeditor.bean.ScrawlDetails;
import robin.com.robinimageeditor.data.share.SharableData;
import robin.com.robinimageeditor.layer.CropHelper;
import robin.com.robinimageeditor.layer.LayerViewProvider;
import robin.com.robinimageeditor.layer.MosaicView;
import robin.com.robinimageeditor.layer.ScrawlView;
import robin.com.robinimageeditor.layer.StickerView;
import robin.com.robinimageeditor.layer.TextPastingView;
import robin.com.robinimageeditor.layer.base.BaseLayerView;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;

/**
 * Created by Robin Yang on 1/3/18.
 */

public class FuncHelper implements FuncModeListener, FuncDetailsListener, OnRevokeListener {

    private final int TEXT_INPUT_RESULT_CODE = 301;

    private LayerViewProvider mProvider;
    private DragToDeleteView mDragToDeleteView;
    private Context mContext;
    private FuncAndActionBarAnimHelper mFuncAndActionBarAnimHelper;
    private CropHelper mCropHelper;
    private StickerDetailsView mStickerDetailsView;
    private boolean mStickerDetailsShowing;

    public FuncHelper(LayerViewProvider provider, DragToDeleteView dragToDeleteView) {
        this.mProvider = provider;
        this.mDragToDeleteView = dragToDeleteView;
        init();
    }

    private void init() {
        mContext = mProvider.getActivityContext();
        mFuncAndActionBarAnimHelper = mProvider.getFuncAndActionBarAnimHelper();
        mCropHelper = mProvider.getCropHelper();

        final TextPastingView textPastingView = ((TextPastingView) mProvider.findLayerByEditorMode(EditorMode.TextPastingMode));
        if (textPastingView != null) {
            setUpPastingView(textPastingView);

        }

        final StickerView stickerView = ((StickerView) mProvider.findLayerByEditorMode(EditorMode.StickerMode));
        if (stickerView != null) {
            setUpPastingView(stickerView);
        }

        mDragToDeleteView.setOnLayoutRectChangeListener(new DragToDeleteView.OnLayoutRectChangeListener() {
            @Override
            public void onChange(View view, RectF rectF) {
                if (stickerView != null) {
                    stickerView.setDragViewRect(rectF);
                }

                if (textPastingView != null) {
                    textPastingView.setDragViewRect(rectF);
                }
            }
        });
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

    private void setMosaicDetails(MosaicDetails details) {
        ((MosaicView) mProvider.findLayerByEditorMode(EditorMode.MosaicMode)).setMosaicMode(details.getMosaicMode(), null);
        if (details.getPaintStrokekWidth() > 0) {
            ((MosaicView) mProvider.findLayerByEditorMode(EditorMode.MosaicMode)).setPaintStrokWidth(details.getPaintStrokekWidth());
        }
    }

    private void showOrHideDrag2Delete(boolean show) {
        mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(!show);
        mDragToDeleteView.showOrHide(show);
    }

    @Override
    public void onFuncModeSelected(EditorMode editorMode) {
        switch (editorMode) {
            case ScrawlMode:
                enableOrDisableEditorMode(EditorMode.ScrawlMode, true);
                enableOrDisableEditorMode(EditorMode.MosaicMode, false);
                break;
            case StickerMode:
                go2StickerPanel();
                break;
            case TextPastingMode:
                go2InputView(null);
                break;
            case MosaicMode:
                enableOrDisableEditorMode(EditorMode.ScrawlMode, false);
                enableOrDisableEditorMode(EditorMode.MosaicMode, true);
                break;
            case CropMode:
                mCropHelper.showCropDetails();
                break;
        }
    }

    @Override
    public void onFuncModeUnselected(EditorMode editorMode) {
        switch (editorMode) {
            case ScrawlMode:
                enableOrDisableEditorMode(EditorMode.ScrawlMode, false);
                break;
            case MosaicMode:
                enableOrDisableEditorMode(EditorMode.MosaicMode, false);
                break;
        }
    }

    @Override
    public void onReceiveDetails(EditorMode editorMode, FuncDetailsMarker funcdetailsMarker) {
        switch (editorMode) {
            case ScrawlMode:
                setScrawlDetails((ScrawlDetails) funcdetailsMarker);
                break;
            case MosaicMode:
                setMosaicDetails((MosaicDetails) funcdetailsMarker);
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

    private void go2InputView(InputTextData prepareData) {
        mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(false);
        Intent intent = EditorTextInputActivity.intent(mContext, prepareData);
        ((Activity) mContext).startActivityForResult(intent, TEXT_INPUT_RESULT_CODE);
        ((Activity) mContext).overridePendingTransition(R.anim.animation_bottom_to_top, 0);
    }

    private void resultFromInputView(int resultCode, Intent data) {
        if (data != null) {
            InputTextData result = (InputTextData) data.getSerializableExtra(String.valueOf(resultCode));
            if (result != null) {
                ((TextPastingView) mProvider.findLayerByEditorMode(EditorMode.TextPastingMode)).onTextPastingChanged(result);
            }
            mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(true);
        }
    }

    private void setUpPastingView(final BasePastingLayerView layerView) {
        layerView.setCallback(new BasePastingLayerView.OnOperateCallback() {
            @Override
            public void showOrHideDragCallback(boolean b) {
                showOrHideDrag2Delete(b);
            }

            @Override
            public void setOrNotDragCallback(boolean b) {
                mDragToDeleteView.setDrag2DeleteText(b);
            }

            @Override
            public void onLayerViewDoubleClick(View view, SharableData sharableData) {
                if (layerView instanceof TextPastingView) {
                    go2InputView((InputTextData) sharableData);
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TEXT_INPUT_RESULT_CODE) {
            resultFromInputView(resultCode, data);
        }
    }
}
