package robin.com.robinimageeditor.funcdetail;

/**
 * Created by Robin Yang on 1/3/18.
 */

public class ScrawlDetails implements FuncDetailsMarker {

    private int color;

    public ScrawlDetails(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
