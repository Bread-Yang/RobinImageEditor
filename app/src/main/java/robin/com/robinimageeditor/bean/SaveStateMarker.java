package robin.com.robinimageeditor.bean;

import robin.com.robinimageeditor.util.Utils;

/**
 * Created by Robin Yang on 12/28/17.
 */

public abstract class SaveStateMarker {

    private String id = Utils.randomId();

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SaveStateMarker) {
            return id == (((SaveStateMarker) obj).getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void reset() {

    }

    public SaveStateMarker deepCopy() {
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
