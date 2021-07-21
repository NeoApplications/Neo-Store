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

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceDialogFragmentCompat
import com.android.launcher3.LauncherAppState
import com.android.launcher3.R
import com.saggitt.omega.util.applyAccent

class GridSizeDialogFragmentCompat : PreferenceDialogFragmentCompat() {

    private val gridSizePreference get() = preference as GridSizePreference

    private var numRows = 0
    private var numColumns = 0

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

        numRowsPicker.minValue = 3
        numRowsPicker.maxValue = 9
        numColumnsPicker.minValue = 3
        numColumnsPicker.maxValue = 9

        numRowsPicker.value = numRows
        numColumnsPicker.value = numColumns
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (positiveResult) {
            gridSizePreference.setSize(numRowsPicker.value, numColumnsPicker.value)
        }
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

        fun newInstance(key: String?) = GridSizeDialogFragmentCompat().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }
}