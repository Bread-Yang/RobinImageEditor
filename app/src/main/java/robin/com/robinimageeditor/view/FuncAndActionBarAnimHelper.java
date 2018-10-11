package robin.com.robinimageeditor.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import java.util.ArrayList;

import robin.com.robinimageeditor.utils.MatrixUtils;

/**
 * Created by Robin Yang on 12/28/17.
 */

public class FuncAndActionBarAnimHelper implements ActionFrameLayout.ActionListener {

    private ActionFrameLayout layerActionView;
    private View editorBar;
    private View funcView;
    private Context activityContext;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private HideOrShowRunnable hideRunnable = new HideOrShowRunnable(false);
    private HideOrShowRunnable showRunnable = new HideOrShowRunnable(true);

    private boolean interceptDirtyAnimation;
    private boolean mDirtyAnimating = false;
    private boolean mDirtyScreen = true;
    private boolean mMoveActionHandled;

    private ArrayList<OnFunBarAnimationListener> mFunBarAnimateListeners = new ArrayList<>();

    public interface OnFunBarAnimationListener {
        void onFunBarAnimate(boolean show);
    }

    public FuncAndActionBarAnimHelper(ActionFrameLayout layerActionView, View editorBar,
                                      View funcView, Context activityContext) {
        this.layerActionView = layerActionView;
        this.editorBar = editorBar;
        this.funcView = funcView;
        this.activityContext = activityContext;
        init();
    }

    private void init() {
        layerActionView.setActionListener(this);
    }

    @Override
    public void actionMove() {
        if (interceptDirtyAnimation) {
            return;
        }
        if (mDirtyScreen && !mMoveActionHandled) {
            if (!mDirtyAnimating) {
                mMoveActionHandled = true;
                mHandler.postDelayed(hideRunnable, 300);
            }
        }
    }

    @Override
    public void actionUp() {
        if (interceptDirtyAnimation) {
            return;
        }
        mMoveActionHandled = false;
        if (mDirtyScreen) {
            mHandler.removeCallbacks(hideRunnable);
            if (!mDirtyAnimating) {
                mHandler.postDelayed(hideRunnable, 300);
            }
        } else {
            mHandler.removeCallbacks(showRunnable);
            if (!mDirtyAnimating) {
                mHandler.postDelayed(showRunnable, 300);
            }
        }
    }

    public void showOrHideFuncAndBarView(boolean show) {
        showOrHideFuncAndBarView(show, null);
    }

    public void showOrHideFuncAndBarView(boolean show, final AnimatorListenerAdapter listenerAdapter) {
        if (mDirtyScreen == show) {
            return;
        }
        mDirtyScreen = show;
        if (mDirtyScreen) {
            mHandler.removeCallbacks(hideRunnable);
        } else {
            mHandler.removeCallbacks(showRunnable);
        }
        mDirtyAnimating = true;
        invokeAnimateListener(show);

        int barHeight = editorBar.getHeight();
        int funcHeight = funcView.getHeight();
        int fromBarHeight, toBarHeight, fromFuncHeight, toFuncHeight;
        if (!show) {
            fromBarHeight = 0;
            toBarHeight = -barHeight;

            fromFuncHeight = 0;
            toFuncHeight = funcHeight;
        } else {
            fromBarHeight = -barHeight;
            toBarHeight = 0;

            fromFuncHeight = funcHeight;
            toFuncHeight = 0;
        }
        ObjectAnimator barAnimator = ObjectAnimator.ofFloat(editorBar, "translationY", fromBarHeight, toBarHeight);
        ObjectAnimator funcAnimator = ObjectAnimator.ofFloat(funcView, "translationY", fromFuncHeight, toFuncHeight);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(barAnimator, funcAnimator);
        set.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                mDirtyAnimating = false;
                if (listenerAdapter != null) {
                    listenerAdapter.onAnimationEnd(animation);
                }
            }
        });
        set.setDuration(300);
        set.start();
        if (show) {
            MatrixUtils.showStatusBar((Activity) activityContext);
        } else {
            MatrixUtils.hideStatusBar((Activity) activityContext);
        }
    }

    private void invokeAnimateListener(boolean show) {
        for (int i = 0; i < mFunBarAnimateListeners.size(); i++) {
            OnFunBarAnimationListener listener = mFunBarAnimateListeners.get(i);
            listener.onFunBarAnimate(show);
        }
    }

    public boolean isInterceptDirtyAnimation() {
        return interceptDirtyAnimation;
    }

    public void setInterceptDirtyAnimation(boolean interceptDirtyAnimation) {
        this.interceptDirtyAnimation = interceptDirtyAnimation;
    }

    class HideOrShowRunnable implements Runnable {

        boolean show;

        public HideOrShowRunnable(boolean show) {
            this.show = show;
        }

        @Override
        public void run() {
            showOrHideFuncAndBarView(show);
        }
    }

}
