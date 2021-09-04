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

package com.saggitt.omega.font

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.util.applyColor

class GlobalFontPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {
    private var fontButton: ImageView? = null
    private var switch: Switch? = null
    private var dialogFrame: LinearLayout? = null
    private val prefs by lazy { Utilities.getOmegaPrefs(context) }

    init {
        layoutResource = R.layout.preference_global_font
        widgetLayoutResource = R.layout.preference_switch_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.findViewById(R.id.clickableRow).setOnClickListener {
            updateUI()
        }

        fontButton = holder.findViewById(R.id.fontButton) as ImageView?
        fontButton!!.imageTintList = ColorStateList.valueOf(prefs.accentColor)
        dialogFrame = holder.findViewById(R.id.dialog_frame) as LinearLayout?
        dialogFrame!!.setOnClickListener {
            context.startActivity(
                Intent(context, FontSelectionActivity::class.java)
                    .putExtra(FontSelectionActivity.EXTRA_KEY, key)
            )
        }

        switch = holder.findViewById(R.id.switchWidget) as Switch
        switch!!.applyColor(prefs.accentColor)
    }

    private fun updateUI() {
        switch!!.isChecked = !prefs.globalFont
        if (prefs.globalFont) {
            prefs.globalFont = false
            dialogFrame!!.isClickable = false
        } else {
            prefs.globalFont = true
            dialogFrame!!.isClickable = true
        }
    }
}