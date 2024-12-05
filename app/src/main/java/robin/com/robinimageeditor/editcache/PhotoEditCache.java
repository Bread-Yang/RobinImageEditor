package robin.com.robinimageeditor.editcache;

import java.util.HashMap;

/**
 * Created by Robin Yang on 12/29/17.
 */

public class PhotoEditCache {

    private volatile static PhotoEditCache sInstance;

    /**
     * Images edit cache data hashMap.
     * Key : image's url(Unique) Value : image's edit cache data.
     */
    private HashMap<String, HashMap<String, LayerEditCache>> mLayerCache = new HashMap<>();

    private PhotoEditCache() {

    }

    public static PhotoEditCache getInstance() {
        if (sInstance == null) {
            synchronized (PhotoEditCache.class) {
                if (sInstance == null) {
                    sInstance = new PhotoEditCache();
                }
            }
        }
        return sInstance;
    }

    public HashMap<String, LayerEditCache> getEditCacheDataByImageUrl(String editorId) {
        HashMap<String, LayerEditCache> cache = mLayerCache.get(editorId);
        if (cache == null) {
            cache = new HashMap<>();
            mLayerCache.put(editorId, cache);
        }
        return cache;
    }

    public void putEditCacheDataByImageUrl(String editorId, HashMap<String, LayerEditCache> data) {
        if (data != null) {
            mLayerCache.put(editorId, data);
        }
    }
}
