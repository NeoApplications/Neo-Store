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

package com.saggitt.omega.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.android.launcher3.R
import com.android.launcher3.Utilities

abstract class SingleGridSizePreference(
    context: Context,
    attrs: AttributeSet?,
    private val gridSize: GridSize
) : DialogPreference(context, attrs) {
    val defaultSize by lazy { gridSize.numColumnsOriginal }

    init {
        updateSummary()
    }

    fun getSize(): Int {
        return gridSize.fromPref(gridSize.numColumns, defaultSize)
    }

    fun setSize(rows: Int) {
        gridSize.numColumnsPref.onSetValue((if (rows > 0) rows else defaultSize))
        updateSummary()
    }

    private fun updateSummary() {
        val value = getSize()
        summary = "$value"
    }

    override fun getDialogLayoutResource() = R.layout.preference_grid_size
}

class DrawerGridPreference(context: Context, attrs: AttributeSet?) :
    SingleGridSizePreference(context, attrs, Utilities.getOmegaPrefs(context).drawerGridSize)

class DockGridPreference(context: Context, attrs: AttributeSet?) :
    SingleGridSizePreference(context, attrs, Utilities.getOmegaPrefs(context).dockGridSize)
