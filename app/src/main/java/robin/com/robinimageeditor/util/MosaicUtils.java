package robin.com.robinimageeditor.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by Robin Yang on 1/8/18.
 */

public class MosaicUtils {

    public static Bitmap getGridMosaic(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int radius = 20;
        Bitmap mosaicBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mosaicBitmap);
        int horizontalCount = (int) Math.ceil(width / (float) radius);
        int verticalCount = (int) Math.ceil(height / (float) radius);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        for (int horizontalIndex = 0; horizontalIndex < horizontalCount; horizontalIndex++) {
            for (int verticalIndex = 0; verticalIndex < verticalCount; verticalIndex++) {
                int left = radius * horizontalIndex;
                int top = radius * verticalIndex;
                int right = left + radius;
                if (right > width) {
                    right = width;
                }
                int bottom = top + radius;
                if (bottom > height) {
                    bottom = height;
                }
                int color = bitmap.getPixel(left, top);
                Rect rect = new Rect(left, top, right, bottom);
                paint.setColor(color);
                canvas.drawRect(rect, paint);
            }
        }
        return mosaicBitmap;
    }

    public static Bitmap getBlurMosaic(Bitmap bitmap) {
        int iterations = 1;
        int radius = 8;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap blured = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < iterations; i++) {
            blur(inPixels, outPixels, width, height, radius);
            blur(outPixels, inPixels, height, width, radius);
        }
        blured.setPixels(inPixels, 0, width, 0, 0, width, height);
        return blured;
    }

    private static void blur(int[] input, int[] out, int width, int height, int radius) {
        int widthMinus = width - 1;
        int tableSize = 2 * radius + 1;
        int[] divide = new int[256 * tableSize];
        for (int index = 0; index < divide.length; index++) {
            divide[index] = index / tableSize;
        }
        int inIndex = 0;
        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0;
            int tr = 0;
            int tg = 0;
            int tb = 0;
            for (int i = -radius; i <= radius; i++) {
                int rgb = input[inIndex + clamp(i, 0, width - 1)];
                ta += rgb >> 24 & 0xff;
                tr += rgb >> 16 & 0xff;
                tg += rgb >> 8 & 0xff;
                tb += rgb & 0xff;
            }
            for (int x = 0; x < width; x++) {
                out[outIndex] = divide[ta] << 24 | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + radius + 1;
                if (i1 > widthMinus)
                    i1 = widthMinus;
                int i2 = x - radius;
                if (i2 < 0)
                    i2 = 0;
                int rgb1 = input[inIndex + i1];
                int rgb2 = input[inIndex + i2];

                ta += (rgb1 >> 24 & 0xff) - (rgb2 >> 24 & 0xff);
                tr += (rgb1 & 0xff0000) - (rgb2 & 0xff0000) >> 16;
                tg += (rgb1 & 0xff00) - (rgb2 & 0xff00) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    private static int clamp(int x, int a, int b) {
        if (x < a) {
            return a;
        } else if (x > b) {
            return b;
        } else {
            return x;
        }
    }
}
