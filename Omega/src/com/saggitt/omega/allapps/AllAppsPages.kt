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
import android.util.Log
import com.android.launcher3.AppFilter
import com.android.launcher3.LauncherAppState
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.ItemInfoMatcher
import com.saggitt.omega.util.Config
import java.util.*

class AllAppsPages(
    val context: Context
) : Iterable<AllAppsPages.Page> {
    var pages = ArrayList<Page>()
    var count = 1
    private val idp = LauncherAppState.getIDP(context)
    private var mNumRows = idp.numRows // TODO change to allApps Row instead of desktop row
    private var mNumColumns = idp.numAllAppsColumns
    private var appList = listOf<LauncherActivityInfo>()
    private var componentList = listOf<ComponentKey>()
    private val config = Config(context)
    private val addedApps = mutableSetOf<ComponentKey>()

    init {
        appList = (config.getAppsList(AppFilter()) as ArrayList<LauncherActivityInfo>)
            .sortedBy { it.label.toString().lowercase(Locale.getDefault()) }

        componentList = appList.map { ComponentKey(it.componentName, it.user) }
        reloadPages()
    }

    private fun reloadPages() {
        pages.clear()
        val appsPerPage = mNumColumns * mNumRows
        var pageCount =
            (componentList.size / appsPerPage) + if (componentList.size % appsPerPage <= 0) 0 else 1
        if (pageCount == 0) {
            pageCount++
        }

        var initialApp = 0 * appsPerPage
        var endApp = initialApp + appsPerPage - 1
        for (page in 0 until pageCount) {
            if (endApp > componentList.lastIndex) {
                endApp = componentList.lastIndex
            }

            addedApps.clear()
            for (appIndex in initialApp until endApp) {
                addedApps.add(componentList[appIndex])
            }

            pages.add(Page(false, createMatcher(addedApps)))
            Log.d("AllAppsPages", "Pagina Numero " + page)
            addedApps.map {
                Log.d("AllAppsPages", "Application " + it.componentName)
            }
            Log.d("AllAppsPages", "\n\n\n")
            addedApps.clear()
            initialApp = ((page + 1) * appsPerPage) - 1
            endApp = initialApp + appsPerPage
        }
        count = pages.size
    }

    override fun iterator(): Iterator<Page> {
        return pages.iterator()
    }

    operator fun get(index: Int) = pages[index]

    private fun createMatcher(
        components: Set<ComponentKey>
    ): ItemInfoMatcher {
        return ItemInfoMatcher { info, cn ->
            Log.d(
                "AllAppsPages",
                "Matcher " + !components.contains(ComponentKey(info.targetComponent, info.user))
            )
            !components.contains(ComponentKey(info.targetComponent, info.user))
        }
    }

    class Page(
        val isWork: Boolean = false,
        val matcher: ItemInfoMatcher
    )
}