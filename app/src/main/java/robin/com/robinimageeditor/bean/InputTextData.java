package robin.com.robinimageeditor.bean;

import robin.com.robinimageeditor.data.share.SharableData;

/**
 * Created by Robin Yang on 1/15/18.
 */

public class InputTextData implements SharableData {

    private String id;
    private String text;
    private int color;

    public InputTextData(String id, String text, int color) {
        this.id = id;
        this.text = text;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
