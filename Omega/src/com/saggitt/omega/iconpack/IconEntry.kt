/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.iconpack

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Process
import android.os.UserHandle
import com.android.launcher3.icons.ClockDrawableWrapper

data class IconEntry(val iconPack: IconPack, val name: String) {

    fun getDrawable(iconDpi: Int, user: UserHandle): Drawable? {
        val drawable = iconPack.getIcon(this, iconDpi) ?: return null
        val clockMetadata = if (user == Process.myUserHandle()) iconPack.getClock(this) else null
        if (clockMetadata != null) {
            val clockDrawable = ClockDrawableWrapper.forMeta(Build.VERSION.SDK_INT, clockMetadata) {
                drawable
            }
            if (clockDrawable != null) {
                return clockDrawable
            }
        }
        return drawable
    }
}

data class CalendarIconEntry(val iconPack: IconPack, val prefix: String) {
    fun getIconEntry(day: Int) = IconEntry(iconPack, "$prefix${day + 1}")
}
