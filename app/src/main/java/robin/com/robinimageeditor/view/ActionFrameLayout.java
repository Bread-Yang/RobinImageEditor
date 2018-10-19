package robin.com.robinimageeditor.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import robin.com.robinimageeditor.layer.base.BasePastingLayerView;
import robin.com.robinimageeditor.layer.photoview.PhotoView;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class ActionFrameLayout extends FrameLayout {

    public static boolean sIsMultipleTouching = false;

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
        int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (action == MotionEvent.ACTION_UP) {
            if (actionListener != null) {
                actionListener.actionUp();
            }
            sIsMultipleTouching = false;
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (actionListener != null) {
                actionListener.actionMove();
            }
        }

        // BasePastingLayerView正在操作的时候不拦截事件
        if (ev.getPointerCount() > 1 && !BasePastingLayerView.sIsPastingLayerTouching) {
            View firstChild = getChildAt(0);
            if (firstChild instanceof PhotoView) {

                sIsMultipleTouching = true;
                return firstChild.dispatchTouchEvent(ev);
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
