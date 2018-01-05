package robin.com.robinimageeditor.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import robin.com.robinimageeditor.R;

/**
 * Created by Robin Yang on 1/3/18.
 */

public class ScrawlDetailsView extends FrameLayout {

    private OnRevokeListener onRevokeListener;
    private ColorSeekBar.OnColorChangeListener onColorChangeListener;

    public ScrawlDetailsView(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.scralw_func_details, this, true);

        findViewById(R.id.ivRevoke).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onRevokeListener != null) {
                    onRevokeListener.revoke(EditorMode.ScrawlMode);
                }
            }
        });

        ColorSeekBar ckb = findViewById(R.id.colorBarScrawl);
        ckb.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int colorBarPosition, int alphaBarPosition, int color) {
                if (onColorChangeListener != null) {
                    onColorChangeListener.onColorChangeListener(colorBarPosition, alphaBarPosition, color);
                }
            }
        });
    }

    public void setOnRevokeListener(OnRevokeListener onRevokeListener) {
        this.onRevokeListener = onRevokeListener;
    }

    public void setOnColorChangeListener(ColorSeekBar.OnColorChangeListener onColorChangeListener) {
        this.onColorChangeListener = onColorChangeListener;
    }
}
