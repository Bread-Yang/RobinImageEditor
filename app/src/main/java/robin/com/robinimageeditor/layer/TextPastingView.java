package robin.com.robinimageeditor.layer;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import robin.com.robinimageeditor.bean.TextPastingSaveState;
import robin.com.robinimageeditor.layer.base.BasePastingLayerView;

/**
 * Created by Robin Yang on 1/9/18.
 */

public class TextPastingView extends BasePastingLayerView<TextPastingSaveState> {

    public TextPastingView(Context context) {
        super(context);
    }

    public TextPastingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TextPastingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TextPastingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onPastingDoubleClick(TextPastingSaveState state) {

    }

    @Override
    protected void drawPastingState(TextPastingSaveState state, Canvas canvas) {

    }
}
