package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    preExpanded: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    onClick: () -> Unit = {},
    expandedContent: @Composable () -> Unit = {},
    mainContent: @Composable () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(preExpanded) }
    val background by animateColorAsState(
        targetValue = if (expanded) MaterialTheme.colorScheme.surfaceVariant else backgroundColor
    )

    Surface(
        modifier = Modifier
            .animateContentSize()
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = !expanded }
            ),
        color = background
    ) {
        Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
            mainContent()
            AnimatedVisibility(
                modifier = Modifier.align(Alignment.End),
                visible = expanded,
                enter = fadeIn() + expandIn(expandFrom = Alignment.TopEnd),
                exit = fadeOut() + shrinkOut(shrinkTowards = Alignment.TopEnd)
            ) {
                expandedContent()
            }
        }
    }
}