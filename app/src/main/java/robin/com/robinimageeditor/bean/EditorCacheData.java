package robin.com.robinimageeditor.bean;

import android.support.v4.util.ArrayMap;

import java.util.Map;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class EditorCacheData implements SharableData {

    private ArrayMap<String, SaveStateMarker> layerCache;

    public EditorCacheData(ArrayMap<String, SaveStateMarker> layerCache) {
        this.layerCache = layerCache;
    }

    public Map<String, SaveStateMarker> getLayerCache() {
        return layerCache;
    }

    public void setLayerCache(ArrayMap<String, SaveStateMarker> layerCache) {
        this.layerCache = layerCache;
    }
}
