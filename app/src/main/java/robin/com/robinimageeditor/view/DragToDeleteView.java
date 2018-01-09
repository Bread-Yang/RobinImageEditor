package robin.com.robinimageeditor.view;

import android.graphics.RectF;
import android.view.View;
import android.widget.TextView;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.util.Utils;

/**
 * Created by Robin Yang on 1/9/18.
 */

public class DragToDeleteView {

    public interface OnLayoutRectChangeListener {
        void onChange(View view, RectF rectF);
    }

    private OnLayoutRectChangeListener onLayoutRectChangeListener;

    private View mRootView;
    private TextView mTextView;

    public DragToDeleteView(View view) {
        mRootView = view;
        mRootView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                RectF rectF = new RectF();
                Utils.RectFSetInt(rectF, left, top, right, bottom);
                if (onLayoutRectChangeListener != null) {
                    onLayoutRectChangeListener.onChange(mRootView, rectF);
                }
            }
        });

        mTextView = view.findViewById(R.id.tvDragDelete);
    }

    public void showOrHide(boolean show) {
        mRootView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setDrag2DeleteText(boolean focus) {
        String text = null;
        if (focus) {
            text = Utils.getResourceString(mRootView.getContext(), R.string.editor_drag_to_delete);
        } else {
            text = Utils.getResourceString(mRootView.getContext(), R.string.editor_release_to_delete);
        }
        mTextView.setText(text);
    }

    public void setOnLayoutRectChangeListener(OnLayoutRectChangeListener onLayoutRectChangeListener) {
        this.onLayoutRectChangeListener = onLayoutRectChangeListener;
    }
}