package com.looker.droidify.ui.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import com.looker.droidify.utility.isBlackTheme

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    blackTheme: Boolean = isBlackTheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        shapes = AppShapes,
        colors = when {
            darkTheme && blackTheme -> BlackColors
            darkTheme -> DarkColors
            else -> LightColors
        },
        content = content
    )
}

private val LightColors = lightColors(
    primary = LightPrimary,
    primaryVariant = LightPrimaryContainer,
    onPrimary = LightOnPrimary,
    secondary = LightSecondary,
    secondaryVariant = LightSecondaryContainer,
    onSecondary = LightOnSecondary,
    surface = LightSurface,
    background = LightBackground,
    onBackground = LightOnBackground,
    error = LightError
)

private val DarkColors = darkColors(
    primary = DarkPrimary,
    primaryVariant = DarkPrimaryContainer,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    secondaryVariant = DarkSecondaryContainer,
    onSecondary = DarkOnSecondary,
    surface = DarkSurface,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    error = DarkError
)

private val BlackColors = darkColors(
    primary = DarkPrimary,
    primaryVariant = DarkPrimaryContainer,
    onPrimary = DarkOnPrimary,
    secondary = DarkSecondary,
    secondaryVariant = DarkSecondaryContainer,
    onSecondary = DarkOnSecondary,
    surface = BlackSurface,
    background = BlackBackground,
    onBackground = DarkOnBackground,
    error = DarkError
)