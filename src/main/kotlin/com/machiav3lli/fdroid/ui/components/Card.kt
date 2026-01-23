package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    isExpanded: MutableState<Boolean> = mutableStateOf(false),
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    onClick: () -> Unit = {},
    expandedContent: @Composable () -> Unit = {},
    mainContent: @Composable () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (isExpanded.value) MaterialTheme.colorScheme.surfaceContainerHigh
        else Color.Transparent,
        label = "backgroundColor",
    )

    Surface(
        modifier = Modifier
            .animateContentSize()
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { isExpanded.value = !isExpanded.value }
            ),
        color = background
    ) {
        Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
            mainContent()
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(8.dp),
                visible = isExpanded.value,
                enter = fadeIn() + expandIn(expandFrom = Alignment.TopEnd),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopEnd)
            ) {
                expandedContent()
            }
        }
    }
}

@Composable
fun BlockCard(
    modifier: Modifier = Modifier,
    heading: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(modifier = modifier.padding(8.dp)) {
            ExpandablePrivacyHeader(heading)
            content()
        }
    }
}

@Composable
fun ExpandingFadingCard(
    expanded: Boolean = false,
    expandedView: @Composable (AnimatedVisibilityScope.() -> Unit),
    collapsedView: @Composable (AnimatedVisibilityScope.() -> Unit),
) {
    val bgColor by animateColorAsState(
        if (expanded) MaterialTheme.colorScheme.surfaceContainerHigh
        else FloatingActionButtonDefaults.containerColor,
        label = "bgColor"
    )

    Surface(
        modifier = Modifier
            .animateContentSize(),
        shape = MaterialTheme.shapes.large,
        color = bgColor,
    ) {
        StatefulAnimatedVisibility(
            currentState = expanded,
            enterPositive = fadeIn(tween(100)) + expandIn(tween(200)),
            exitPositive = fadeOut(tween(100)) + shrinkOut(tween(200)),
            enterNegative = fadeIn(tween(100)) + expandIn(tween(200)),
            exitNegative = fadeOut(tween(100)) + shrinkOut(tween(200)),
            collapsedView = collapsedView,
            expandedView = expandedView
        )
    }
}