package robin.com.robinimageeditor.layer;

import java.util.HashMap;

import robin.com.robinimageeditor.editcache.EditorCacheData;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface LayerCacheNode {

    String getLayerTag();

    void restoreLayerEditData(HashMap<String, EditorCacheData> cacheDataHashMap);

    void saveLayerEditData(HashMap<String, EditorCacheData> cacheDataHashMap);
}
