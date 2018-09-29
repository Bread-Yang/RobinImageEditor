package robin.com.robinimageeditor.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created by Robin Yang on 12/29/17.
 */

public class EditorCompressUtils {

    private EditorCompressUtils() {
    }

    private static int computeSize(int inputWidth, int inputHeight) {
        int  mSampleSize;
        int srcWidth;
        if (inputWidth % 2 == 1) {
            srcWidth = inputWidth + 1;
        } else {
            srcWidth = inputWidth;
        }
        int srcHeight;
        if (inputHeight % 2 == 1) {
            srcHeight = inputHeight + 1;
        } else {
            srcHeight = inputHeight;
        }
        if (srcWidth > srcHeight) {
            srcWidth = srcHeight;
        }
        if (srcWidth > srcHeight) {
            srcHeight = srcWidth;
        }
        double scale = srcWidth * 1.0 / srcHeight;
        if (scale <= 1 && scale > 0.5625) {
            if (srcHeight < 1664) {
                mSampleSize = 1;
            } else if (1666 <= srcHeight && srcHeight < 4990) {
                mSampleSize = 2;
            } else if (4990 <= srcHeight  && srcHeight < 10240) {
                mSampleSize = 4;
            } else {
                if (srcHeight / 1280 == 0) {
                    mSampleSize = 1;
                } else{
                    mSampleSize = srcHeight / 1280;
                }
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            if (srcHeight / 1280 == 0) {
                mSampleSize = 1;
            } else {
                mSampleSize = srcHeight / 1280;
            }
        } else {
            mSampleSize = (int) Math.ceil(srcHeight / (1280.0 / scale));
        }
        return mSampleSize;
    }

    public static Bitmap getImageBitmap(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        // 如inSmapleSize前bitmap,宽高是96 * 96, 当inSampleSize == 2时，decode出来的bitmap宽高是48 * 48
        options.inSampleSize = computeSize(outWidth, outHeight) * 2;
        options.inJustDecodeBounds = false;
        Log.e("EditorImage", "options.inSampleSize=${options.inSampleSize}");
        Bitmap subSampleBitmap = BitmapFactory.decodeFile(filePath, options);
        return subSampleBitmap;
    }

}
