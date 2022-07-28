/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
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

package com.saggitt.omega.preferences.views

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.saggitt.omega.preferences.custom.GridSizePreference
import com.saggitt.omega.util.applyAccent

class GridSizeDialogFragment : PreferenceDialogFragmentCompat() {
    private val gridSizePreference get() = preference as GridSizePreference

    private var numRows = 5
    private var numColumns = 5

    private lateinit var numRowsPicker: NumberPicker
    private lateinit var numColumnsPicker: NumberPicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val size = gridSizePreference.getSize()
        numRows = savedInstanceState?.getInt(SAVE_STATE_ROWS) ?: size.first
        numColumns = savedInstanceState?.getInt(SAVE_STATE_COLUMNS) ?: size.second
    }

    override fun onBindDialogView(view: View) {
        super.onBindDialogView(view)

        numRowsPicker = view.findViewById(R.id.rowsPicker)
        numColumnsPicker = view.findViewById(R.id.columnsPicker)

        numRowsPicker.minValue = 2
        numRowsPicker.maxValue = 16
        numColumnsPicker.minValue = 2
        numColumnsPicker.maxValue = 16

        numRowsPicker.value = numRows
        numColumnsPicker.value = numColumns
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) gridSizePreference.setSize(numRowsPicker.value, numColumnsPicker.value)
    }

    override fun onPrepareDialogBuilder(builder: AlertDialog.Builder) {
        super.onPrepareDialogBuilder(builder)

        builder.setNeutralButton(R.string.title_default) { _, _ ->
            val idp = LauncherAppState.getIDP(context)
            gridSizePreference.setSize(idp.numRowsOriginal, idp.numColumnsOriginal)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(SAVE_STATE_ROWS, numRowsPicker.value)
        outState.putInt(SAVE_STATE_COLUMNS, numColumnsPicker.value)
    }

    override fun onStart() {
        super.onStart()
        (dialog as AlertDialog).applyAccent()
    }

    companion object {
        const val SAVE_STATE_ROWS = "rows"
        const val SAVE_STATE_COLUMNS = "columns"

        fun newInstance(key: String?) = GridSizeDialogFragment().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }
}
