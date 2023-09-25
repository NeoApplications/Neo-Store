package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerBasedShape
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
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    shape: CornerBasedShape = MaterialTheme.shapes.large,
    onClick: () -> Unit = {},
    expandedContent: @Composable () -> Unit = {},
    mainContent: @Composable () -> Unit,
) {
    val background by animateColorAsState(
        targetValue = if (isExpanded.value) MaterialTheme.colorScheme.surfaceVariant else backgroundColor,
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