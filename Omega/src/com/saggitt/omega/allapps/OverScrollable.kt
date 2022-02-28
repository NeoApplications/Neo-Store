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

import android.graphics.Interpolator
import com.saggitt.omega.anim.AllAppsAnimInterpolator

interface OverScrollable {

    class OverScrollParam {
        var mFriction = 1.0f
        var mOverScrollInterpolator: Interpolator = AllAppsAnimInterpolator.pagedOverScrollAnim
    }

    fun getOvScrollParam(): OverScrollParam

    companion object {
        val DefaultOverScrollParam: OverScrollParam = OverScrollParam()
    }
}