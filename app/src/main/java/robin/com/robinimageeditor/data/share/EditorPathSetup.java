package robin.com.robinimageeditor.data.share;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class EditorSetup implements SharableData {

    private String originalImageUrl;
    private String editedImageUrl;
    private String editor2SavedPath;

    public EditorSetup(String originalImageUrl, String editedImageUrl, String editor2SavedPath) {
        this.originalImageUrl = originalImageUrl;
        this.editedImageUrl = editedImageUrl;
        this.editor2SavedPath = editor2SavedPath;
    }

    public String getOriginalImageUrl() {
        return originalImageUrl;
    }

    public void setOriginalImageUrl(String originalImageUrl) {
        this.originalImageUrl = originalImageUrl;
    }

    public String getEditedImageUrl() {
        return editedImageUrl;
    }

    public void setEditedImageUrl(String editedImageUrl) {
        this.editedImageUrl = editedImageUrl;
    }

    public String getEditor2SavedPath() {
        return editor2SavedPath;
    }

    public void setEditor2SavedPath(String editor2SavedPath) {
        this.editor2SavedPath = editor2SavedPath;
    }
}
