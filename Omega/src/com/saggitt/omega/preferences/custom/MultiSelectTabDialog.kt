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

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import com.android.launcher3.R
import com.android.launcher3.R.string
import com.android.launcher3.util.ComponentKey
import com.saggitt.omega.groups.DrawerTabs
import com.saggitt.omega.preferences.views.AppCategorizationFragment
import com.saggitt.omega.preferences.views.PreferencesActivity
import com.saggitt.omega.util.addOrRemove

@SuppressLint("ValidFragment")
class MultiSelectTabDialog(
        context: Context,
        val componentKey: ComponentKey,
        val tabs: List<DrawerTabs.CustomTab>,
        private val updatePref: () -> Unit
) : DialogFragment() {

    val mContext = context

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val entries = tabs.map { it.title }.toTypedArray()
        val checkedEntries = tabs.map {
            it.contents.value().contains(componentKey)
        }.toBooleanArray()

        val selectedItems = checkedEntries.toMutableList()
        return AlertDialog.Builder(activity)
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
                    PreferencesActivity.startFragment(
                            mContext, AppCategorizationFragment::class.java.name,
                            mContext.resources.getString(R.string.title__drawer_categorization)
                    )
                }
                .setNegativeButton(android.R.string.cancel) { dialog: DialogInterface?, _: Int -> dialog?.cancel() }
                .create()

    }
}
