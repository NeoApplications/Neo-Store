/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.groups

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.launcher3.R
import com.android.launcher3.pm.UserCache
import com.saggitt.omega.groups.ui.AppGroupsAdapter
import com.saggitt.omega.util.isVisible
import com.saggitt.omega.util.omegaPrefs

open class DrawerTabsAdapter(context: Context) :
        AppGroupsAdapter<DrawerTabsAdapter.TabHolder, DrawerTabs.Tab>(context) {

    override val groupsModel: DrawerTabs = manager.drawerTabs
    override val headerText = R.string.app_categorization_tabs

    private val hasWorkApps = context.omegaPrefs.separateWorkApps &&
            UserCache.INSTANCE.get(context).userProfiles.size > 1

    override fun createGroup(callback: (DrawerTabs.Tab, Boolean) -> Unit) {
         DrawerTabTypeSelectionBottomSheet.show(
                 context, mapOf(
                 FlowerpotTabs.TYPE_FLOWERPOT to arrayOf(
                         R.string.tab_type_smart,
                         R.string.pref_appcategorization_flowerpot_summary,
                         R.drawable.ic_category
                 ),
                 DrawerTabs.TYPE_CUSTOM to arrayOf(
                         R.string.custom,
                         R.string.tab_type_custom_desc,
                         R.drawable.ic_squares_four
                )
        )
        ) {
            when (it) {
                DrawerTabs.TYPE_CUSTOM -> callback(DrawerTabs.CustomTab(context), false)
                FlowerpotTabs.TYPE_FLOWERPOT -> callback(FlowerpotTabs.FlowerpotTab(context), false)
            }
        }
    }

    override fun createGroupHolder(parent: ViewGroup): TabHolder {
        return TabHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.tab_item, parent, false)
        )
    }

    override fun filterGroups(): Collection<DrawerTabs.Tab> {
        return if (hasWorkApps) {
            groupsModel.getGroups()
                    .filter { it !is DrawerTabs.ProfileTab || !it.profile.matchesAll }
        } else {
            groupsModel.getGroups().filter { it !is DrawerTabs.ProfileTab || it.profile.matchesAll }
        }
    }

    open inner class TabHolder(itemView: View) : GroupHolder(itemView) {

        override fun bind(info: AppGroups.Group) {
            super.bind(info)

            delete.isVisible =
                    info.type in arrayOf(DrawerTabs.TYPE_CUSTOM, FlowerpotTabs.TYPE_FLOWERPOT)
        }
    }
}
