package robin.com.robinimageeditor.layer.crop;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.utils.MatrixUtils;

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
    private TextView tvCropRestore;
    OnOperateCallback mCallback;

    public CropDetailsView(View view) {
        this.view = view;
        init();
    }

    private void init() {
        view.getViewTreeObserver().addOnPreDrawListener(this);
        view.findViewById(R.id.ivCropLeftRotate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cropListener != null) {
                    cropListener.onCropRotation(-90f);
                }
            }
        });
        view.findViewById(R.id.ivCropRightRotate).setOnClickListener(new View.OnClickListener() {
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
        tvCropRestore = view.findViewById(R.id.tvCropRestore);
        tvCropRestore.setOnClickListener(new View.OnClickListener() {
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
            color = R.color.image_color_white;
        } else {
            color = R.color.color_919191;
        }
        color = MatrixUtils.getResourceColor(view.getContext(), color);
        tvCropRestore.setTextColor(color);
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
