package robin.com.robinimageeditor;


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import robin.com.robinimageeditor.data.share.EditorPathSetup
import robin.com.robinimageeditor.data.share.EditorResult
import robin.com.robinimageeditor.utils.PathUtils
import java.util.Arrays

/**
 * Created by Robin Yang on 12/27/17.
 */
class MainActivity : AppCompatActivity() {

    private var mDisplayImageUrl: String? = null
    private lateinit var ivDisplay: ImageView
    private lateinit var mEditorPath: String
    /**
     * Key : 编辑后图片的url, 原来图片的url
     */
    private val editResultMap = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        ivDisplay = findViewById<ImageView>(R.id.iv_display)
        ivDisplay.scaleType = ImageView.ScaleType.FIT_CENTER

        testPre()
        testPost()
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
        mDisplayImageUrl ?: let {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show()
            return
        }
        var source = editResultMap[mDisplayImageUrl!!]

        // 第一次选择图片
        if (source == null) {
            source = mDisplayImageUrl
            mDisplayImageUrl = null
        }

        val setup = EditorPathSetup(source, mDisplayImageUrl, getEditorSavePath())
        val intent = ImageEditorActivity.intent(this, setup)
        startActivityForResult(intent, ACTION_REQUEST_EDITOR)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "选择照片")
        startActivityForResult(chooser, ACTION_REQUEST_GALLERY)
    }

    private fun testPre() {
        val TAG = "Matrix pre操作 : "
        val matrix = Matrix()
        matrix.preTranslate(100f, 100f)
        val points = floatArrayOf(10f, 10f)
        matrix.mapPoints(points)
        Log.i(TAG, "preTranslate(100, 100)之后 : " + points.contentToString())   // 打印第一次postTranslate后, 原坐标(0, 0)对应的新坐标

        points[0] = 10f
        points[1] = 10f
        matrix.preScale(0.5f, 0.5f)
        matrix.mapPoints(points)

        // 结果是(105.0f, 105.0f)
        // 进行变换的顺序是先执行preScale(0.5f,0.5f)，再执行的preTranslate(100f,100f)
        Log.i(TAG, "preTranslate(100, 100), 再preScale(0.5f, 0.5f)之后 : " + points.contentToString())   // 打印再次postScale后, 原坐标(0, 0)对应的新坐标
    }

    private fun testPost() {
        val TAG = "Matrix post操作 : "
        val matrix = Matrix()
        matrix.postTranslate(100f, 100f)
        val points = floatArrayOf(10f, 10f)
        matrix.mapPoints(points)
        Log.i(TAG, "postTranslate(100, 100)之后 : " + points.contentToString())   // 打印第一次postTranslate后, 原坐标(0, 0)对应的新坐标

        points[0] = 10f
        points[1] = 10f
        matrix.postScale(0.5f, 0.5f)
        matrix.mapPoints(points)

        // 结果是(55.0f, 55.0f)
        // 进行变换的顺序是先执行postTranslate(100f,100f)，再执行的postScale(0,5f,0.5f)
        Log.i(TAG, "postTranslate(100, 100), 再postScale(0.5f, 0.5f)之后 : " + points.contentToString())   // 打印再次postScale后, 原坐标(0, 0)对应的新坐标

        val invert = Matrix()
        matrix.invert(invert)
        invert.mapPoints(points)
        // 结果是(10f, 10f), 变换前的初始值
        Log.i(TAG, "invert之后 : " + points.contentToString())
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
                        mDisplayImageUrl = PathUtils.getFilePath(this, uri)
                        //ivDisplay.setImageURI(Uri.fromFile(File(mDisplayImageUrl)))
                        Glide.with(this).load(mDisplayImageUrl).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                                .apply(RequestOptions.skipMemoryCacheOf(true)).into(ivDisplay)
                    }
                }

                ACTION_REQUEST_EDITOR -> {
                    val result = data.getSerializableExtra(resultCode.toString()) as EditorResult
                    if (result.isEditStatus()) {
                        //editor result path and original path
                        mDisplayImageUrl = result.editor2SavedPath
                        editResultMap.put(result.editor2SavedPath, result.originalPath!!)
                    } else {
                        mDisplayImageUrl = result.originalPath
                    }
                    Glide.with(this).load(mDisplayImageUrl).apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                            .apply(RequestOptions.skipMemoryCacheOf(true)).into(ivDisplay)
                }
            }

        }
    }
}


