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
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.android.launcher3.Launcher
import com.android.launcher3.LauncherAppState
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.OmegaPreferences
import java.util.*

val Context.omegaPrefs: OmegaPreferences get() = Utilities.getOmegaPrefs(this)

val Context.launcherAppState: LauncherAppState get() = LauncherAppState.getInstance(this)

fun Context.getLauncherOrNull(): Launcher? {
    return try {
        Launcher.getLauncher(this)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun Context.getBooleanAttr(attr: Int): Boolean {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val value = ta.getBoolean(0, false)
    ta.recycle()
    return value
}

@ColorInt
fun Context.getColorAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    @ColorInt val colorAccent = ta.getColor(0, 0)
    ta.recycle()
    return colorAccent
}

fun Context.getThemeAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val theme = ta.getResourceId(0, 0)
    ta.recycle()
    return theme
}

fun Context.getDimenAttr(attr: Int): Int {
    val ta = obtainStyledAttributes(intArrayOf(attr))
    val size = ta.getDimensionPixelSize(0, 0)
    ta.recycle()
    return size
}

fun Context.getIcon(): Drawable = packageManager.getApplicationIcon(applicationInfo)

val Context.hasStoragePermission
    get() = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
        this, android.Manifest.permission.READ_EXTERNAL_STORAGE
    )

fun Context.checkLocationAccess(): Boolean {
    return Utilities.hasPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ||
            Utilities.hasPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
}

fun Context.checkPackagePermission(packageName: String, permissionName: String): Boolean {
    try {
        val info = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
        info.requestedPermissions.forEachIndexed { index, s ->
            if (s == permissionName) {
                return info.requestedPermissionsFlags[index].hasFlag(PackageInfo.REQUESTED_PERMISSION_GRANTED)
            }
        }
    } catch (_: PackageManager.NameNotFoundException) {
    }
    return false
}

val Context.locale: Locale
    get() = this.resources.configuration.locales[0]

fun <T> useApplicationContext(creator: (Context) -> T): (Context) -> T {
    return { it -> creator(it.applicationContext) }
}