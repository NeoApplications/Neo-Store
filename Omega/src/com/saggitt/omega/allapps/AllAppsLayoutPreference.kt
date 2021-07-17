/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.allapps

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.applyAccent

class AllAppsLayoutPreference(context: Context, attrs: AttributeSet? = null) :
    ListPreference(context, attrs) {

    override fun callChangeListener(newValue: Any?): Boolean {
        if (newValue is String) {
            if (newValue == "1") {
                AlertDialog.Builder(context)
                    .setTitle(R.string.title__drawer_sort)
                    .setMessage(R.string.dialog_message_change_sort)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        val prefs = Utilities.getOmegaPrefs(context)
                        prefs.sharedPrefs.edit()
                            .putString("pref_key__sort_mode", "0")
                            .apply()
                    }
                    .show()
                    .applyAccent()
            }
        }
        return super.callChangeListener(newValue)
    }
}