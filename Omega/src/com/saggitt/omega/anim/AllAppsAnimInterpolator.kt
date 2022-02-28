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

package com.saggitt.omega.anim

import android.graphics.Interpolator

class AllAppsAnimInterpolator(f: Float, f2: Float, f3: Float, f4: Float) : Interpolator(0, 0) {
    private var f21581a: Float = f
    private var f21582b: Float = f2
    private var f21583c: Float = f3
    private var f21584d: Float = f4

    private fun m7510a(f: Float, f2: Float, f3: Float): Float {
        val f4 = 1.0f - f
        return (f * f * f) + (f3 * 3.0f * f * f * f4) + (f2 * 3.0f * f * f4 * f4)
    }

    fun getInterpolation(input: Float): Float {
        var f2 = 0.0f
        if (input == 0.0f || input == 1.0f) {
            return input
        }

        var i = 0
        while (i < 1000 && m7510a(f2, f21581a, f21583c) < input) {
            f2 += 0.001f
            i++
        }
        return m7510a(f2, f21582b, f21584d)
    }

    companion object {
        val pagedOverScrollAnim = AllAppsAnimInterpolator(0.1f, 0.9f, 0.2f, 1.0f)
    }
}
