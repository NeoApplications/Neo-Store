package com.looker.droidify.ui.compose.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.looker.droidify.ui.compose.theme.LocalShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpandableCard(
    modifier: Modifier = Modifier,
    preExpanded: Boolean = false,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    shape: CornerBasedShape = RoundedCornerShape(LocalShapes.current.large),
    onClick: () -> Unit = {},
    expandedContent: @Composable () -> Unit = {},
    mainContent: @Composable () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(preExpanded) }
    val cardElevation by animateDpAsState(targetValue = if (expanded) 12.dp else 0.dp)
    val background by animateColorAsState(targetValue = backgroundColor)

    Surface(
        modifier = Modifier
            .animateContentSize()
            .clip(shape)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { expanded = !expanded }
            ),
        tonalElevation = cardElevation,
        color = background
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