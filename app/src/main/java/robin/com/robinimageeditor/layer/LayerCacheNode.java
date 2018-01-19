package robin.com.robinimageeditor.layer;

import java.util.HashMap;

import robin.com.robinimageeditor.bean.EditorCacheData;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface LayerCacheNode {

    String getLayerTag();

    void restoreLayerData(HashMap<String, EditorCacheData> cacheDataHashMap);

    void saveLayerData(HashMap<String, EditorCacheData> cacheDataHashMap);
}
