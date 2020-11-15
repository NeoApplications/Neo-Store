/*
 *  Copyright (c) 2020 Omega Launcher
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.saggitt.omega.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.android.launcher3.R

abstract class SingleDimensionGridSizePreference(context: Context, attrs: AttributeSet?, private val gridSize: GridSize) :
        DialogPreference(context, attrs) {
    val defaultSize by lazy { gridSize.numRowsOriginal }

    init {
        updateSummary()
    }

    fun getSize(): Int {
        return gridSize.fromPref(gridSize.numRows, defaultSize)
    }

    fun setSize(rows: Int) {
        gridSize.numRowsPref = gridSize.toPref(rows, defaultSize)
        updateSummary()
    }

    private fun updateSummary() {
        val value = getSize()
        summary = "$value"
    }

    override fun getDialogLayoutResource() = R.layout.preference_grid_size
}