package robin.com.robinimageeditor.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.util.Utils;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicDetailsView extends FrameLayout {

    public interface OnMosaicChangeListener {
        void onChange(MosaicMode mosaicMode);
    }

    private OnMosaicChangeListener onMosaicChangeListener;
    private OnRevokeListener onRevokeListener;

    public MosaicDetailsView(@NonNull Context context) {
        this(context, null);
    }

    public MosaicDetailsView(@NonNull Context context, OnMosaicChangeListener onMosaicChangeListener) {
        super(context);
        this.onMosaicChangeListener = onMosaicChangeListener;
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.mosaic_func_details, this, true);
        final LinearLayout rootFunc = findViewById(R.id.llMosaicDetails);
        MosaicMode[] values = MosaicMode.values();
        for (int index = 0; index < values.length; index++) {
            final MosaicMode mode = values[index];
            if (mode.getModeBgResource() <= 0) {
                continue;
            }
            final View item = LayoutInflater.from(context).inflate(R.layout.item_mosaic_func_details, rootFunc, false);
            ImageView ivFuncDesc = item.findViewById(R.id.ivMosaicDesc);
            ivFuncDesc.setImageResource(mode.getModeBgResource());
            item.setTag(mode);
            rootFunc.addView(item);
            final int currentIndex = index;
            item.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onMosaicClick(mode, currentIndex, item, rootFunc);
                }
            });
            if (index == 0) {
                item.setSelected(true);
                onMosaicClick(mode, 0, item, rootFunc);
            }
        }
        findViewById(R.id.ivRevoke).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRevokeListener != null) {
                    onRevokeListener.revoke(EditorMode.MosaicMode);
                }
            }
        });
    }

    private void onMosaicClick(MosaicMode mosaicMode, int position, View clickView, ViewGroup rootView) {
        Utils.changeSelectedStatus(rootView, position);
        if (onMosaicChangeListener != null) {
            onMosaicChangeListener.onChange(mosaicMode);
        }
    }

    public void setOnRevokeListener(OnRevokeListener onRevokeListener) {
        this.onRevokeListener = onRevokeListener;
    }
}
