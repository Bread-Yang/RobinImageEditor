package robin.com.robinimageeditor.layer.base.detector;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * Does a whole lot of gesture detecting.
 * Created by Robin Yang on 12/29/17.
 */
public class CustomGestureDetector {

    private static final String TAG = "CustomGestureDetector";

    private static final int INVALID_POINTER_ID = -1;

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;
    private final ScaleGestureDetector mScaleDetector;
    private final RotateGestureDetector mRotateDetector;

    private VelocityTracker mVelocityTracker;
    private boolean mIsDragging;
    private float mLastTouchX;
    private float mLastTouchY;
    private final float mTouchSlop;
    private final float mMinimumVelocity;
    private GestureDetectorListener mListener;
    private boolean enableMultipleFinger = true;
    private boolean isScaling = false;
    private boolean isRotating = false;

    public CustomGestureDetector(Context context, GestureDetectorListener listener) {
        final ViewConfiguration configuration = ViewConfiguration
                .get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        // getScaledTouchSlop 是按根据设备密度（density）来获取的最小滑动距离，默认是 8dp （<dimen name="config_viewConfigurationTouchSlop">8dp</dimen>）
//        mTouchSlop = configuration.getScaledTouchSlop();
        mTouchSlop = 2;

        mListener = listener;
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();

                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor))
                    return false;

                if (isRotating == false) {
                    isScaling = true;
                    mListener.onScale(scaleFactor,
                            detector.getFocusX(), detector.getFocusY(), false);
                    isScaling = false;
                }

                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                // NO-OP
            }
        };
        mScaleDetector = new ScaleGestureDetector(context, mScaleListener);

        RotateGestureDetector.OnRotateGestureListener mRotateGestureListener = new RotateGestureDetector.OnRotateGestureListener() {

            @Override
            public boolean onRotate(float degrees, float focusX, float focusY) {
                if (isScaling == false) {
                    isRotating = true;
                    mListener.onRotate(degrees, focusX, focusY, false);
                    isRotating = false;
                }
                return true;
            }
        };
        mRotateDetector = new RotateGestureDetector(context, mRotateGestureListener);
    }

    public CustomGestureDetector(Context context, GestureDetectorListener listener, boolean enableMultipleFinger) {
        this(context, listener);
        this.enableMultipleFinger = enableMultipleFinger;
    }

    private float getActiveX(MotionEvent ev) {
        // 获取事件坐标有两种方法，一种是无参数的 float getX() ,这个方法获取的是索引为0的点的坐标，一种是带参数的 float getX(int pointerIndex) ，这个需要传入索引值，用于多指操作
        try {
            return ev.getX(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getX();
        }
    }

    private float getActiveY(MotionEvent ev) {
        try {
            return ev.getY(mActivePointerIndex);
        } catch (Exception e) {
            return ev.getY();
        }
    }

    public boolean isScaling() {
        return mScaleDetector.isInProgress();
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        try {
            if (enableMultipleFinger) {
                mScaleDetector.onTouchEvent(ev);
                mRotateDetector.onTouchEvent(ev);
            }
            // touch事件产生的所有x,y坐标, 都是相对于屏幕左上角的坐标, 要拿到真正相对于图片编辑框左上角的真正坐标,必须通过逆矩阵,也就是通过方法
            // MatrixUtils.mapInvertMatrixPoint(getDrawMatrix(), new PointF(x, y))
            // 才能拿到相对于图片编辑框左上角的真正坐标
            return processTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            // Fix for support lib bug, happening when onDestroy is called
            return true;
        }
    }

    /**
     * ACTION_DOWN 第一个手指按下
     * ACTION_POINTER_DOWN 第一个手指按下后其他手指按下
     * ACTION_POINTER_UP 多个手指长按时抬起其中一个手指，注意松开后还有手指在屏幕上
     * ACTION_UP 最后一个手指抬起
     * ACTION_MOVE 手指移动
     * ACTION_CANCEL 父View收到ACTION_DOWN后会把事件传给子View，如果后续的ACTION_MOVE和ACTION_UP等事件被父View拦截掉，那子View就会收到ACTION_CANCEL事件
     * 可以通过 getAction() 方法获取到一个动作，这里的返回值，对于单指而言，就是动作的状态，含义跟上面这些常量一样，但是如果是多指按下或者抬起，返回值是包含动作的索引的，多指的滑动返回值不包含索引，还是状态。动作的状态和索引可以分开获取，getActionMasked() 可以只获取状态，getActionIndex() 可以只获取索引
     * 对于多指操作，要关注两个属性，触摸点id（PointerId）和索引（PointerIndex），触摸点索引可以通过刚刚说的 getActionIndex() 获取到，也可以通过 findPointerIndex(int pointerId) 获取到，触摸点id可以通过 getPointerId(int pointerIndex) 方法来获取，这个方法需要传入触摸点索引。值得注意的是 PointerId 和 PointerIndex 的取值
     * PointerId 手指按下时生成，手指抬起时回收，注意多点触摸时，抬起任何一个手指，其他手指的 PointerId 不变，PointerId 赋值后不会变更
     * PointerIndex 手指按下时生成，从0开始计数，多点触摸抬起其中一个手指时，后面的手指 PointerIndex 会更新，取值范围是0~触摸点个数-1
     *
     * @param ev The MotionEvent object containing full information about
     *        the event.
     * @return
     */
    private boolean processTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);

                // 初始化 VelocityTracker ，VelocityTracker 是一个速度检测类，内部存了个 SynchronizedPool ，obtain() 方法会优先从池子里取 VelocityTracker 的实例，取不到再创建
                mVelocityTracker = VelocityTracker.obtain();
                if (null != mVelocityTracker) {
                    // addMovement 用于跟踪移动事件，一般会在 ACTION_DOWN 、ACTION_MOVE 、ACTION_UP 中调用
                    mVelocityTracker.addMovement(ev);
                }

                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                mIsDragging = false;
                mListener.onFingerDown(mLastTouchX, mLastTouchY);
                break;
            case MotionEvent.ACTION_MOVE: {
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                // 首先得出移动距离dx、dy，这个距离用于拖动手势
                final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }

                mLastTouchX = x;
                mLastTouchY = y;

                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (mIsDragging && ev.getPointerCount() == 1 && pointerId == mActivePointerId) {
//                if (mIsDragging) {
                    mListener.onDrag(dx, dy, x, y, false);

                    if (null != mVelocityTracker) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mListener.onFingerCancel();
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);

                        // 这里主要处理松开手后的惯性滑动以及释放 VelocityTracker ，判断是否要惯性滑动，要看 x 轴和 y 轴的速度，VelocityTracker 在获取速度前要先调用 computeCurrentVelocity(int units) 计算速度，computeCurrentVelocity(int units) 方法的参数是单位，1表示1ms，1000表示1s
                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity(), vY = mVelocityTracker
                                .getYVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX,
                                    -vY, false);
                        }
                    }
                }

                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mListener.onFingerUp(mLastTouchX, mLastTouchY);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                // 多指触摸抬起其中一个手指，因为 mLastTouchX 在之前一直存的是第一个手指的坐标，所以这里只要判断是不是第一个手指抬起，如果是第一个手指抬起，就更新到下一个手指的坐标
                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
                break;
        }

        mActivePointerIndex = ev
                .findPointerIndex(mActivePointerId != INVALID_POINTER_ID ? mActivePointerId
                        : 0);
        return true;
    }
}

