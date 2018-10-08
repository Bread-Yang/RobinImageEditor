package robin.com.robinimageeditor.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.RadioGroup;

public class PictureColorGroup extends RadioGroup {

    public PictureColorGroup(Context context) {
        super(context);
    }

    public PictureColorGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public int getCheckColor() {
        int checkedId = getCheckedRadioButtonId();
        PictureColorRadio radio = (PictureColorRadio)findViewById(checkedId);
        if (radio != null) {
            return radio.getColor();
        }
        return Color.RED;
    }

    public void setCheckColor(int color) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            PictureColorRadio radio = (PictureColorRadio) getChildAt(i);
            if (radio.getColor() == color) {
                radio.setChecked(true);
            }else {
                radio.setChecked(false);
            }
        }
    }
}
