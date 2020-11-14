/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.views;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.views.AbstractSlideInView;

public class CenterFloatingView extends AbstractSlideInView implements Insettable {
    private static final int DEFAULT_CLOSE_DURATION = 200;
    private final Rect mInsets;

    public CenterFloatingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CenterFloatingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
        mInsets = new Rect();
        mContent = this;
    }

    public static CenterFloatingView inflate(Launcher launcher) {
        return (CenterFloatingView) launcher.getLayoutInflater()
                .inflate(R.layout.base_center_sheet, launcher.getDragLayer(), false);
    }

    public void show(View view, boolean animate) {
        ((ViewGroup) findViewById(R.id.sheet_contents)).addView(view);

        mLauncher.getDragLayer().addView(this);
        mIsOpen = false;
        animateOpen(animate);
    }

    @Override
    public void setInsets(Rect insets) {
        // Extend behind left, right, and bottom insets.
        int leftInset = insets.left - mInsets.left;
        int rightInset = insets.right - mInsets.right;
        int bottomInset = insets.bottom - mInsets.bottom;
        mInsets.set(insets);

        setPadding(getPaddingLeft() + leftInset, getPaddingTop(),
                getPaddingRight() + rightInset, getPaddingBottom() + bottomInset);
    }

    @Override
    protected boolean isOfType(@FloatingViewType int type) {
        return (type & TYPE_SETTINGS_SHEET) != 0;
    }

    private void animateOpen(boolean animate) {
        if (mIsOpen || mOpenCloseAnimator.isRunning()) {
            return;
        }
        mIsOpen = true;
        mOpenCloseAnimator.setValues(
                PropertyValuesHolder.ofFloat(TRANSLATION_SHIFT, TRANSLATION_SHIFT_OPENED));
        mOpenCloseAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        if (!animate) {
            mOpenCloseAnimator.setDuration(0);
        }
        mOpenCloseAnimator.start();
    }

    @Override
    protected void handleClose(boolean animate) {
        handleClose(animate, DEFAULT_CLOSE_DURATION);
    }

    @Override
    public final void logActionCommand(int command) {
    }
}
