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

import android.content.Context
import android.content.pm.LauncherActivityInfo
import com.android.launcher3.AppFilter
import com.android.launcher3.LauncherAppState
import com.android.launcher3.model.data.AppInfo
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.CustomFilter
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.sortApps

class AllAppsPages(
    val context: Context
) : Iterable<AllAppsPages.Page> {
    var pages = ArrayList<Page>()
    var count = 1
    private val idp = LauncherAppState.getIDP(context)
    private var mNumRows = idp.numRows // TODO change to allApps Row instead of desktop row
    private var mNumColumns = idp.numAllAppsColumns
    private var activityInfoList = listOf<LauncherActivityInfo>()
    private var appList = ArrayList<AppInfo>()
    private val config = Config(context)

    init {
        activityInfoList = config.getAppsList(AppFilter())

        activityInfoList.forEach {
            appList.add(AppInfo(it, it.user, false))
        }
        reloadPages()
    }

    private fun reloadPages() {
        pages.clear()
        val appsPerPage = mNumColumns * mNumRows
        var pageCount = (appList.size / appsPerPage) + if (appList.size % appsPerPage <= 0) 0 else 1
        if (pageCount == 0) {
            pageCount++
        }

        appList.sortApps(
            context,
            OmegaPreferences.getInstance(context).drawerSortModeNew.onGetValue()
        )
        var initialApp = 0
        var endApp = appsPerPage
        for (page in 0 until pageCount) {
            if (endApp > appList.lastIndex) {
                endApp = appList.lastIndex + 1
            }

            val addedApps = HashSet<ComponentKey>()
            for (appIndex in initialApp until endApp) {
                addedApps.add(appList[appIndex].toComponentKey())
            }

            pages.add(Page(false, CustomFilter(context, addedApps)))
            initialApp = ((page + 1) * appsPerPage)
            endApp = initialApp + appsPerPage
        }
        count = pages.size
    }

    override fun iterator(): Iterator<Page> {
        return pages.iterator()
    }

    operator fun get(index: Int) = pages[index]

    class Page(
        val isWork: Boolean = false,
        val filter: CustomFilter
    )
}