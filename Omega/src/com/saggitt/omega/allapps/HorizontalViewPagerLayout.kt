/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.allapps

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.RelativeLayout
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.pageindicators.PageIndicatorDots
import kotlin.math.abs

class HorizontalViewPagerLayout(context: Context, attrs: AttributeSet? = null) :
    RelativeLayout(context, attrs), OverScrollable {
    private var mIsBeingDragged = false
    private var mIsSwipeDown = false
    private var mIsSwipeLeftRight = false
    private var mIsSwipeUp = false

    private var mMotionBeginX = 0f
    private var mMotionBeginY = 0f
    var mTouchSlop = 0
    var mViewPager: AllAppsPagedView

    init {
        val layoutParams = LayoutParams(-1, -1)
        mViewPager = AllAppsPagedView(context)
        addView(mViewPager, layoutParams)
        mTouchSlop = ViewConfiguration.get(getContext()).scaledTouchSlop
        getOvScrollParam().mFriction = 0.14f
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<PageIndicatorDots>(R.id.all_apps_indicator).apply {
            backgroundTintList =
                ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)
        }
    }

    override fun onInterceptTouchEvent(motionEvent: MotionEvent): Boolean {
        val action = motionEvent.action
        mIsSwipeUp = false
        mIsSwipeDown = false
        mIsSwipeLeftRight = false

        if (action == MotionEvent.ACTION_UP) {
            mMotionBeginX = motionEvent.x
            mMotionBeginY = motionEvent.y
            mIsBeingDragged = false
        } else if (action == MotionEvent.ACTION_MOVE) {
            val motionX = motionEvent.x - mMotionBeginX
            val motionY = motionEvent.y - mMotionBeginY
            val absX = abs(motionX)
            val absY = abs(motionY)
            if (!mIsSwipeDown) {
                mIsSwipeDown = y > mTouchSlop
            }
            if (!mIsSwipeUp) {
                mIsSwipeUp = y * (-1f) > mTouchSlop
            }
            if (!mIsSwipeLeftRight) {
                mIsSwipeLeftRight = absX > mTouchSlop
            }
            if (!mIsBeingDragged && absX > mTouchSlop && absX > absY) {
                if (canOverscrollAtStart() && x > 0.0f) {
                    mIsBeingDragged = true
                } else if (canOverScrollAtEnd() && x < 0.0f) {
                    mIsBeingDragged = true
                }
            }
        }
        if (!mIsSwipeLeftRight) {
            return mIsBeingDragged || mIsSwipeDown || mIsSwipeUp
        }
        return mIsBeingDragged
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val action = motionEvent.action
        val motionX: Float = motionEvent.x - mMotionBeginX

        if (action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false
        } else if (action == MotionEvent.ACTION_MOVE) {
            val y: Float = motionEvent.y - mMotionBeginY
            val abs: Float = abs(motionX)
            if (!mIsSwipeDown) {
                mIsSwipeDown = y > mTouchSlop.toFloat()
            }
            if (!mIsSwipeLeftRight) {
                mIsSwipeLeftRight = abs > mTouchSlop.toFloat()
            }
            if (!mIsSwipeDown) {
                scrollTo(-((getOvScrollParam().mFriction * motionX).toInt()), 0)
            }
        }

        return true
    }

    private fun canOverscrollAtStart(): Boolean {
        return mViewPager.currentPage == 0
    }

    private fun canOverScrollAtEnd(): Boolean {
        return mViewPager.currentPage == 1
    }

    fun getViewPager(): AllAppsPagedView {
        return mViewPager
    }

    override fun getOvScrollParam(): OverScrollable.OverScrollParam {
        return OverScrollable.DefaultOverScrollParam
    }
}