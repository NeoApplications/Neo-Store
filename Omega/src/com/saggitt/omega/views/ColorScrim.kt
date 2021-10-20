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
import android.graphics.Color
import android.view.View
import android.view.animation.Interpolator
import androidx.core.graphics.ColorUtils
import com.android.launcher3.R.integer
import com.android.launcher3.anim.Interpolators
import com.android.launcher3.uioverrides.WallpaperColorInfo
import kotlin.math.roundToInt

class ColorScrim(view: View?, private val mColor: Int, private val mInterpolator: Interpolator) :
    ViewScrim<View?>(view) {
    private var mCurrentColor = 0
    override fun onProgressChanged() {
        mCurrentColor = ColorUtils.setAlphaComponent(
            mColor,
            (mInterpolator.getInterpolation(mProgress) * Color.alpha(mColor)).roundToInt()
        )
    }

    override fun draw(canvas: Canvas, width: Int, height: Int) {
        if (mProgress > 0) {
            canvas.drawColor(mCurrentColor)
        }
    }

    companion object {
        fun createExtractedColorScrim(view: View): ColorScrim {
            val colors = WallpaperColorInfo.getInstance(view.context)
            val alpha = view.resources.getInteger(integer.extracted_color_gradient_alpha)
            val scrim = ColorScrim(
                view, ColorUtils.setAlphaComponent(
                    colors.secondaryColor, alpha
                ), Interpolators.LINEAR
            )
            scrim.attach()
            return scrim
        }
    }
}