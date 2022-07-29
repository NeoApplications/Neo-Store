package com.saggitt.omega.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.preference.DialogPreference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.OmegaPreferences
import com.saggitt.omega.util.JavaField

class GridSizePreference(context: Context, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {
    val gridSize = Utilities.getOmegaPrefs(context).desktopGridSize
    val defaultSize by lazy { Pair(gridSize.numRowsOriginal, gridSize.numColumnsOriginal) }

    init {
        updateSummary()
    }

    fun getSize(): Pair<Int, Int> {
        val rows = gridSize.fromPref(gridSize.numRows, defaultSize.first)
        val columns = gridSize.fromPref(gridSize.numColumns, defaultSize.second)
        return Pair(rows, columns)
    }

    fun setSize(rows: Int, columns: Int) {
        gridSize.numRowsPref = gridSize.toPref(rows, defaultSize.first)
        gridSize.numColumnsPref = gridSize.toPref(columns, defaultSize.second)
        updateSummary()
    }

    private fun updateSummary() {
        val value = getSize()
        summary = "${value.first}x${value.second}"
    }

    override fun getDialogLayoutResource() = R.layout.pref_dialog_grid_size
}

open class GridSize(
    prefs: OmegaPreferences,
    rowsKey: String,
    targetObject: Any,
    private val onChangeListener: () -> Unit
) {
    var numRows by JavaField<Int>(targetObject, rowsKey)
    val numRowsOriginal by JavaField<Int>(targetObject, "${rowsKey}Original")
    protected val onChange = {
        applyCustomization()
        onChangeListener.invoke()
    }

    var numRowsPref by prefs.IntPref(
        key = "pref_$rowsKey",
        titleId = -1,
        defaultValue = 0,
        onChange = onChange
    )

    init {
        applyNumRows()
    }

    protected open fun applyCustomization() {
        applyNumRows()
    }

    private fun applyNumRows() {
        numRows = fromPref(numRowsPref, numRowsOriginal)
    }

    fun fromPref(value: Int, default: Int) = if (value != 0) value else default
    fun toPref(value: Int, default: Int) = if (value != default) value else 0
}

class GridSize2D(
    prefs: OmegaPreferences,
    rowsKey: String,
    columnsKey: String,
    targetObject: Any,
    onChangeListener: () -> Unit
) : GridSize(prefs, rowsKey, targetObject, onChangeListener) {
    var numColumns by JavaField<Int>(targetObject, columnsKey)
    val numColumnsOriginal by JavaField<Int>(targetObject, "${columnsKey}Original")
    var numColumnsPref by prefs.IntPref(
        key = "pref_$columnsKey",
        titleId = -1,
        defaultValue = 0,
        onChange = onChange
    )

    init {
        applyNumColumns()
    }

    override fun applyCustomization() {
        super.applyCustomization()
        applyNumColumns()
    }

    private fun applyNumColumns() {
        numColumns = fromPref(numColumnsPref, numColumnsOriginal)
    }
}