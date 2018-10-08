package robin.com.robinimageeditor.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

/**
 * Created by Robin Yeung on 10/8/18.
 */
public class PictureStrokeGroup extends RadioGroup {

    public PictureStrokeGroup(Context context) {
        super(context);
    }

    public PictureStrokeGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCheckStroke() {
        int checkedId = getCheckedRadioButtonId();
        PictureStrokeRadio radio = (PictureStrokeRadio)findViewById(checkedId);
        if (radio != null) {
            return radio.getStrokeWidth();
        }
        return 72;
    }

    public void setCheckStroke(float stroke) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            PictureStrokeRadio radio = (PictureStrokeRadio) getChildAt(i);
            if (radio.getStrokeWidth() == stroke) {
                radio.setChecked(true);
                break;
            }
        }
    }
}
