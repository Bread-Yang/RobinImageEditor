package robin.com.robinimageeditor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by Robin Yeung on 10/8/18.
 */
public class PictureStrokeRadio extends PictureColorRadio {

    public PictureStrokeRadio(Context context) {
        super(context);
    }

    public PictureStrokeRadio(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PictureStrokeRadio(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        float hw = getWidth() / 2f, hh = getHeight() / 2f;
        float radius = Math.min(hw, hh);

        canvas.save();
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(hw, hh, getBallRadius(radius), mPaint);

        mPaint.setColor(isChecked() ? mStrokeColor : Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(hw, hh, getRingRadius(radius), mPaint);
        canvas.restore();
    }

    public int getStrokeWidth() {
        return getWidth();
    }
}