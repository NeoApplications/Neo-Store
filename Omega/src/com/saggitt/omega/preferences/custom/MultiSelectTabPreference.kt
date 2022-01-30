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

package com.saggitt.omega.preferences.custom

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import com.android.launcher3.R
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.preferences.views.AppCategorizationFragment
import com.saggitt.omega.preferences.views.PreferencesActivity
import com.saggitt.omega.util.addOrRemove
import com.saggitt.omega.util.applyColor
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
            multiSelectDialog(componentKey, tabs) {
                callChangeListener(tabs.hashCode())
            }
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

    private fun multiSelectDialog(componentKey: ComponentKey,
                                  tabs: List<DrawerTabs.CustomTab>,
                                  updatePref: () -> Unit) {

        val entries = tabs.map { it.title }.toTypedArray()
        val checkedEntries = tabs.map {
            it.contents.value().contains(componentKey)
        }.toBooleanArray()

        val selectedItems = checkedEntries.toMutableList()

        val builder = AlertDialog.Builder(context).apply {
            setMultiChoiceItems(entries, checkedEntries) { _, which, isChecked ->
                selectedItems[which] = isChecked
            }

            setPositiveButton(android.R.string.ok) { _, _ ->
                tabs.forEachIndexed { index, tab ->
                    tab.contents.value().addOrRemove(componentKey, selectedItems[index])
                }
                updatePref.invoke()
            }

            setNegativeButton(android.R.string.cancel) { _, _ -> }

            setNeutralButton(R.string.tabs_manage) { _, _ ->
                PreferencesActivity.startFragment(
                        context, AppCategorizationFragment::class.java.name,
                        context.resources.getString(R.string.title__drawer_categorization)
                )
            }
        }

        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).apply {
                applyColor(context.omegaPrefs.accentColor)
                setTextColor(context.omegaPrefs.accentColor)
            }
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).apply {
                setTextColor(context.omegaPrefs.accentColor)
            }
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).apply {
                setTextColor(context.omegaPrefs.accentColor)
            }
        }
        alertDialog.show()
    }
}
