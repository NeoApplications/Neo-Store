package com.google.android.apps.nexuslauncher.qsb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherRootView.WindowStateListener;
import com.android.launcher3.LauncherState;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.statemanager.StateManager.StateListener;

public class QsbAnimationController implements WindowStateListener, StateListener<LauncherState> {
    private final Launcher mLauncher;
    public boolean mQsbHasFocus;
    AnimatorSet mAnimatorSet;
    private boolean mSearchRequested;

    public QsbAnimationController(Launcher launcher) {
        mLauncher = launcher;
        mLauncher.getStateManager().addStateListener(this);
        mLauncher.getRootView().setWindowStateListener(this);
    }

    public final void playQsbAnimation() {
        if (mLauncher.hasWindowFocus()) {
            mSearchRequested = true;
        } else {
            openQsb();
        }
    }

    public AnimatorSet openQsb() {
        mSearchRequested = false;
        mQsbHasFocus = true;
        playAnimation(true, true);
        return mAnimatorSet;
    }

    public final void prepareAnimation(boolean hasFocus) {
        mSearchRequested = false;
        if (mQsbHasFocus) {
            mQsbHasFocus = false;
            playAnimation(false, hasFocus);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus || !mSearchRequested) {
            if (hasFocus) {
                prepareAnimation(true);
            }
            return;
        }
        openQsb();
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        prepareAnimation(false);
    }

    private void playAnimation(boolean checkHotseat, boolean hasFocus) {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
            mAnimatorSet = null;
        }
        View view = mLauncher.getDragLayer();
        if (mLauncher.isInState(LauncherState.ALL_APPS)) {
            view.setAlpha(1.0f);
            view.setTranslationY(0.0f);
            return;
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (animation == mAnimatorSet) {
                    mAnimatorSet = null;
                }
            }
        });
        if (checkHotseat) {
            mAnimatorSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 0f));
            Animator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, (float) ((-mLauncher.getHotseat().getHeight()) / 2));
            animator.setInterpolator(Interpolators.ACCEL);
            mAnimatorSet.play(animator);
        } else {
            mAnimatorSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 1f));
            Animator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f);
            animator.setInterpolator(Interpolators.DEACCEL);
            mAnimatorSet.play(animator);
        }
        mAnimatorSet.setDuration(200);
        mAnimatorSet.start();
        if (!hasFocus) {
            mAnimatorSet.end();
        }
    }

    public void onStateTransitionComplete(LauncherState launcherState) {
        reattachFocus(launcherState);
    }

    private void reattachFocus(LauncherState launcherState) {
        if (mQsbHasFocus && launcherState != LauncherState.ALL_APPS && !mLauncher.hasWindowFocus()) {
            playAnimation(true, false);
        }
    }
}