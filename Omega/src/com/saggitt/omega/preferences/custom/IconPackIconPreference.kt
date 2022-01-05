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

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.iconpack.IconPackProvider

class IconPackIconPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    private val prefs = Utilities.getOmegaPrefs(context)

    val packs = IconPackProvider.INSTANCE.get(context).getIconPackList()

    init {
        layoutResource = R.layout.preference_preview_icon
        updatePreview()
    }

    private fun updatePreview() {
        try {
            for (pack in packs) {
                if (pack.packageName == prefs.iconPackPackage) {
                    summary = pack.name
                    icon = pack.icon
                } else {
                    summary = context.getString(R.string.icon_pack_default)
                    icon = context.getDrawable(R.mipmap.ic_launcher)
                }
            }
        } catch (ignored: Exception) {
        }
    }
}