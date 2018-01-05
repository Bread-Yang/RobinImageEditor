package robin.com.robinimageeditor.layer;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class LayerComposite extends FrameLayout {

    private boolean handleEvent = true;

    public LayerComposite(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public LayerComposite(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LayerComposite(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LayerComposite(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (handleEvent) {
            return super.dispatchTouchEvent(ev);
        } else {
            return false;
        }
    }

    public void setHandleEvent(boolean handleEvent) {
        this.handleEvent = handleEvent;
    }
}
