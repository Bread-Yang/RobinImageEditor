package robin.com.robinimageeditor.funcdetail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import robin.com.robinimageeditor.data.share.InputStickerSharableData;
import robin.com.robinimageeditor.data.share.InputTextSharableData;
import robin.com.robinimageeditor.data.share.SharableData;
import robin.com.robinimageeditor.editmode.EditorMode;
import robin.com.robinimageeditor.editmode.FuncModeListener;
import robin.com.robinimageeditor.layer.base.BaseLayerView;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.layer.base.LayerViewProvider;
import robin.com.robinimageeditor.layer.crop.CropHelper;
import robin.com.robinimageeditor.layer.mosaic.MosaicView;
import robin.com.robinimageeditor.layer.scrawl.ScrawlView;
import robin.com.robinimageeditor.layer.sticker.StickerDetailsView;
import robin.com.robinimageeditor.layer.sticker.StickerView;
import robin.com.robinimageeditor.layer.textpasting.TextPastingView;
import robin.com.robinimageeditor.view.DragToDeleteView;
import robin.com.robinimageeditor.view.FuncAndActionBarAnimHelper;
import robin.com.robinimageeditor.view.PictureTextEditDialog;

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
    private PictureTextEditDialog mInputTextDialog;
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
                    stickerView.setDragToDeleteViewRect(rectF);
                }

                if (textPastingView != null) {
                    textPastingView.setDragToDeleteViewRect(rectF);
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
            ((MosaicView) mProvider.findLayerByEditorMode(EditorMode.MosaicMode)).setPaintStrokeWidth(details.getPaintStrokekWidth());
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
                public void onResult(InputStickerSharableData stickerData) {
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

    private void go2InputView(InputTextSharableData prepareData) {
        mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(false);
//        Intent intent = EditorTextInputActivity.intent(mContext, prepareData);
//        ((Activity) mContext).startActivityForResult(intent, TEXT_INPUT_RESULT_CODE);
//        ((Activity) mContext).overridePendingTransition(R.anim.animation_bottom_to_top, 0);
        if (mInputTextDialog == null) {
            mInputTextDialog = new PictureTextEditDialog(mContext, prepareData, new PictureTextEditDialog.OnClickSaveListener() {
                @Override
                public void onSave(InputTextSharableData inputTextSharableData) {
                    resultFromInputDialog(inputTextSharableData);
                }
            });
        } else {
            mInputTextDialog.setInputTextSharableData(prepareData);
        }
        mInputTextDialog.show();
    }

    private void resultFromInputDialog(InputTextSharableData inputTextSharableData) {
        if (inputTextSharableData != null) {
            ((TextPastingView) mProvider.findLayerByEditorMode(EditorMode.TextPastingMode)).onTextPastingChanged(inputTextSharableData);
        }
        mFuncAndActionBarAnimHelper.showOrHideFuncAndBarView(true);
    }

    private void resultFromInputView(int resultCode, Intent data) {
        if (data != null) {
            InputTextSharableData result = (InputTextSharableData) data.getSerializableExtra(String.valueOf(resultCode));
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
                    go2InputView((InputTextSharableData) sharableData);
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
