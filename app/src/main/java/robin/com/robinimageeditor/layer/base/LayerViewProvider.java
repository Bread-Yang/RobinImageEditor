package robin.com.robinimageeditor.layer.base;

import android.content.Context;
import android.view.View;

import robin.com.robinimageeditor.data.Pair;
import robin.com.robinimageeditor.layer.crop.CropHelper;
import robin.com.robinimageeditor.layer.LayerComposite;
import robin.com.robinimageeditor.layer.RootEditorDelegate;
import robin.com.robinimageeditor.editmode.EditorMode;
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
