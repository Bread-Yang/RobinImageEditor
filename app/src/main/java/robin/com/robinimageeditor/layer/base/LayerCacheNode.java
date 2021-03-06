package robin.com.robinimageeditor.layer.base;

import java.util.HashMap;

import robin.com.robinimageeditor.editcache.LayerEditCache;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface LayerCacheNode {

    String getLayerTag();

    void restoreLayerEditData(HashMap<String, LayerEditCache> cacheDataHashMap);

    void saveLayerEditData(HashMap<String, LayerEditCache> cacheDataHashMap);
}
