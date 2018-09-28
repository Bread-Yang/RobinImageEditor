package robin.com.robinimageeditor.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.bean.InputStickerData;
import robin.com.robinimageeditor.util.StickerUtils;

/**
 * Created by Robin Yang on 1/5/18.
 */

public class StickerDetailsView extends FrameLayout {

    private RecyclerView stickerView;
    private OnStickerClickResult onStickerClickListener;

    public interface OnStickerClickResult {
        void onResult(InputStickerData stickerData);
    }

    public StickerDetailsView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public StickerDetailsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerDetailsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StickerDetailsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.sticker_func_details, this, true);
        stickerView = findViewById(R.id.rvSticker);
        GridLayoutManager layoutManager = new GridLayoutManager(context, 8);
        stickerView.setAdapter(new SimpleStickerAdapter(StickerType.Emoji));
        stickerView.setLayoutManager(layoutManager);
    }

    public void setOnStickerClickListener(OnStickerClickResult onStickerClickListener) {
        this.onStickerClickListener = onStickerClickListener;
    }

    class SimpleStickerAdapter extends RecyclerView.Adapter<SimpleStickerAdapter.SimpleStickerHolder> {

        StickerType stickerType;
        int[] stickerResource;

        public SimpleStickerAdapter(StickerType stickerType) {
            this.stickerType = stickerType;
            stickerResource = StickerUtils.getInstance().getStickers(stickerType);
        }

        @Override
        public SimpleStickerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.sticker_details_recycle_item, parent, false);
            return new SimpleStickerHolder(view);
        }

        @Override
        public void onBindViewHolder(SimpleStickerHolder holder, final int position) {
            holder.iv.setImageResource(stickerResource[position]);
            holder.iv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onStickerClickListener != null) {
                        onStickerClickListener.onResult(new InputStickerData(stickerType, position));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return stickerResource.length;
        }

        class SimpleStickerHolder extends RecyclerView.ViewHolder {

            ImageView iv;

            public SimpleStickerHolder(View itemView) {
                super(itemView);
                iv = itemView.findViewById(R.id.ivSimpleSticker);
            }
        }
    }
}
