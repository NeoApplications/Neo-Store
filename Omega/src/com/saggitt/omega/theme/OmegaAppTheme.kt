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

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.saggitt.omega.util.Config
import com.saggitt.omega.util.omegaPrefs

@Composable
fun OmegaAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when (Config.getCurrentTheme(LocalContext.current)) {
            Config.THEME_BLACK -> {
                OmegaBlackColors
            }
            Config.THEME_DARK -> {
                OmegaDarkColors
            }
            else -> {
                OmegaLightColors
            }
        }.copy(
            primary = Color(LocalContext.current.omegaPrefs.themeAccentColor.onGetValue()),
            surfaceTint = Color(LocalContext.current.omegaPrefs.themeAccentColor.onGetValue())
        ),
        content = content
    )
}

private val OmegaLightColors = lightColorScheme(
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline
)

private val OmegaDarkColors = darkColorScheme(
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

private val OmegaBlackColors = darkColorScheme(
    background = BlackBackground,
    onBackground = BlackOnBackground,
    surface = BlackSurface,
    onSurface = BlackOnSurface,
    primary = BlackPrimary,
    onPrimary = BlackOnPrimary,
    surfaceVariant = BlackSurfaceVariant,
    onSurfaceVariant = BlackOnSurfaceVariant,
    outline = BlackOutline
)
