package robin.com.robinimageeditor.layer.photoview;

/**
 * Created by Robin Yang on 1/2/18.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

import robin.com.robinimageeditor.layer.base.detector.GestureDetectorListener;
import robin.com.robinimageeditor.layer.base.OnPhotoRectUpdateListener;
import robin.com.robinimageeditor.layer.RootNode;

/**
 * A zoomable {@link ImageView}. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished
 * Created by Robin Yang on 1/3/18.
 */
public class PhotoView extends ImageView implements RootNode<ImageView> {

    private PhotoViewAttacher mAttacher;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    @TargetApi(21)
    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mAttacher = new PhotoViewAttacher(this);
        //We always pose as a Matrix scale type, though we can change to another scale type
        //via the mAttacher
        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    public ScaleType getScaleType() {
        return mAttacher.getScaleType();
    }

    @Override
    public Matrix getImageMatrix() {
        return mAttacher.getImageMatrix();
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (mAttacher != null) {
            mAttacher.setScaleType(scaleType);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // setImageBitmap calls through to this method
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (mAttacher != null) {
            mAttacher.update();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            mAttacher.update();
        }
        return changed;
    }

    public void setRotationTo(float rotationDegree) {
        mAttacher.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        mAttacher.setRotationBy(rotationDegree);
    }

    public RectF getDisplayRect() {
        return mAttacher.getDisplayRect();
    }

    @Override
    public Matrix getDisplayMatrix() {
        Matrix matrix = new Matrix();
        mAttacher.getDisplayMatrix(matrix);
        return matrix;
    }

    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return mAttacher.setDisplayMatrix(finalRectangle);
    }

    public float getMinimumScale() {
        return mAttacher.getMinimumScale();
    }

    public void setMinimumScale(float minimumScale) {
        mAttacher.setMinimumScale(minimumScale);
    }

    public float getMediumScale() {
        return mAttacher.getMediumScale();
    }

    public void setMediumScale(float mediumScale) {
        mAttacher.setMediumScale(mediumScale);
    }

    public float getMaximumScale() {
        return mAttacher.getMaximumScale();
    }

    public void setMaximumScale(float maximumScale) {
        mAttacher.setMaximumScale(maximumScale);
    }

    public float getScale() {
        return mAttacher.getScale();
    }

    public void setScale(float scale) {
        mAttacher.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        mAttacher.setScale(scale, animate);
    }

    @Override
    public void resetMaxScale(float maxScale) {
        setMaximumScale(maxScale);
    }

    @Override
    public void resetMinScale(float minScale) {
        setMinimumScale(minScale);
    }

    public void setScaleAndTranslate(float scale, float dx, float dy) {
        mAttacher.setScaleAndTranslate(scale, dx, dy);
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAttacher.setAllowParentInterceptOnEdge(allow);
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        mAttacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    /* rootNode support */
    @Override
    public void addOnMatrixChangeListener(OnPhotoRectUpdateListener listener) {
        mAttacher.setOnMatrixChangeListener(listener);
    }

    @Override
    public void addGestureDetectorListener(GestureDetectorListener listener) {
        mAttacher.setGestureDetectorListener(listener);
    }

    private Bitmap getBitmap() {
        Bitmap bm = null;
        Drawable d = getDrawable();
        if (d != null && d instanceof BitmapDrawable) {
            bm = ((BitmapDrawable) d).getBitmap();
        }
        return bm;
    }

    @Override
    public Matrix getSupportMatrix() {
        return mAttacher.getSupportMatrix();
    }

    @Override
    public void setSupportMatrix(Matrix matrix) {
        mAttacher.setSupportMatrix(matrix);
    }

    @Override
    public Bitmap getDisplayBitmap() {
        return getBitmap();
    }

    @Override
    public void setDisplayBitmap(Bitmap bitmap) {
        setImageBitmap(bitmap);
    }

    @Override
    public ImageView getRooView() {
        return this;
    }

    @Override
    public RectF getDisplayingRect() {
        return new RectF(getDisplayRect());
    }

    @Override
    public Matrix getBaseLayoutMatrix() {
        return mAttacher.getBaseMatrix();
    }

    @Override
    public RectF getOriginalRect() {
        return mAttacher.getDisplayRect(getBaseLayoutMatrix());
    }
}
