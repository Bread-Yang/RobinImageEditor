package robin.com.robinimageeditor.view;

import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.view.View;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * Created by Robin Yang on 1/9/18.
 */

public class DragToDeleteView {

    public interface OnLayoutRectChangeListener {
        void onChange(View view, RectF rectF);
    }

    private OnLayoutRectChangeListener onLayoutRectChangeListener;

    private View mRootView;

    public DragToDeleteView(View view) {
        mRootView = view;
        mRootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                RectF rectF = new RectF();
                MatrixUtils.RectFSetInt(rectF, left, top, right, bottom);
                if (onLayoutRectChangeListener != null) {
                    onLayoutRectChangeListener.onChange(mRootView, rectF);
                }
            }
        });
    }

    public void showOrHide(boolean show) {
        mRootView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setDrag2DeleteText(boolean focus) {
        int color = focus ? R.color.bg_alpha_33 : R.color.delete_focus;

        mRootView.setBackgroundColor(ContextCompat.getColor(mRootView.getContext(), color));
    }

    public void setOnLayoutRectChangeListener(OnLayoutRectChangeListener onLayoutRectChangeListener) {
        this.onLayoutRectChangeListener = onLayoutRectChangeListener;
    }
}