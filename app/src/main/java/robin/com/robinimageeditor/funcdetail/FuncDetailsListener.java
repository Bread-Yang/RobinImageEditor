package robin.com.robinimageeditor.funcdetail;

import robin.com.robinimageeditor.editmode.EditorMode;

/**
 * Created by Robin Yang on 1/3/18.
 */

public interface FuncDetailsListener {

    void onReceiveDetails(EditorMode editorMode, FuncDetailsMarker funcdetailsMarker);

}
