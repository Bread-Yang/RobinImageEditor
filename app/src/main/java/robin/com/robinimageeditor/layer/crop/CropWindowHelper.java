package robin.com.robinimageeditor.layer.crop;

import android.graphics.RectF;
import android.view.MotionEvent;

/**
 * Created by Robin Yang on 1/17/18.
 */

public class CropWindowHelper {

    enum Type {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        LEFT,
        TOP,
        RIGHT,
        BOTTOM,
        CENTER
    }

    private float targetRadius;

    private RectF mEdges = new RectF();
    private Type mPressedCropType;
    private RectF mFeedBackEdges = new RectF();
    /*cropWindow,size*/
    float minCropWindowHeight;
    float maxCropWindowHeight = Float.MAX_VALUE;
    float minCropWindowWidth;
    float maxCropWindowWidth = Float.MAX_VALUE;

    public CropWindowHelper(float targetRadius) {
        this.targetRadius = targetRadius;
    }

    public boolean interceptTouchEvent(MotionEvent event) {
        mPressedCropType = getPressedCropType(event.getX(), event.getY(), targetRadius);
        return mPressedCropType != null;
    }

    public void resetTouchEvent(MotionEvent event) {
        mPressedCropType = null;
    }

    public boolean checkCropWindowBounds(RectF bounds) {
        float offsetX = 0f;
        float offsetY = 0f;
        RectF tempRect = new RectF(mEdges);
        if (mEdges.left < bounds.left) {
            offsetX = bounds.left - mEdges.left;
            tempRect.left = bounds.left;
        }
        if (mEdges.top < bounds.top) {
            offsetY = bounds.top - mEdges.top;
            tempRect.top = bounds.top;
        }
        if (mEdges.right > bounds.right) {
            offsetX = bounds.right - mEdges.right;
            tempRect.right = bounds.right;
        }
        if (mEdges.bottom > bounds.bottom) {
            offsetY = bounds.bottom - mEdges.bottom;
            tempRect.bottom = bounds.bottom;
        }
        mEdges.offset(offsetX, offsetY);
        if (!bounds.contains(mEdges)) {
            mEdges.set(tempRect);
        }
        return offsetX != 0f || offsetY != 0f;
    }

    public boolean onCropWindowDrag(float dx, float dy, RectF bound) {
        boolean hasDrag = false;
        if (mPressedCropType == Type.CENTER) {
            hasDrag = moveCenter(mEdges, dx, dy, bound);
        } else {
            hasDrag = moveOtherCropType(mEdges, dx, dy, bound, mPressedCropType);
        }
        return hasDrag;
    }

    private boolean moveOtherCropType(RectF rect, float dx, float dy, RectF bounds, Type pressedCropType) {
        if (pressedCropType == null) {
            return false;
        }
        switch (pressedCropType) {
            case LEFT:
                return moveLeft(rect, dx, bounds);
            case RIGHT:
                return moveRight(rect, dx, bounds);
            case TOP:
                return moveTop(rect, dy, bounds);
            case BOTTOM:
                return moveBottom(rect, dy, bounds);
            case TOP_LEFT:
                return moveTop(rect, dy, bounds) && moveLeft(rect, dx, bounds);
            case TOP_RIGHT:
                return moveTop(rect, dy, bounds) && moveRight(rect, dx, bounds);
            case BOTTOM_LEFT:
                return moveBottom(rect, dy, bounds) && moveLeft(rect, dx, bounds);
            case BOTTOM_RIGHT:
                return moveBottom(rect, dy, bounds) && moveRight(rect, dx, bounds);
            default:
                return false;
        }
    }

    //region: move center,left,top,right,bottom constraint
    private boolean moveCenter(RectF rect, float dx, float dy, RectF bounds) {
        float offsetX = dx;
        float offsetY = dy;
        if (rect.left + dx <= bounds.left) {
            offsetX = bounds.left - rect.left;
        }
        if (rect.right + dx >= bounds.right) {
            offsetX = bounds.right - rect.right;
        }
        if (rect.top + dy <= bounds.top) {
            offsetY = bounds.top - rect.top;
        }
        if (rect.bottom + dy >= bounds.bottom) {
            offsetY = bounds.bottom - rect.bottom;
        }
        rect.offset(offsetX, offsetY);
        return offsetX != 0f || offsetY != 0f;
    }

    private boolean moveLeft(RectF rect, float dx, RectF bounds) {
        float newLeft = rect.left + dx;
        if (rect.right - newLeft > maxCropWindowWidth) {
            newLeft = rect.right - maxCropWindowWidth;
        }
        if (rect.right - newLeft < minCropWindowWidth) {
            newLeft = rect.right - minCropWindowWidth;
        }
        if (newLeft < bounds.left) {
            newLeft = bounds.left;
        }
        boolean changed = rect.left != newLeft;
        rect.left = newLeft;
        return changed;
    }

    private boolean moveTop(RectF rect, float dy, RectF bounds) {
        float newTop = rect.top + dy;
        if (rect.bottom - newTop > maxCropWindowHeight) {
            newTop = rect.bottom - maxCropWindowHeight;
        }
        if (rect.bottom - newTop < minCropWindowHeight) {
            newTop = rect.bottom - minCropWindowHeight;
        }
        if (newTop < bounds.top) {
            newTop = bounds.top;
        }
        boolean changed = rect.top != newTop;
        rect.top = newTop;
        return changed;
    }

    private boolean moveRight(RectF rect, float dx, RectF bounds) {
        float newRight = rect.right + dx;
        if (newRight - rect.left > maxCropWindowWidth) {
            newRight = rect.left + maxCropWindowWidth;
        }
        if (newRight - rect.left < minCropWindowWidth) {
            newRight = rect.left + minCropWindowWidth;
        }
        if (newRight > bounds.right) {
            newRight = bounds.right;
        }
        boolean changed = rect.right != newRight;
        rect.right = newRight;
        return changed;
    }

    private boolean moveBottom(RectF rect, float dy, RectF bounds) {
        float newBottom = rect.bottom + dy;
        if (newBottom - rect.top > maxCropWindowHeight) {
            newBottom = rect.top + maxCropWindowHeight;
        }
        if (newBottom - rect.top < minCropWindowHeight) {
            newBottom = rect.top + minCropWindowHeight;
        }
        if (newBottom > bounds.bottom) {
            newBottom = bounds.bottom;
        }
        boolean changed = rect.bottom != newBottom;
        rect.bottom = newBottom;
        return changed;
    }

    //region: press crop type.

    private Type getPressedCropType(float x, float y, float targetRadius) {
        Type moveType = null;
        if (isInCornerTargetZone(x, y, mEdges.left, mEdges.top, targetRadius)) {
            moveType = Type.TOP_LEFT;
        } else if (isInCornerTargetZone(x, y, mEdges.right, mEdges.top, targetRadius)) {
            moveType = Type.TOP_RIGHT;
        } else if (isInCornerTargetZone(x, y, mEdges.left, mEdges.bottom, targetRadius)) {
            moveType = Type.BOTTOM_LEFT;
        } else if (isInCornerTargetZone(x, y, mEdges.right, mEdges.bottom, targetRadius)) {
            moveType = Type.BOTTOM_RIGHT;
        } else if (isInHorizontalTargetZone(x, y, mEdges.left, mEdges.right, mEdges.top, targetRadius)) {
            moveType = Type.TOP;
        } else if (isInHorizontalTargetZone(x, y, mEdges.left, mEdges.right, mEdges.bottom, targetRadius)) {
            moveType = Type.BOTTOM;
        } else if (isInVerticalTargetZone(x, y, mEdges.left, mEdges.top, mEdges.bottom, targetRadius)) {
            moveType = Type.LEFT;
        } else if (isInVerticalTargetZone(x, y, mEdges.right, mEdges.top, mEdges.bottom, targetRadius)) {
            moveType = Type.RIGHT;
        } else if (isInCenterTargetZone(x, y, mEdges.left, mEdges.top, mEdges.right, mEdges.bottom)) {
            moveType = Type.CENTER;
        }
        return moveType;
    }

    private boolean isInCornerTargetZone(float x, float y, float handleX, float handleY, float targetRadius) {
        return Math.abs(x - handleX) <= targetRadius && Math.abs(y - handleY) <= targetRadius;
    }

    private boolean isInHorizontalTargetZone(float x, float y, float handleXStart,
                                             float handleXEnd, float handleY, float targetRadius) {
        return x > handleXStart && x < handleXEnd && Math.abs(y - handleY) <= targetRadius;
    }

    private boolean isInCenterTargetZone(float x, float y, float left, float top, float right, float bottom) {
        return x > left && x < right && y > top && y < bottom;
    }

    private boolean isInVerticalTargetZone(float x, float y, float handleX, float handleYStart,
                                           float handleYEnd, float targetRadius) {
        return Math.abs(x - handleX) <= targetRadius && y > handleYStart && y < handleYEnd;
    }

    public RectF getEdge() {
        mFeedBackEdges.set(mEdges);
        return mFeedBackEdges;
    }

    public void setEdge(RectF edges) {
        this.mEdges.set(edges);
    }
}
