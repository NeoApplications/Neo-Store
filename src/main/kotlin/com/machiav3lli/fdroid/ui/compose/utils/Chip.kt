package com.machiav3lli.fdroid.ui.compose.utils

import androidx.compose.material3.ElevatedAssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
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
@OptIn(ExperimentalMaterial3Api::class)
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
        shape = ShapeDefaults.Large,
        onClick = { onClick(text) },
        label = { Text(text = text, color = borderColor) }
    )
}
