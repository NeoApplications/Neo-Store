package com.machiav3lli.fdroid.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TitleText(
    text: String?,
    color: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier,
) = Text(
    text = text.orEmpty(),
    style = MaterialTheme.typography.titleMedium,
    fontWeight = FontWeight.Bold,
    color = color,
    modifier = modifier
)

@Composable
fun BlockText(
    text: String?,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    monospace: Boolean = false,
    modifier: Modifier = Modifier,
) = Text(
    text = text.orEmpty(),
    style = MaterialTheme.typography.bodyLarge,
    color = color,
    modifier = modifier,
    fontFamily = if (monospace) FontFamily.Monospace else null,
)