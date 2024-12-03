package robin.com.robinimageeditor.data.savestate;

import android.graphics.Matrix;
import android.graphics.RectF;

import robin.com.robinimageeditor.layer.crop.CropHelper;
import robin.com.robinimageeditor.layer.textpasting.TextPastingView;

/**
 * 文本编辑记录
 * Created by Robin Yang on 1/9/18.
 */

public class TextPastingSaveState extends PastingSaveStateMarker {

    private String text;
    private int textColor;
    /**
     * 文字显示Rect, 区分于高亮的initTextRect, 高亮的白色选中框initDisplayRect相对于文本Text的initTextRect, 它的left和top各减了mFocusRectOffset, right和bottom各加了mFocusRectOffset
     * {@link TextPastingView#initTextPastingSaveState}
     */
    private RectF initTextRect;  // 坐标已经是通过逆矩阵计算出来的, 相对于图片编辑框左上角的位置(不是相对于屏幕左上角), 所以直接在canvas上画就行

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
