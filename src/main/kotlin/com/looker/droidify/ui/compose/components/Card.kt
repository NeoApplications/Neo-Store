package com.looker.droidify.ui.compose.components

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.looker.droidify.ui.compose.theme.LocalShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    preExpanded: Boolean = false,
    onClick: () -> Unit = {},
    expandedContent: @Composable () -> Unit = {},
    mainContent: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(preExpanded) }

    Surface(
        modifier = Modifier
            .animateContentSize()
            .clip(RoundedCornerShape(LocalShapes.current.large))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = !expanded }
            ),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = modifier, contentAlignment = Alignment.CenterStart) {
            Column {
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
}