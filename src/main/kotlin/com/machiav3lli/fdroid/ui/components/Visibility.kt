package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable

@Composable
fun StatefulAnimatedVisibility(
    currentState: Boolean = false,
    enterPositive: EnterTransition,
    exitPositive: ExitTransition,
    enterNegative: EnterTransition,
    exitNegative: ExitTransition,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit),
) {
    AnimatedVisibility(
        visible = currentState,
        enter = enterPositive,
        exit = exitPositive,
        content = expandedView
    )
    AnimatedVisibility(
        visible = !currentState,
        enter = enterNegative,
        exit = exitNegative,
        content = collapsedView
    )
}