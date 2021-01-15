/*
 *  This file is part of Omega Launcher.
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.qsb;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherRootView;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager;
import com.android.launcher3.anim.Interpolators;

public class QsbAnimationController implements LauncherRootView.WindowStateListener, LauncherStateManager.StateListener {
    private final Launcher mLauncher;
    public boolean mGoogleHasFocus;
    AnimatorSet mAnimatorSet;
    private boolean mSearchRequested;

    public QsbAnimationController(Launcher launcher) {
        mLauncher = launcher;
        mLauncher.getStateManager().addStateListener(this);
        mLauncher.getRootView().setWindowStateListener(this);
    }

    public final void dZ() {
        if (mLauncher.hasWindowFocus()) {
            mSearchRequested = true;
        } else {
            openQsb();
        }
    }

    public AnimatorSet openQsb() {
        mSearchRequested = false;
        playAnimation(mGoogleHasFocus = true, true);
        return mAnimatorSet;
    }

    public final void z(boolean z) {
        mSearchRequested = false;
        if (mGoogleHasFocus) {
            mGoogleHasFocus = false;
            playAnimation(false, z);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus || !mSearchRequested) {
            if (hasFocus) {
                z(true);
            }
            return;
        }
        openQsb();
    }

    @Override
    public void onWindowVisibilityChanged(int visibility) {
        z(false);
    }

    private void playAnimation(boolean z, boolean z2) {
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
        if (z) {
            mAnimatorSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 0f));
            Animator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, (float) ((-mLauncher.getHotseat().getHeight()) / 2));
            animator.setInterpolator(Interpolators.ACCEL);
            mAnimatorSet.play(animator);
            mAnimatorSet.setDuration(200);
        } else {
            mAnimatorSet.play(ObjectAnimator.ofFloat(view, View.ALPHA, 1f));
            Animator animator = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f);
            animator.setInterpolator(Interpolators.DEACCEL);
            mAnimatorSet.play(animator);
            mAnimatorSet.setDuration(200);
        }
        mAnimatorSet.start();
        if (!z2) {
            mAnimatorSet.end();
        }
    }

    public void onStateTransitionStart(LauncherState launcherState) {
    }

    public void onStateTransitionComplete(LauncherState launcherState) {
        a(launcherState);
    }

    private void a(LauncherState launcherState) {
        if (mGoogleHasFocus && launcherState != LauncherState.ALL_APPS && !mLauncher.hasWindowFocus()) {
            playAnimation(true, false);
        }
    }
}