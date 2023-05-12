package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R

@Composable
fun ActionChip(
    modifier: Modifier = Modifier,
    text: String,
    icon: ImageVector,
    positive: Boolean = true,
    onClick: () -> Unit = {},
) {
    AssistChip(
        modifier = modifier,
        label = {
            Text(text = text)
        },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = icon,
                contentDescription = stringResource(id = R.string.sort_filter)
            )
        },
        shape = MaterialTheme.shapes.large,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (positive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.tertiaryContainer,
            labelColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
            leadingIconContentColor = if (positive) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onTertiaryContainer,
        ),
        border = null,
        onClick = onClick
    )
}