package robin.com.robinimageeditor.layer;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.util.Utils;

/**
 * Created by Robin Yang on 1/17/18.
 */

public class CropDetailsView implements ViewTreeObserver.OnPreDrawListener {

    public interface OnCropOperationListener {
        void onCropRotation(float degree);

        void onCropCancel();

        void onCropConfirm();

        void onCropRestore();
    }

    public interface OnOperateCallback {
        void preDrawSize(int width, int height);
    }

    private View view;
    OnCropOperationListener cropListener;
    private TextView mRestoreView;
    OnOperateCallback mCallback;

    public CropDetailsView(View view) {
        this.view = view;
        init();
    }

    private void init() {
        view.getViewTreeObserver().addOnPreDrawListener(this);
        view.findViewById(R.id.ivCropRotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropListener != null) {
                    cropListener.onCropRotation(90f);
                }
            }
        });
        view.findViewById(R.id.ivCropCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropListener != null) {
                    cropListener.onCropCancel();
                }
            }
        });
        mRestoreView = view.findViewById(R.id.tvCropRestore);
        mRestoreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropListener != null) {
                    cropListener.onCropRestore();
                }
            }
        });
        view.findViewById(R.id.ivCropConfirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropListener != null) {
                    cropListener.onCropConfirm();
                }
            }
        });
    }

    @Override
    public boolean onPreDraw() {
        if (mCallback != null) {
            mCallback.preDrawSize(view.getWidth(), view.getHeight());
        }
        view.getViewTreeObserver().removeOnPreDrawListener(this);
        return false;
    }

    public void setRestoreTextStatus(boolean restore) {
        int color;
        if (restore) {
            color = R.color.green_btn;
        } else {
            color = R.color.white_f;
        }
        color = Utils.getResourceColor(view.getContext(), color);
        mRestoreView.setTextColor(color);
    }

    public void showOrHide(boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void setCropListener(OnCropOperationListener cropListener) {
        this.cropListener = cropListener;
    }

    public View getView() {
        return view;
    }
}
