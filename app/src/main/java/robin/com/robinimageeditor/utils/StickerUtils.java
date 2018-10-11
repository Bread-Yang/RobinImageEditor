package robin.com.robinimageeditor.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.util.Arrays;

import robin.com.robinimageeditor.R;
import robin.com.robinimageeditor.layer.sticker.StickerType;

/**
 * Created by Robin Yang on 1/5/18.
 */

public class StickerUtils {

    private static StickerUtils mInstance;

    private int[] mEmojiResource = new int[]{
            R.mipmap.f14, R.mipmap.f1, R.mipmap.f2,
            R.mipmap.f3, R.mipmap.f4, R.mipmap.f5, R.mipmap.f6, R.mipmap.f7,
            R.mipmap.f8, R.mipmap.f9, R.mipmap.f10, R.mipmap.f11, R.mipmap.f12,
            R.mipmap.f13, R.mipmap.f0, R.mipmap.f15, R.mipmap.f16, R.mipmap.f96,
            R.mipmap.f18, R.mipmap.f19, R.mipmap.f20, R.mipmap.f21, R.mipmap.f22,
            R.mipmap.f23, R.mipmap.f24, R.mipmap.f25, R.mipmap.f26, R.mipmap.f27,
            R.mipmap.f28, R.mipmap.f29, R.mipmap.f30, R.mipmap.f31, R.mipmap.f32,
            R.mipmap.f33, R.mipmap.f34, R.mipmap.f35, R.mipmap.f36, R.mipmap.f37,
            R.mipmap.f38, R.mipmap.f39, R.mipmap.f97, R.mipmap.f98, R.mipmap.f99,
            R.mipmap.f100, R.mipmap.f101, R.mipmap.f102, R.mipmap.f103, R.mipmap.f104,
            R.mipmap.f105, R.mipmap.f106, R.mipmap.f107, R.mipmap.f108, R.mipmap.f109,
            R.mipmap.f110, R.mipmap.f111, R.mipmap.f112, R.mipmap.f89, R.mipmap.f113,
            R.mipmap.f114, R.mipmap.f115, R.mipmap.f60, R.mipmap.f61, R.mipmap.f46,
            R.mipmap.f63, R.mipmap.f64, R.mipmap.f116, R.mipmap.f66, R.mipmap.f67,
            R.mipmap.f53, R.mipmap.f54, R.mipmap.f55, R.mipmap.f56, R.mipmap.f57,
            R.mipmap.f117, R.mipmap.f59, R.mipmap.f75, R.mipmap.f74, R.mipmap.f69,
            R.mipmap.f49, R.mipmap.f76, R.mipmap.f77, R.mipmap.f78, R.mipmap.f79,
            R.mipmap.f118, R.mipmap.f119, R.mipmap.f120, R.mipmap.f121, R.mipmap.f122,
            R.mipmap.f123, R.mipmap.f124
    };

    private StickerUtils() {}

    public static StickerUtils getInstance() {
        if (mInstance == null) {
            mInstance = new StickerUtils();
        }
        return mInstance;
    }

    private int getByIndex(StickerType stickerType, int index) {
        if (isIndexValidate(stickerType, index)) {
            return mEmojiResource[index];
        }
        return -1;
    }

    private boolean isIndexValidate(StickerType stickerType, int index) {
        if (stickerType == StickerType.Emoji) {
            return index >= 0 && index < mEmojiResource.length;
        }
        return false;
    }

    public int[] getStickers(StickerType stickerType) {
        if (stickerType == StickerType.Emoji) {
            return Arrays.copyOf(mEmojiResource, mEmojiResource.length);
        }
        return null;
    }

    public Bitmap getStickerBitmap(Context context, StickerType stickerType, int index) {
        int resId = getByIndex(stickerType, index);
        if (resId == -1) {
            return null;
        }
        if (stickerType == StickerType.Emoji) {
            Drawable drawable = getLocalDrawable(context, resId);
            if (drawable == null) {
                return null;
            }
            return ((BitmapDrawable)drawable).getBitmap();
        }
        return null;
    }

    private Drawable getLocalDrawable(Context context, int id) {
        return context.getResources().getDrawable(id);
    }
}
