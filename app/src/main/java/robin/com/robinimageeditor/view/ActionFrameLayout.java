package robin.com.robinimageeditor.view;

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

public class ActionFrameLayout extends FrameLayout {

    interface ActionListener {
        void actionUp();
        void actionMove();
    }

    ActionListener actionListener;

    public ActionFrameLayout(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ActionFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ActionFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ActionFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            if (actionListener != null) {
                actionListener.actionUp();
            }
        } else if ( ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (actionListener != null) {
                actionListener.actionMove();
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
