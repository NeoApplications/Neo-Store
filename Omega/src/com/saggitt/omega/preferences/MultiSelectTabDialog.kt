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

import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.android.launcher3.R.string
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.groups.ui.AppCategorizationFragment
import com.saggitt.omega.settings.SettingsActivity
import com.saggitt.omega.util.addOrRemove

class MultiSelectTabDialog(
    val componentKey: ComponentKey,
    val tabs: List<DrawerTabs.CustomTab>,
    private val updatePref: () -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val entries = tabs.map { it.getTitle() }.toTypedArray()
        val checkedEntries = tabs.map {
            it.contents.value().contains(componentKey)
        }.toBooleanArray()

        val selectedItems = checkedEntries.toMutableList()
        return AlertDialog.Builder(getActivity())
            .setMultiChoiceItems(
                entries,
                checkedEntries
            ) { _: DialogInterface?, index: Int, isChecked: Boolean ->
                selectedItems[index] =
                    isChecked
            }
            .setPositiveButton(android.R.string.ok) { _: DialogInterface?, _: Int ->
                tabs.forEachIndexed { i, tab ->
                    tab.contents.value().addOrRemove(componentKey, selectedItems[i])
                }
                updatePref.invoke()
            }
            .setNeutralButton(string.tabs_manage) { _, _ ->
                SettingsActivity.startFragment(
                    context, AppCategorizationFragment::class.java.name,
                    string.title__drawer_categorization
                )
            }
            .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
            .create()
    }
}
