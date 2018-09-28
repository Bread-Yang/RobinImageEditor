package robin.com.robinimageeditor;


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import robin.com.robinimageeditor.data.share.EditorResult
import robin.com.robinimageeditor.data.share.EditorSetup
import robin.com.robinimageeditor.util.PathUtils

/**
 * Created by Robin Yang on 12/27/17.
 */
class MainActivity : AppCompatActivity() {

    private var mOriginalPath: String? = null
    private lateinit var ivDisplay: ImageView
    private lateinit var mEditorPath: String
    private val editResultMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        ivDisplay = findViewById<ImageView>(R.id.iv_display)
        ivDisplay.scaleType = ImageView.ScaleType.FIT_CENTER
    }

    companion object {
        private val ACTION_REQUEST_GALLERY = 301
        private val ACTION_REQUEST_EDITOR = 302
    }

    fun getEditorSavePath() = "${Environment.getExternalStorageDirectory()}/EditorCache/image-editor-${System.currentTimeMillis()}.png"

    fun chooseImage(view: View) {
        pickFromGallery()
    }

    fun editImage(view: View) {
        mOriginalPath ?: let {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show()
            return
        }
        val source = editResultMap[mOriginalPath!!]
        val setup = EditorSetup(source, mOriginalPath, getEditorSavePath())
        val intent = ImageEditorActivity.intent(this, setup)
        startActivityForResult(intent, ACTION_REQUEST_EDITOR)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "选择照片")
        startActivityForResult(chooser, ACTION_REQUEST_GALLERY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        data ?: return
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                ACTION_REQUEST_GALLERY -> {
                    val uri = data.data
                    val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

                    if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                    } else {
                        mOriginalPath = PathUtils.getFilePath(this, uri)
                        //ivDisplay.setImageURI(Uri.fromFile(File(mOriginalPath)))
                        Glide.with(this).load(mOriginalPath).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .apply(RequestOptions.skipMemoryCacheOf(true)).into(ivDisplay)
                    }
                }

                ACTION_REQUEST_EDITOR -> {
                    val result = data.getSerializableExtra(resultCode.toString()) as EditorResult
                    if (result.isEditStatus()) {
                        //editor result path and original path
                        mOriginalPath = result.editor2SavedPath
                        editResultMap.put(result.editor2SavedPath, result.originalPath!!)
                        //ivDisplay.setImageURI(Uri.fromFile(File(result.editor2SavedPath)))
                        Glide.with(this).load(result.editor2SavedPath).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .apply(RequestOptions.skipMemoryCacheOf(true)).into(ivDisplay)
                    }
                }
            }

        }
    }
}


