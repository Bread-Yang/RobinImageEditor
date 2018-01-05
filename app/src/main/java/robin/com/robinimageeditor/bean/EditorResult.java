package robin.com.robinimageeditor.bean;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class EditorResult implements SharableData {

    private String originalPath;
    private String editorPath;
    private String editor2SavedPath;
    private boolean editStatus;

    public EditorResult(String originalPath, String editorPath, String editor2SavedPath, boolean editStatus) {
        this.originalPath = originalPath;
        this.editorPath = editorPath;
        this.editor2SavedPath = editor2SavedPath;
        this.editStatus = editStatus;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getEditorPath() {
        return editorPath;
    }

    public void setEditorPath(String editorPath) {
        this.editorPath = editorPath;
    }

    public String getEditor2SavedPath() {
        return editor2SavedPath;
    }

    public void setEditor2SavedPath(String editor2SavedPath) {
        this.editor2SavedPath = editor2SavedPath;
    }

    public boolean isEditStatus() {
        return editStatus;
    }

    public void setEditStatus(boolean editStatus) {
        this.editStatus = editStatus;
    }
}
