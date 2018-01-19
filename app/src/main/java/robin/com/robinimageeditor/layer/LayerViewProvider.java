package robin.com.robinimageeditor.layer;

import android.content.Context;
import android.view.View;

import robin.com.robinimageeditor.bean.Pair;
import robin.com.robinimageeditor.view.EditorMode;
import robin.com.robinimageeditor.view.FuncAndActionBarAnimHelper;

/**
 * Created by Robin Yang on 12/28/17.
 */

public interface LayerViewProvider {

    View findLayerByEditorMode(EditorMode editorMode);

    Context getActivityContext();

    FuncAndActionBarAnimHelper getFuncAndActionBarAnimHelper();

    CropHelper getCropHelper();

    RootEditorDelegate getRootEditorDelegate();

    LayerComposite getLayerCompositeView();

    String getSetupEditorId();

    String getResultEditorId();

    Pair<Integer, Integer> getEditorSizeInfo();

    Pair<Integer, Integer> getScreenSizeInfo();
}
