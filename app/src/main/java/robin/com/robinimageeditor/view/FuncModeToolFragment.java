package robin.com.robinimageeditor.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.bean.MosaicDetails;
import robin.com.robinimageeditor.bean.ScrawlDetails;
import robin.com.robinimageeditor.util.MatrixUtils;

/**
 * Created by Robin Yang on 1/3/18.
 */

public class FuncModeToolFragment extends Fragment implements EditorModeHandler {

    private static final String KEY_MODE = "mode";

    private LinearLayout mFuncModePanel;
    private FrameLayout mFuncDetailPanel;

    private EditorMode mSelectedMode;

    private ArrayList<FuncModeListener> mFuncModeListeners = new ArrayList<>();
    private ArrayList<FuncDetailsListener> mFuncDetailsListeners = new ArrayList<>();
    private ArrayList<OnRevokeListener> mOnRevokeListeners = new ArrayList<>();

    public static FuncModeToolFragment newInstance(List<EditorMode> mode) {
        FuncModeToolFragment result = new FuncModeToolFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_MODE, (Serializable) mode);
        result.setArguments(bundle);
        return result;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.func_mode_fragment, container, false);
        mFuncModePanel = root.findViewById(R.id.llFuncMode);
        mFuncDetailPanel = root.findViewById(R.id.flFuncDetails);
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        List<EditorMode> modeList = (List<EditorMode>) getArguments().getSerializable(KEY_MODE);
        for (int i = 0; i < modeList.size(); i++) {
            final int index = i;
            final EditorMode mode = modeList.get(i);
            if (mode.getModeBgResource() <= 0) {
                continue;
            }
            final View item = LayoutInflater.from(getContext()).inflate(R.layout.func_mode_item, mFuncModePanel , false);
            ImageView ivFuncDesc = item.findViewById(R.id.ivFuncDesc);
            ivFuncDesc.setImageResource(mode.getModeBgResource());
            item.setTag(mode);
            mFuncModePanel.addView(item);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onFuncModeClick(mode, index, item);
                }
            });
        }
    }

    private void onFuncModeClick(EditorMode editorMode, int position, View clickView) {
        if (mSelectedMode == editorMode) {
            editorMode.onHandle(false, this);
            MatrixUtils.changeSelectedStatus(mFuncModePanel, -1);
            for (int i = 0; i < mFuncModeListeners.size(); i++) {
                mFuncModeListeners.get(i).onFuncModeUnselected(editorMode);
            }
            mSelectedMode = null;
        } else {
            editorMode.onHandle(true, this);
            if (editorMode.canPersistMode()) {
                MatrixUtils.changeSelectedStatus(mFuncModePanel, position);
                mSelectedMode = editorMode;
            }
            for (int i = 0; i < mFuncModeListeners.size(); i++) {
                mFuncModeListeners.get(i).onFuncModeSelected(editorMode);
            }
        }
    }

    public void addFuncModeListener(FuncModeListener funcModeListener) {
        mFuncModeListeners.add(funcModeListener);
    }

    public void addFuncModeDetailsListener(FuncDetailsListener funcDetailsListener) {
        mFuncDetailsListeners.add(funcDetailsListener);
    }

    public void addOnRevokeListener(OnRevokeListener onRevokeListener) {
        mOnRevokeListeners.add(onRevokeListener);
    }

    private void showOrHideDetails(boolean show) {
        mFuncDetailPanel.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void handleScrawlMode(boolean selected) {
        if (selected) {
            ScrawlDetailsView scrawlDetailsView = new ScrawlDetailsView(getContext());
            scrawlDetailsView.setOnColorChangedListener(new ScrawlDetailsView.OnColorChangedListener() {
                @Override
                public void onColorChanged(int checkedColor) {
                    for (int i = 0; i < mFuncDetailsListeners.size(); i++) {
                        FuncDetailsListener funcDetailsListener = mFuncDetailsListeners.get(i);
                        funcDetailsListener.onReceiveDetails(EditorMode.ScrawlMode, new ScrawlDetails(checkedColor));
                    }
                }
            });
//            scrawlDetailsView.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
//                @Override
//                public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
//                    for (int i = 0; i < mFuncDetailsListeners.size(); i++) {
//                        FuncDetailsListener funcDetailsListener = mFuncDetailsListeners.get(i);
//                        funcDetailsListener.onReceiveDetails(EditorMode.ScrawlMode, new ScrawlDetails(color));
//                    }
//                }
//            });

            scrawlDetailsView.setOnRevokeListener(new OnRevokeListener() {
                @Override
                public void revoke(EditorMode editorMode) {
                    for (int i = 0; i < mOnRevokeListeners.size(); i++) {
                        OnRevokeListener onRevokeListener = mOnRevokeListeners.get(i);
                        onRevokeListener.revoke(EditorMode.ScrawlMode);
                    }
                }
            });
            showOrHideDetailsView(EditorMode.ScrawlMode, scrawlDetailsView);
        }
        showOrHideDetails(selected);
    }

    @Override
    public void handleStickerMode(boolean selected) {

    }

    @Override
    public void handleMosaicMode(boolean selected) {
        if (selected) {
            MosaicDetailsView.OnMosaicChangeListener  listener = new MosaicDetailsView.OnMosaicChangeListener() {
                @Override
                public void onChange(MosaicMode mosaicMode) {
                    for (int i = 0; i < mFuncDetailsListeners.size(); i++) {
                        FuncDetailsListener funcDetailsListener = mFuncDetailsListeners.get(i);
                        funcDetailsListener.onReceiveDetails(EditorMode.MosaicMode, new MosaicDetails(mosaicMode));
                    }
                }
            };
            MosaicDetailsView mosaicDetails = new MosaicDetailsView(getContext(), listener);
            mosaicDetails.setOnRevokeListener(new OnRevokeListener() {
                @Override
                public void revoke(EditorMode editorMode) {
                    for (int i = 0; i < mOnRevokeListeners.size(); i++) {
                        OnRevokeListener onRevokeListener = mOnRevokeListeners.get(i);
                        onRevokeListener.revoke(EditorMode.MosaicMode);
                    }
                }
            });
            showOrHideDetailsView(EditorMode.MosaicMode, mosaicDetails);
        }
        showOrHideDetails(selected);
    }

    @Override
    public void handleTextPastingMode(boolean selected) {

    }

    @Override
    public void handleCropMode(boolean selected) {

    }

    private void showOrHideDetailsView(EditorMode editorMode, View view) {
        int count = mFuncDetailPanel.getChildCount();
        View toRemoveView = null;
        boolean handled = false;
        if (count > 0) {
            View topView = mFuncDetailPanel.getChildAt(count - 1);
            EditorMode tag = (EditorMode) topView.getTag();
            if (tag != editorMode) {
                toRemoveView = topView;
            } else {
                handled = true;
            }
        }
        if (!handled) {
            mFuncDetailPanel.addView(view);
            if (toRemoveView != null) {
                mFuncDetailPanel.removeView(toRemoveView);
            }
        }
    }
}
