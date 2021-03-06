package robin.com.robinimageeditor.data.share;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class EditorResult implements SharableData {

    private String originalPath;
    private String editor2SavedPath;
    private boolean editStatus;

    public EditorResult(String originalPath, String editor2SavedPath, boolean editStatus) {
        this.originalPath = originalPath;
        this.editor2SavedPath = editor2SavedPath;
        this.editStatus = editStatus;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }

    public String getEditor2SavedPath() {
        return editor2SavedPath;
    }

    public void setEditor2SavedPath(String editor2SavedPath) {
        this.editor2SavedPath = editor2SavedPath;
    }

    /**
     * 图片是否编辑过
     *
     * @return
     */
    public boolean isEditStatus() {
        return editStatus;
    }

    public void setEditStatus(boolean editStatus) {
        this.editStatus = editStatus;
    }
}
