package com.saggitt.omega.preferences.custom

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.StringRes
import androidx.preference.DialogPreference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.preferences.BasePreferences
import com.saggitt.omega.util.JavaField

class GridSizePreference(context: Context, attrs: AttributeSet?) :
    DialogPreference(context, attrs) {
    val gridSize = Utilities.getOmegaPrefs(context).desktopGridSize
    val defaultSize by lazy { Pair(gridSize.numColumnsOriginal, gridSize.numRowsOriginal) }

    init {
        updateSummary()
    }

    fun getSize(): Pair<Int, Int> {
        val rows = gridSize.fromPref(gridSize.numColumns, defaultSize.first)
        val columns = gridSize.fromPref(gridSize.numRows, defaultSize.second)
        return Pair(rows, columns)
    }

    fun setSize(rows: Int, columns: Int) {
        gridSize.numColumnsPref.onSetValue(gridSize.toPref(rows, defaultSize.first))
        gridSize.numRowsPref.onSetValue(gridSize.toPref(columns, defaultSize.second))
        updateSummary()
    }

    private fun updateSummary() {
        val value = getSize()
        summary = "${value.first}x${value.second}"
    }

    override fun getDialogLayoutResource() = R.layout.pref_dialog_grid_size
}

open class GridSize(
    @StringRes val titleId: Int,
    val numColumnsPref: BasePreferences.IdpIntPref,
    columnsKey: String,
    targetObject: Any,
    private val onChangeListener: () -> Unit
) {
    var numColumns by JavaField<Int>(targetObject, columnsKey)
    val numColumnsOriginal by JavaField<Int>(targetObject, "${columnsKey}Original")
    protected val onChange = {
        applyCustomization()
        onChangeListener.invoke()
    }

    init {
        applyNumColumns()
    }

    protected open fun applyCustomization() {
        applyNumColumns()
    }

    private fun applyNumColumns() {
        numColumns = fromPref(numColumnsPref.onGetValue(), numColumnsOriginal)
    }

    fun fromPref(value: Int, default: Int) = if (value != 0) value else default
    fun toPref(value: Int, default: Int) = if (value != default) value else 0
}

class GridSize2D(
    @StringRes titleId: Int,
    numColumnsPref: BasePreferences.IdpIntPref,
    val numRowsPref: BasePreferences.IdpIntPref,
    columnsKey: String,
    rowsKey: String,
    targetObject: Any,
    onChangeListener: () -> Unit
) : GridSize(
    titleId,
    numColumnsPref,
    columnsKey,
    targetObject,
    onChangeListener
) {
    var numRows by JavaField<Int>(targetObject, rowsKey)
    val numRowsOriginal by JavaField<Int>(targetObject, "${rowsKey}Original")

    init {
        applyNumRows()
    }

    override fun applyCustomization() {
        super.applyCustomization()
        applyNumRows()
    }

    private fun applyNumRows() {
        numRows = fromPref(numRowsPref.onGetValue(), numRowsOriginal)
    }
}