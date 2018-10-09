package robin.com.robinimageeditor.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import robin.com.robinimageeditor.R;

/**
 * Created by Robin Yang on 1/3/18.
 */

public class ScrawlDetailsView extends FrameLayout {

    private static int sCheckedId = -1;

    private OnRevokeListener onRevokeListener;
    private OnColorChangedListener onColorChangedListener;

    private PictureColorGroup colorGroup;

    interface OnColorChangedListener {
        void onColorChanged(int checkedColor);
    }

    public ScrawlDetailsView(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.scrawl_func_details_view, this, true);

        findViewById(R.id.ivRevoke).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRevokeListener != null) {
                    onRevokeListener.revoke(EditorMode.ScrawlMode);
                }
            }
        });

        colorGroup = findViewById(R.id.pcgColors);
        colorGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                sCheckedId = group.getCheckedRadioButtonId();
                if (onColorChangedListener != null) {
                    onColorChangedListener.onColorChanged(colorGroup.getCheckColor());
                }
            }
        });

        if (sCheckedId != -1) {
            RadioButton rb = colorGroup.findViewById(sCheckedId);
            rb.setChecked(true);
        }

//        ColorSeekBar ckb = findViewById(R.id.colorBarScrawl);
//        ckb.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
//            @Override
//            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
//                if (onColorChangeListener != null) {
//                    onColorChangeListener.onColorChangeListener(colorBarPosition, alphaBarPosition, color);
//                }
//            }
//        });
    }

    public void setOnRevokeListener(OnRevokeListener onRevokeListener) {
        this.onRevokeListener = onRevokeListener;
    }

    public void setOnColorChangedListener(OnColorChangedListener onColorChangedListener) {
        this.onColorChangedListener = onColorChangedListener;

        if (onColorChangedListener != null) {
            onColorChangedListener.onColorChanged(colorGroup.getCheckColor());
        }
    }
}
