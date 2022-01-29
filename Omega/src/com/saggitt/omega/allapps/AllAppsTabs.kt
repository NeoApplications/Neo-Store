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

import android.content.ComponentName
import android.content.Context
import com.android.launcher3.model.data.ItemInfo
import com.android.launcher3.util.ComponentKey
import com.android.launcher3.util.ItemInfoMatcher
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.groups.FlowerpotTabs
import com.saggitt.omega.util.omegaPrefs

class AllAppsTabs(private val context: Context) : Iterable<AllAppsTabs.Tab> {

    val tabs = ArrayList<Tab>()
    val count get() = tabs.size

    var hasWorkApps = false
        set(value) {
            if (value != field) {
                field = value
                reloadTabs()
            }
        }

    private val addedApps = ArrayList<ComponentKey>()

    init {
        reloadTabs()
    }

    fun reloadTabs() {
        addedApps.clear()
        tabs.clear()
        context.omegaPrefs.currentTabsModel.getGroups().mapNotNullTo(tabs) {
            when {
                it is DrawerTabs.ProfileTab -> {
                    if (hasWorkApps != it.profile.matchesAll) {
                        ProfileTab(createMatcher(addedApps, it.profile.matcher), it)
                    } else null
                }
                it is DrawerTabs.CustomTab -> {
                    if (it.hideFromAllApps.value()) {
                        addedApps.addAll(it.contents.value())
                    }
                    Tab(it.title, it.filter.matcher, drawerTab = it)
                }
                it is FlowerpotTabs.FlowerpotTab && it.getMatches().isNotEmpty() -> {
                    addedApps.addAll(it.getMatches())
                    Tab(it.title, it.getFilter(context).matcher, drawerTab = it)
                }
                else -> null
            }
        }
    }

    private fun createMatcher(
            components: List<ComponentKey>,
            base: ItemInfoMatcher? = null
    ): ItemInfoMatcher {
        return object : ItemInfoMatcher {
            override fun matches(info: ItemInfo, cn: ComponentName?): Boolean {
                if (base?.matches(info, cn) == false) return false
                return !components.contains(ComponentKey(info.targetComponent, info.user))
            }
        }
    }

    override fun iterator(): Iterator<Tab> {
        return tabs.iterator()
    }

    operator fun get(index: Int) = tabs[index]

    inner class ProfileTab(matcher: ItemInfoMatcher?, drawerTab: DrawerTabs.ProfileTab) :
            Tab(drawerTab.title, matcher, drawerTab.profile.isWork, drawerTab)

    open class Tab(
            val name: String, val matcher: ItemInfoMatcher?,
            val isWork: Boolean = false, val drawerTab: DrawerTabs.Tab
    )
}
