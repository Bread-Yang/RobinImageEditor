package robin.com.robinimageeditor;

import android.util.LruCache;

import java.util.HashMap;

import robin.com.robinimageeditor.bean.EditorCacheData;

/**
 * Created by Robin Yang on 12/29/17.
 */

public class LayerCache {

    private static LayerCache sInstance;

    private LruCache<String, HashMap<String, EditorCacheData>> mLayerCache = new LruCache<>(5);

    private LayerCache() {

    }

    public static LayerCache getIntance() {
        if (sInstance == null) {
            sInstance = new LayerCache();
        }
        return sInstance;
    }

    public HashMap<String, EditorCacheData> getCacheDataById(String editorId) {
        HashMap<String, EditorCacheData> cache = mLayerCache.get(editorId);
        if (cache == null) {
            cache = new HashMap<>();
            mLayerCache.put(editorId, cache);
        }
        return cache;
    }

    public void cacheEditorData(String editorId, HashMap<String, EditorCacheData> data) {
        if (data != null) {
            mLayerCache.put(editorId, data);
        }
    }
}
