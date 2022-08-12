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

package com.saggitt.omega.groups

import androidx.annotation.StringRes
import com.android.launcher3.R
import com.saggitt.omega.preferences.OmegaPreferences

class AppGroupsManager(val prefs: OmegaPreferences) {

    var categorizationEnabled by prefs.BooleanPref(
        key = "pref_apps_categorization_enabled",
        titleId = R.string.title_app_categorization_enable,
        summaryId = R.string.summary_app_categorization_enable,
        defaultValue = false,
        onChange = ::onPrefsChanged
    )
    var categorizationType by prefs.EnumPref(
        key = "pref_apps_categorization_type",
        titleId = R.string.pref_appcategorization_style_text,
        defaultValue = CategorizationType.Tabs,
        onChange = ::onPrefsChanged
    )
    var categorizationTypeNew by prefs.EnumSelectionPref(
        key = "pref_apps_categorization_type",
        titleId = R.string.pref_appcategorization_style_text,
        defaultValue = CategorizationType.Tabs,
        entries = CategorizationType.values()
            .associateBy(CategorizationType::ordinal, CategorizationType::nameId),
        onChange = ::onPrefsChanged
    )

    val drawerTabs by lazy { CustomTabs(this) }
    val flowerpotTabs by lazy { FlowerpotTabs(this) }
    val drawerFolders by lazy { DrawerFolders(this) }

    private fun onPrefsChanged() {
        prefs.getOnChangeCallback()!!.let {
            drawerTabs.checkIsEnabled(it)
            flowerpotTabs.checkIsEnabled(it)
            drawerFolders.checkIsEnabled(it)
        }
    }

    fun getEnabledType(): CategorizationType? {
        return CategorizationType.values().firstOrNull { getModel(it).isEnabled }
    }

    fun getEnabledModel(): AppGroups<*>? {
        return CategorizationType.values().map { getModel(it) }.firstOrNull { it.isEnabled }
    }

    private fun getModel(type: CategorizationType): AppGroups<*> {
        return when (type) {
            CategorizationType.Flowerpot -> flowerpotTabs
            CategorizationType.Tabs -> drawerTabs
            CategorizationType.Folders -> drawerFolders
        }
    }

    enum class CategorizationType(val prefsKey: String, @StringRes val nameId: Int) {
        Tabs("pref_drawer_tabs", R.string.app_categorization_tabs),
        Folders("pref_drawer_folders", R.string.app_categorization_tabs),
        Flowerpot("pref_drawer_flowerpot", R.string.pref_appcategorization_flowerpot_title)
    }
}