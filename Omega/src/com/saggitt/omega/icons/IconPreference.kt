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

package com.saggitt.omega.icons

import android.content.Context
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.preference.Preference
import com.android.launcher3.R
import com.saggitt.omega.adaptive.IconShapeDrawable
import com.saggitt.omega.adaptive.IconShapeManager
import com.saggitt.omega.util.dpToPx
import com.saggitt.omega.util.getColorAttr

class IconPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {
    private var iconShape = IconShapeManager.getInstance(context).iconShape
    private var iconShapeString = iconShape.toString()
    private val drawable = IconShapeDrawable(dpToPx(48f).toInt(), iconShape).apply {
        setColorFilter(context.getColorAttr(android.R.attr.colorControlNormal), PorterDuff.Mode.SRC_IN)
    }

    init {
        layoutResource = R.layout.preference_preview_icon
        fragment = IconCustomizeFragment::class.java.name
        updatePreview()
    }

    private fun updatePreview() {
        try {
            summary = iconShapeString
            icon = drawable
        } catch (ignored: Exception) {
        }
    }
}