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

package com.saggitt.omega.util

import android.content.Context
import android.content.pm.LauncherActivityInfo
import com.android.launcher3.Utilities
import com.android.launcher3.icons.LauncherActivityCachingLogic
import com.android.launcher3.util.ComponentKey

class CustomActivityCachingLogic(context: Context) : LauncherActivityCachingLogic() {
    private val prefs = Utilities.getOmegaPrefs(context)

    override fun getLabel(info: LauncherActivityInfo): CharSequence {
        val key = ComponentKey(info.componentName, info.user)
        val customLabel = prefs.customAppName[key]
        if (!customLabel.isNullOrEmpty()) {
            return customLabel
        }
        return super.getLabel(info)
    }
}