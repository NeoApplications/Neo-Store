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
package com.saggitt.omega.feed

import android.appwidget.AppWidgetHostView
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Rect
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.launcher3.CheckLongPressHelper
import com.android.launcher3.R

class RoundedWidgetView(private val mContext: Context) : AppWidgetHostView(
    mContext
) {
    private val stencilPath = Path()
    private val cornerRadius = 12f
    private val mLongPressHelper: CheckLongPressHelper = CheckLongPressHelper(this)
    private var resizeBorder: ImageView? = null
    private val _onTouchListener: OnTouchListener? = null
    private val _longClick: OnLongClickListener? = null
    private val _down: Long = 0
    private var mChildrenFocused = false
    var isWidgetActivated = false
        private set

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // compute the path
        stencilPath.reset()
        stencilPath.addRoundRect(
            0f,
            0f,
            w.toFloat(),
            h.toFloat(),
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )
        stencilPath.close()
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(stencilPath)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        Log.d(TAG, "onInterceptTouchEvent() called with: ev = [" + ev.action + "]")
        if (ev.action == MotionEvent.ACTION_DOWN) {
            mLongPressHelper.cancelLongPress()
        }

        // Consume any touch events for ourselves after longpress is triggered
        if (mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress()
            return true
        }
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                mLongPressHelper.postCheckForLongPress()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> mLongPressHelper.cancelLongPress()
        }

        // Otherwise continue letting touch events fall through to children
        return false
    }

    override fun cancelLongPress() {
        super.cancelLongPress()
        mLongPressHelper.cancelLongPress()
    }

    override fun getDescendantFocusability(): Int {
        return if (mChildrenFocused) FOCUS_BEFORE_DESCENDANTS else FOCUS_BLOCK_DESCENDANTS
    }

    override fun onFocusChanged(
        gainFocus: Boolean, direction: Int,
        previouslyFocusedRect: Rect?
    ) {
        if (gainFocus) {
            mChildrenFocused = false
            dispatchChildFocus(false)
        }
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }

    override fun requestChildFocus(child: View, focused: View?) {
        super.requestChildFocus(child, focused)
        dispatchChildFocus(mChildrenFocused && focused != null)
        if (focused != null) {
            focused.isFocusableInTouchMode = false
        }
    }

    override fun dispatchUnhandledMove(focused: View, direction: Int): Boolean {
        return mChildrenFocused
    }

    private fun dispatchChildFocus(childIsFocused: Boolean) {
        // The host view's background changes when selected, to indicate the focus is inside.
        isSelected = childIsFocused
    }

    fun addBorder() {
        if (resizeBorder != null) {
            removeBorder()
        }
        resizeBorder = ImageView(mContext)
        resizeBorder!!.setImageResource(R.drawable.widget_resize_frame)
        val layoutParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        resizeBorder!!.layoutParams = layoutParams
        addView(resizeBorder)
        isWidgetActivated = true
    }

    fun removeBorder() {
        if (resizeBorder != null) {
            removeView(resizeBorder)
            resizeBorder = null
            isWidgetActivated = false
        }
    }

    companion object {
        private const val TAG = "RoundedWidgetView"
    }

    init {
        //context.getResources().getDimensionPixelSize(R.dimen.corner_radius);
    }
}