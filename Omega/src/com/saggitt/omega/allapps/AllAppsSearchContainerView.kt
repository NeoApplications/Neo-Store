/*
 *     Copyright (C) 2019 Lawnchair Team.
 *
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
package com.saggitt.omega.allapps

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import com.android.launcher3.allapps.LauncherAllAppsContainerView
import com.google.android.apps.nexuslauncher.qsb.AllAppsQsbLayout

class AllAppsSearchContainerView @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LauncherAllAppsContainerView(context, attrs, defStyleAttr) {
    private var mClearQsb = false

    override fun dispatchDraw(canvas: Canvas) {
        val searchView = searchView
        if (mClearQsb && searchView is AllAppsQsbLayout) {
            val left = (searchView.left + searchView.translationX).toInt()
            val top = (searchView.top + searchView.translationY).toInt()
            val right = left + searchView.width + 1
            val bottom = top + searchView.height + 1
            canvas.saveLayer(left.toFloat(), 0f, right.toFloat(), bottom.toFloat(), null)
        }
        super.dispatchDraw(canvas)
    }

    override fun setTranslationY(translationY: Float) {
        super.setTranslationY(translationY)
        (searchView as BlurQsbLayout).invalidateBlur()
    }
}