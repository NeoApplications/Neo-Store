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

package com.saggitt.omega.override

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import com.android.launcher3.LauncherAppState
import com.android.launcher3.model.ModelWriter
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.iconpack.CustomIconEntry
import com.saggitt.omega.util.SingletonHolder
import com.saggitt.omega.util.ensureOnMainThread
import com.saggitt.omega.util.omegaPrefs
import com.saggitt.omega.util.useApplicationContext

class AppInfoProvider(context: Context) : CustomInfoProvider<AppInfo>(context) {

    private val prefs = context.omegaPrefs
    private val launcherApps by lazy { context.getSystemService(LauncherApps::class.java) }

    override fun getTitle(info: AppInfo): String {
        return prefs.customAppName[info.toComponentKey()] ?: info.title.toString()
    }

    override fun getDefaultTitle(info: AppInfo): String {
        val app = getLauncherActivityInfo(info)
        return app?.label?.toString() ?: "" // TODO: can this really be null?
    }

    override fun getCustomTitle(info: AppInfo): String? {
        return prefs.customAppName[ComponentKey(info.componentName, info.user)]
    }

    fun getTitle(app: LauncherActivityInfo): CharSequence {
        return prefs.customAppName[getComponentKey(app)] ?: app.label
    }

    override fun setTitle(info: AppInfo, title: String?, modelWriter: ModelWriter) {
        setTitle(info.toComponentKey(), title)
    }

    fun setTitle(key: ComponentKey, title: String?) {
        prefs.customAppName[key] = title
        LauncherAppState.getInstance(context).iconCache.updateIconsForPkg(
            key.componentName.packageName,
            key.user
        )
    }

    /*override fun setIcon(info: AppInfo, entry: CustomIconEntry?) {
        setIcon(info.toComponentKey(), entry)
    }*/

    fun setIcon(key: ComponentKey, entry: CustomIconEntry?) {
        prefs.customAppIcon[key] = entry
        LauncherAppState.getInstance(context).iconCache.updateIconsForPkg(
            key.componentName.packageName,
            key.user
        )
    }

    private fun getLauncherActivityInfo(info: AppInfo): LauncherActivityInfo? {
        return launcherApps.resolveActivity(info.getIntent(), info.user)
    }

    fun getCustomIconEntry(app: LauncherActivityInfo): CustomIconEntry? {
        return getCustomIconEntry(getComponentKey(app))
    }

    private fun getCustomIconEntry(key: ComponentKey): CustomIconEntry? {
        return prefs.customAppIcon[key]
    }

    /*override fun getIcon(info: AppInfo): CustomIconEntry? {
        return getCustomIconEntry(info.toComponentKey())
    }*/

    private fun getComponentKey(app: LauncherActivityInfo) =
        ComponentKey(app.componentName, app.user)

    companion object : SingletonHolder<AppInfoProvider, Context>(
        ensureOnMainThread(
            useApplicationContext(::AppInfoProvider)
        )
    )
}