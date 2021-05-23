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

package com.saggitt.omega.preferences

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.util.omegaPrefs

class MultiSelectTabPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {

    lateinit var componentKey: ComponentKey
    lateinit var activity: Activity
    private val tabs: List<DrawerTabs.CustomTab> =
        context.omegaPrefs.drawerTabs.getGroups().mapNotNull { it as? DrawerTabs.CustomTab }
    var edited = false
        private set

    init {
        setOnPreferenceClickListener {
            MultiSelectTabDialog( componentKey, tabs) {
                callChangeListener(tabs.hashCode())
            }.show(activity.fragmentManager, "TABS_MULTISELECT_DIALOG")
            true
        }
        setOnPreferenceChangeListener { _, _ ->
            updateSummary()
            edited = true
            true
        }
    }

    fun updateSummary() {
        summary = tabs
            .filter { it.contents.value?.contains(componentKey) == true }
            .joinToString(", ") { it.title }
    }
}
