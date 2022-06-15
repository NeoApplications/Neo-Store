package com.looker.droidify.ui.compose.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// TODO: Rename

/**
 * Basically a OutlineChip without spamming "ExperimentalMaterialApi"
 */
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomChip(
    modifier: Modifier = Modifier,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    borderWidth: Dp = 1.dp,
    onClick: (String) -> Unit = {}
) {
    ElevatedAssistChip(
        modifier = modifier,
        shape = Shapes.Full,
        onClick = { onClick(text) },
        label = { Text(text = text, color = borderColor) }
    )
}
