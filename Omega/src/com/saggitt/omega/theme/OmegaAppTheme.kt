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

package com.saggitt.omega.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Composable
fun OmegaAppTheme(
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (isDarkTheme) darkColorPalette else lightColorPalette
    ProvideOmegaColors(colors) {
        MaterialTheme(
            content = content
        )
    }
}

object OmegaTheme {
    val colors: OmegaColors
        @Composable
        get() = localOmegaColors.current
}

private val darkColorPalette = OmegaColors(
    surface = DarkGrey50,
    textPrimary = LightGrey05,
    textSecondary = LightGrey05,
    dividerLine = DarkGrey05
)

private val lightColorPalette = OmegaColors(
    surface = White,
    textPrimary = DarkGrey90,
    textSecondary = DarkGrey05,
    dividerLine = LightGrey30
)

@Stable
class OmegaColors(
    surface: Color,
    textPrimary: Color,
    textSecondary: Color,
    dividerLine: Color
) {
    var surface by mutableStateOf(surface)
        private set
    var textPrimary by mutableStateOf(textPrimary)
        private set
    var textSecondary by mutableStateOf(textSecondary)
        private set
    var dividerLine by mutableStateOf(dividerLine)
        private set

    fun update(other: OmegaColors) {
        surface = other.surface
        textPrimary = other.textPrimary
        textSecondary = other.textSecondary
        dividerLine = other.dividerLine
    }

    fun copy(): OmegaColors = OmegaColors(
        surface = surface,
        textPrimary = textPrimary,
        textSecondary = textSecondary,
        dividerLine = dividerLine
    )
}

@Composable
fun ProvideOmegaColors(
    colors: OmegaColors,
    content: @Composable () -> Unit
) {
    val colorPalette = remember {
        // Explicitly creating a new object here so we don't mutate the initial [colors]
        // provided, and overwrite the values set in it.
        colors.copy()
    }
    colorPalette.update(colors)
    CompositionLocalProvider(localOmegaColors provides colorPalette, content = content)
}

private val localOmegaColors = staticCompositionLocalOf<OmegaColors> {
    error("No OmegaColors provided")
}