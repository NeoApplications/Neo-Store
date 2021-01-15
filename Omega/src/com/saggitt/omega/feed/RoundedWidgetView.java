/*
 *  This file is part of Omega Launcher
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

package com.saggitt.omega.feed;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.android.launcher3.CheckLongPressHelper;
import com.android.launcher3.R;

public class RoundedWidgetView extends AppWidgetHostView {

    private final Path stencilPath = new Path();
    private float cornerRadius;
    private CheckLongPressHelper mLongPressHelper;
    private Context mContext;
    private static final String TAG = "RoundedWidgetView";
    private ImageView resizeBorder;

    private OnTouchListener _onTouchListener;
    private OnLongClickListener _longClick;
    private long _down;
    private boolean mChildrenFocused;

    private boolean activated = false;

    public RoundedWidgetView(Context context) {
        super(context);
        this.mContext = context;
        this.cornerRadius = 12;//context.getResources().getDimensionPixelSize(R.dimen.corner_radius);
        mLongPressHelper = new CheckLongPressHelper(this);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // compute the path
        stencilPath.reset();
        stencilPath.addRoundRect(0, 0, w, h, cornerRadius, cornerRadius, Path.Direction.CW);
        stencilPath.close();

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int save = canvas.save();
        canvas.clipPath(stencilPath);
        super.dispatchDraw(canvas);
        canvas.restoreToCount(save);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(TAG, "onInterceptTouchEvent() called with: ev = [" + ev.getAction() + "]");

        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mLongPressHelper.cancelLongPress();
        }

        // Consume any touch events for ourselves after longpress is triggered
        if (mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress();
            return true;
        }

        // Watch for longpress events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLongPressHelper.postCheckForLongPress();
                break;
            }

            case MotionEvent.ACTION_UP:
                mLongPressHelper.cancelLongPress();
                break;

            case MotionEvent.ACTION_CANCEL:
                mLongPressHelper.cancelLongPress();
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;

    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();

        mLongPressHelper.cancelLongPress();
    }

    @Override
    public int getDescendantFocusability() {
        return mChildrenFocused ? ViewGroup.FOCUS_BEFORE_DESCENDANTS
                : ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction,
                                  @Nullable Rect previouslyFocusedRect) {
        if (gainFocus) {
            mChildrenFocused = false;
            dispatchChildFocus(false);
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        dispatchChildFocus(mChildrenFocused && focused != null);
        if (focused != null) {
            focused.setFocusableInTouchMode(false);
        }
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        return mChildrenFocused;
    }

    private void dispatchChildFocus(boolean childIsFocused) {
        // The host view's background changes when selected, to indicate the focus is inside.
        setSelected(childIsFocused);
    }

    public void addBorder() {
        if (resizeBorder != null) {
            removeBorder();
        }
        resizeBorder = new ImageView(mContext);
        resizeBorder.setImageResource(R.drawable.widget_resize_frame);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        resizeBorder.setLayoutParams(layoutParams);
        addView(resizeBorder);
        activated = true;
    }

    public void removeBorder() {
        if (resizeBorder != null) {
            removeView(resizeBorder);
            resizeBorder = null;
            activated = false;
        }
    }

    public boolean isWidgetActivated() {
        return activated;
    }
}
