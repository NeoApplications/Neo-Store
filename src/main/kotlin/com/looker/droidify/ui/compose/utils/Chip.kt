package com.looker.droidify.ui.compose.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChipRow(
    modifier: Modifier = Modifier,
    list: List<String>,
    chipColors: ChipColors = ChipDefaults.chipColors(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary.copy(alpha = ChipDefaults.ContentOpacity),
    ),
    shapes: Shape = RoundedCornerShape(50),
    onClick: (String) -> Unit
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(list) {
            Chip(
                shape = shapes,
                colors = chipColors,
                onClick = { onClick(it) }
            ) {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = ChipDefaults.ContentOpacity)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectableChipRow(
    modifier: Modifier = Modifier,
    list: List<String>,
    chipColors: SelectableChipColors = ChipDefaults.filterChipColors(
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        selectedBackgroundColor = MaterialTheme.colorScheme.primary,
        selectedContentColor = MaterialTheme.colorScheme.onPrimary
    ),
    shapes: Shape = RoundedCornerShape(50),
    onClick: (String) -> Unit
) {
    var selected by remember { mutableStateOf(list[0]) }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(list) {
            FilterChip(
                shape = shapes,
                colors = chipColors,
                selected = it == selected,
                onClick = {
                    onClick(it)
                    selected = it
                }
            ) {
                Text(
                    text = it,
                    color = chipColors.contentColor(enabled = true, selected = it == selected).value
                )
            }
        }
    }
}