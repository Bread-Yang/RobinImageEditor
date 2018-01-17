package robin.com.robinimageeditor.bean;

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * Created by Robin Yang on 1/9/18.
 */

public class TextPastingSaveState extends PastingSaveStateMarker {

    private String text;
    private int textColor;
    private RectF initTextRect;

    public TextPastingSaveState(String text, int textColor, RectF initTextRect, RectF initDisplayRect,
                                Matrix displayMatrix, Matrix transformMatrix) {
        super(initDisplayRect, displayMatrix, transformMatrix);
        this.text = text;
        this.textColor = textColor;
        this.initTextRect = initTextRect;
    }

    public String getText() {
        return text;
    }

    public int getTextColor() {
        return textColor;
    }

    public RectF getInitTextRect() {
        return initTextRect;
    }

    @Override
    public SaveStateMarker deepCopy() {
        SaveStateMarker state = new TextPastingSaveState(text, textColor,
                new RectF(initTextRect), new RectF(initDisplayRect),
                new Matrix(initDisplayMatrix),
                new Matrix(transformMatrix));
        state.setId(this.getId());
        return state;
    }
}
