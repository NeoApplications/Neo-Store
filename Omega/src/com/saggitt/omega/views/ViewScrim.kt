/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.views

import android.graphics.Canvas
import android.util.Property
import android.view.View
import com.android.launcher3.R

/**
 * A utility class that can be used to draw a scrim behind a view
 */
abstract class ViewScrim<T : View?> protected constructor(protected val mView: T) {
    protected var mProgress = 0f
    fun attach() {
        mView!!.setTag(R.id.view_scrim, this)
    }

    fun setProgress(progress: Float) {
        if (mProgress != progress) {
            mProgress = progress
            onProgressChanged()
            invalidate()
        }
    }

    abstract fun draw(canvas: Canvas, width: Int, height: Int)
    protected open fun onProgressChanged() {}
    fun invalidate() {
        val parent = mView!!.parent
        if (parent != null) {
            (parent as View).invalidate()
        }
    }

    companion object {
        var PROGRESS: Property<ViewScrim<*>, Float> =
            object : Property<ViewScrim<*>, Float>(java.lang.Float.TYPE, "progress") {
                override fun get(viewScrim: ViewScrim<*>): Float {
                    return viewScrim.mProgress
                }

                override fun set(`object`: ViewScrim<*>, value: Float) {
                    `object`.setProgress(value)
                }
            }

        operator fun get(view: View): ViewScrim<View>? {
            return view.getTag(R.id.view_scrim) as ViewScrim<View>?
        }
    }
}