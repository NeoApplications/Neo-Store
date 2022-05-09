package com.looker.droidify.ui.compose.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.Chip
import androidx.compose.material.ChipDefaults.chipColors
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// TODO: Rename

/**
 * Basically a OutlineChip without spamming "ExperimentalMaterialApi"
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomChip(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    borderWidth: Dp = 1.dp,
    onClick: (String) -> Unit = {}
) {
    Chip(
        modifier = modifier,
        shape = Shapes.Full,
        border = BorderStroke(
            width = borderWidth,
            color = borderColor.compositeOverBackground(
                alpha = 0.5f,
                MaterialTheme.colorScheme.surface
            )
        ),
        colors = chipColors(
            backgroundColor = containerColor.compositeOverBackground(alpha = 0.1f)
        ),
        onClick = { onClick(text) }
    ) {
        Text(text = text, color = borderColor)
    }
}
