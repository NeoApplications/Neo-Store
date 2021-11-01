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
package com.saggitt.omega.allapps

import android.content.Context
import androidx.core.graphics.ColorUtils
import com.android.launcher3.allapps.AppInfoComparator
import com.android.launcher3.model.data.AppInfo

class AppColorComparator(context: Context?) : AppInfoComparator(context) {
    override fun compare(a: AppInfo, b: AppInfo): Int {
        val hslA = FloatArray(3)
        val hslB = FloatArray(3)
        ColorUtils.colorToHSL(a.iconColor, hslA)
        ColorUtils.colorToHSL(b.iconColor, hslB)
        val h2A = remapHue(hslA[0])
        val h2B = remapHue(hslB[0])
        var s2A = remap(hslA[1])
        var s2B = remap(hslB[1])
        var l2A = remap(hslA[2])
        var l2B = remap(hslB[2])
        if (h2A % 2 == 1) {
            s2A = REPETITIONS - s2A
            l2A = REPETITIONS - l2A
        }
        if (h2B % 2 == 1) {
            s2B = REPETITIONS - s2B
            l2B = REPETITIONS - l2B
        }
        var result = h2A.compareTo(h2B)
        if (result != 0) {
            return result
        }
        result = l2A.compareTo(l2B)
        if (result != 0) {
            return result
        }
        result = s2A.compareTo(s2B)
        return if (result != 0) {
            result
        } else super.compare(a, b)
    }

    companion object {
        const val REPETITIONS = 6

        @JvmStatic
        fun remapHue(hue: Float): Int {
            return (hue / 360 * REPETITIONS).toInt()
        }

        @JvmStatic
        fun remap(value: Float): Int {
            return (value * REPETITIONS).toInt()
        }
    }
}