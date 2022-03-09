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

import com.android.launcher3.allapps.AllAppsContainerView
import com.android.launcher3.allapps.AllAppsPagedView
import com.android.launcher3.allapps.AllAppsStore

class AllAppsPagesController(val pages: AllAppsPages, private val container: AllAppsContainerView) {
    private var holders = mutableListOf<AllAppsContainerView.AdapterHolder>()
    val pagesCount get() = pages.count

    private var horizontalPadding = 0
    private var bottomPadding = 0

    fun createHolders(): AdapterHolders {
        while (holders.size < pagesCount) {
            holders.add(container.createHolder(false).apply {
                padding.bottom = bottomPadding
                padding.left = horizontalPadding
                padding.right = horizontalPadding
            })
        }
        return holders.toTypedArray()
    }

    fun registerIconContainers(allAppsStore: AllAppsStore) {
        holders.forEach { allAppsStore.registerIconContainer(it.recyclerView) }
    }

    fun unregisterIconContainers(allAppsStore: AllAppsStore) {
        holders.forEach { allAppsStore.unregisterIconContainer(it.recyclerView) }
    }

    fun setup(pagedView: AllAppsPagedView) {
        pages.forEachIndexed { index, page ->
            holders[index].setIsWork(page.isWork)
            holders[index].setup(pagedView.getChildAt(index), page.filter.matcher)
        }
    }

    fun setPadding(horizontal: Int, bottom: Int) {
        horizontalPadding = horizontal
        bottomPadding = bottom

        holders.forEach {
            it.padding.bottom = bottomPadding
            it.padding.left = horizontalPadding
            it.padding.right = horizontalPadding
            it.applyPadding()
        }
    }
}