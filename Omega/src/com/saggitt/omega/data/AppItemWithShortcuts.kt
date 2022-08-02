/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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

package com.saggitt.omega.data

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.text.TextUtils
import android.util.DisplayMetrics
import com.android.launcher3.shortcuts.ShortcutRequest
import com.android.launcher3.util.ComponentKey

class AppItemWithShortcuts(val context: Context, val info: LauncherActivityInfo) {
    val key = ComponentKey(info.componentName, info.user)
    val shortcuts = loadShortcuts()
    val hasShortcuts get() = shortcuts.isNotEmpty()
    var expanded = false

    private fun loadShortcuts(): List<ShortcutItem> {
        val shortcuts = ShortcutRequest(context, key.user)
            .withContainer(key.componentName)
            .query(ShortcutRequest.PUBLISHED)
        return shortcuts.map { ShortcutItem(it) }
    }

    fun clone(context: Context, expanded: Boolean) =
        AppItemWithShortcuts(context, info).also { it.expanded = expanded }

    inner class ShortcutItem(val info: ShortcutInfo) {

        val label = if (!TextUtils.isEmpty(info.longLabel)) info.longLabel else info.shortLabel
        val iconDrawable = context
            .getSystemService(LauncherApps::class.java)
            .getShortcutIconDrawable(info, DisplayMetrics.DENSITY_XXHIGH)!!
    }
}