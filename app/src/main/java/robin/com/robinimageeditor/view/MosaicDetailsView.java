package robin.com.robinimageeditor.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import robin.com.robinimageeditor.R;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicDetailsView extends FrameLayout {

    private static int sCheckedId = -1;

    public interface OnMosaicChangeListener {
        void onModeChange(MosaicMode mosaicMode);
        void onStrokeWidthChange(int strokeWidth);
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
        LayoutInflater.from(context).inflate(R.layout.mosaic_func_details_view, this, true);
//        final LinearLayout rootFunc = findViewById(R.id.llMosaicDetails);
//        MosaicMode[] values = MosaicMode.values();
//        for (int index = 0; index < values.length; index++) {
//            final MosaicMode mode = values[index];
//            if (mode.getModeBgResource() <= 0) {
//                continue;
//            }
//            final View item = LayoutInflater.from(context).inflate(R.layout.mosaic_func_details_item, rootFunc, false);
//            ImageView ivFuncDesc = item.findViewById(R.id.ivMosaicDesc);
//            ivFuncDesc.setImageResource(mode.getModeBgResource());
//            item.setTag(mode);
//            rootFunc.addView(item);
//            final int currentIndex = index;
//            item.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    onMosaicClick(mode, currentIndex, item, rootFunc);
//                }
//            });
//            if (index == 0) {
//                item.setSelected(true);
//                onMosaicClick(mode, 0, item, rootFunc);
//            }
//        }
        // MosaicMode默认是Grid
        onMosaicClick(MosaicMode.Grid, -1, null, null);

        final PictureStrokeGroup psgStrokes = findViewById(R.id.psgStrokes);
        psgStrokes.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sCheckedId = group.getCheckedRadioButtonId();
//                if (onColorChangedListener != null) {
//                    onColorChangedListener.onColorChanged(colorGroup.getCheckColor());
//                }
                if (onMosaicChangeListener != null) {
                    onMosaicChangeListener.onStrokeWidthChange(psgStrokes.getCheckStroke());
                }
            }
        });

        if (sCheckedId != -1) {
            RadioButton rb = psgStrokes.findViewById(sCheckedId);
            rb.setChecked(true);

            if (onMosaicChangeListener != null) {
                psgStrokes.post(new Runnable() {
                    @Override
                    public void run() {
                        onMosaicChangeListener.onStrokeWidthChange(psgStrokes.getCheckStroke());
                    }
                });
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
//        MatrixUtils.changeSelectedStatus(rootView, position);
        if (onMosaicChangeListener != null) {
            onMosaicChangeListener.onModeChange(mosaicMode);
        }
    }

    public void setOnRevokeListener(OnRevokeListener onRevokeListener) {
        this.onRevokeListener = onRevokeListener;
    }
}
