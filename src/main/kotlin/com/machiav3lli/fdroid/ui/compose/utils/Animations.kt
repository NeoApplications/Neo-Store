package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment

@Composable
fun HorizontalExpandingVisibility(
    expanded: Boolean = false,
    from: Alignment.Horizontal = Alignment.Start,
    towards: Alignment.Horizontal = Alignment.End,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit)
) {
    AnimatedVisibility(
        visible = expanded,
        enter = expandHorizontally(expandFrom = from),
        exit = shrinkHorizontally(shrinkTowards = from),
        content = expandedView
    )
    AnimatedVisibility(
        visible = !expanded,
        enter = expandHorizontally(expandFrom = towards),
        exit = shrinkHorizontally(shrinkTowards = towards),
        content = collapsedView
    )
}