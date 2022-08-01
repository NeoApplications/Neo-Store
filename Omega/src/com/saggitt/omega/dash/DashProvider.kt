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

package com.saggitt.omega.dash

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import com.android.launcher3.Utilities

abstract class DashProvider(protected val context: Context) {
    val accentColor = Utilities.getOmegaPrefs(context).themeAccentColor.onGetValue()
    abstract val itemId: Int
    abstract val name: String
    abstract val description: String
    abstract val icon: Drawable?

    @ColorInt
    fun darkenColor(@ColorInt color: Int): Int {
        return Color.HSVToColor(FloatArray(3).apply {
            Color.colorToHSV(color, this)
            this[2] *= 0.8f
        })
    }
}

abstract class DashActionProvider(context: Context) : DashProvider(context) {
    abstract fun runAction(context: Context)
}

abstract class DashControlProvider(context: Context) : DashProvider(context) {
    abstract var state: Boolean
    abstract val extendable: Boolean
}