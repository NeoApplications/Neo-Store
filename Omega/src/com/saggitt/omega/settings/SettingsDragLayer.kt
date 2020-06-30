/*
 *
 *  *
 *  *  * Copyright (c) 2020 Omega Launcher
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.saggitt.omega.settings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.android.launcher3.InsettableFrameLayout
import com.android.launcher3.Utilities
import com.android.launcher3.util.TouchController
import com.saggitt.omega.util.forEachChildReversed
import com.saggitt.omega.views.ViewScrim

class SettingsDragLayer(context: Context, attrs: AttributeSet?) : InsettableFrameLayout(context, attrs) {

    private val mTmpXY = FloatArray(2)
    private val mHitRect = Rect()

    private var activeController: TouchController? = null

    fun getTopOpenView(): SettingsBottomSheet? {
        forEachChildReversed {
            if (it is SettingsBottomSheet) {
                return it
            }
        }
        return null
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return findActiveController(ev)
    }

    private fun findActiveController(ev: MotionEvent): Boolean {
        activeController = null
        getTopOpenView()?.let {
            if (it.onControllerInterceptTouchEvent(ev)) {
                activeController = it
                return true
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        activeController?.let {
            return it.onControllerTouchEvent(event)
        }
        return findActiveController(event)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        ViewScrim.get(child)?.draw(canvas, width, height)
        return super.drawChild(canvas, child, drawingTime)
    }

    override fun fitSystemWindows(insets: Rect): Boolean {
        setInsets(insets)
        return true
    }

    fun isEventOverView(view: View, ev: MotionEvent): Boolean {
        getDescendantRectRelativeToSelf(view, mHitRect)
        return mHitRect.contains(ev.x.toInt(), ev.y.toInt())
    }

    fun getDescendantRectRelativeToSelf(descendant: View, r: Rect): Float {
        mTmpXY[0] = 0F
        mTmpXY[1] = 0F
        val scale = getDescendantCoordRelativeToSelf(descendant, mTmpXY)

        r.set(mTmpXY[0].toInt(), mTmpXY[1].toInt(),
                (mTmpXY[0] + scale * descendant.measuredWidth).toInt(),
                (mTmpXY[1] + scale * descendant.measuredHeight).toInt())
        return scale
    }

    private fun getDescendantCoordRelativeToSelf(descendant: View, coord: FloatArray): Float {
        return getDescendantCoordRelativeToSelf(descendant, coord, false)
    }

    private fun getDescendantCoordRelativeToSelf(descendant: View, coord: FloatArray,
                                                 includeRootScroll: Boolean): Float {
        return Utilities.getDescendantCoordRelativeToAncestor(descendant, this,
                coord, includeRootScroll)
    }
}
