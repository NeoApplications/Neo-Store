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

package com.saggitt.omega.util

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.DrawableCompat
import com.android.launcher3.Utilities
import com.android.systemui.shared.system.QuickStepContract

fun AlertDialog.applyAccent() {
    val color = Utilities.getOmegaPrefs(context).themeAccentColor.onGetValue()
    val buttons = listOf(
        getButton(DialogInterface.BUTTON_NEGATIVE),
        getButton(DialogInterface.BUTTON_NEUTRAL),
        getButton(DialogInterface.BUTTON_POSITIVE)
    )
    buttons.forEach {
        it?.setTextColor(color)
    }
}

fun ImageView.tintDrawable(color: Int) {
    val drawable = drawable.mutate()
    drawable.setTint(color)
    setImageDrawable(drawable)
}

fun GradientDrawable.getCornerRadiiCompat(): FloatArray? {
    return try {
        cornerRadii
    } catch (e: NullPointerException) {
        null
    }
}

fun Button.applyColor(color: Int) {
    val rippleColor = ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 31))
    background?.let {
        (it as RippleDrawable).setColor(rippleColor)
        DrawableCompat.setTint(background, color)
    }
    val tintList = ColorStateList.valueOf(color)
    if (this is RadioButton) {
        buttonTintList = tintList
    }
}

val Configuration.usingNightMode get() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

val Int.luminance get() = ColorUtils.calculateLuminance(this)

val Int.isDark get() = luminance < 0.5f

fun getWindowCornerRadius(context: Context): Float {
    val prefs = Utilities.getOmegaPrefs(context)
    if (prefs.themeCornerRadius.onGetValue() > -1) {
        return prefs.themeCornerRadius.onGetValue()
    }
    return QuickStepContract.getWindowCornerRadius(context.resources)
}

fun supportsRoundedCornersOnWindows(context: Context): Boolean {
    val pref = Utilities.getOmegaPrefs(context)
    if (!Utilities.ATLEAST_R || pref.themeCornerRadius.onGetValue() > -1) {
        return true
    }
    return QuickStepContract.supportsRoundedCornersOnWindows(context.resources)
}
