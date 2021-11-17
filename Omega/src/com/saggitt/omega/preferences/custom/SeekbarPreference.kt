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

package com.saggitt.omega.preferences.custom

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.launcher3.R
import com.android.launcher3.Utilities
import kotlin.math.roundToInt

open class SeekbarPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs),
    View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

    private var mSeekbar: SeekBar? = null

    @JvmField
    var mValueText: TextView? = null

    @JvmField
    var min: Float = 0f

    @JvmField
    var max: Float = 0f

    @JvmField
    var current: Float = 0f

    @JvmField
    var defaultValue: Float = 0f
    private var multiplier: Int = 0
    private var format: String? = null

    @JvmField
    var steps: Int = 100

    open val allowResetToDefault = true
    var mTrackingTouch = false

    init {
        layoutResource = R.layout.preference_seekbar
        val ta = context.obtainStyledAttributes(attrs, R.styleable.SeekbarPreference)
        min = ta.getFloat(R.styleable.SeekbarPreference_minValue, 0f)
        max = ta.getFloat(R.styleable.SeekbarPreference_maxValue, 100f)
        multiplier = ta.getInt(R.styleable.SeekbarPreference_summaryMultiplier, 1)
        format = ta.getString(R.styleable.SeekbarPreference_summaryFormat)
        defaultValue = ta.getFloat(R.styleable.SeekbarPreference_defaultSeekbarValue, min)
        steps = ta.getInt(R.styleable.SeekbarPreference_steps, 100)
        if (format == null) {
            format = "%.2f"
        }
        ta.recycle()
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val view = holder.itemView
        mSeekbar = view.findViewById(R.id.seekbar)
        mValueText = view.findViewById(R.id.txtValue)
        mSeekbar!!.max = steps

        mSeekbar!!.setOnSeekBarChangeListener(mSeekBarChangeListener)
        val stateList = ColorStateList.valueOf(Utilities.getOmegaPrefs(context).accentColor)
        mSeekbar!!.thumbTintList = stateList
        mSeekbar!!.progressTintList = stateList
        mSeekbar!!.progressBackgroundTintList = stateList

        current = getPersistedFloat(defaultValue)
        val progress = ((current - min) / ((max - min) / steps))
        mSeekbar!!.progress = progress.roundToInt()

        if (allowResetToDefault) view.setOnCreateContextMenuListener(this)
        updateSummary()
    }

    fun setValue(value: Float) {
        current = value
        persistFloat(value)
        updateDisplayedValue()
    }

    protected open fun updateDisplayedValue() {
        mSeekbar?.setOnSeekBarChangeListener(null)
        val progress = ((current - min) / ((max - min) / steps))
        mSeekbar!!.progress = progress.roundToInt()
        updateSummary()
        mSeekbar?.setOnSeekBarChangeListener(mSeekBarChangeListener)
    }

    @SuppressLint("SetTextI18n")
    protected open fun updateSummary() {
        if (format != "%.2f") {
            mValueText!!.text = String.format(format!!, current * multiplier)
        } else {
            mValueText!!.text = "${(current * multiplier).toInt()} dp"
        }
    }

    private val mSeekBarChangeListener = object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
            current = min + (max - min) / steps * progress
            current = (current * 100f).roundToInt() / 100f
            updateSummary()
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
            mTrackingTouch = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
            mTrackingTouch = false
            persistFloat(current)
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        menu.setHeaderTitle(title)
        menu.add(0, 0, 0, R.string.reset_to_default)
        for (i in (0 until menu.size())) {
            menu.getItem(i).setOnMenuItemClickListener(this)
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        setValue(defaultValue)
        return true
    }
}