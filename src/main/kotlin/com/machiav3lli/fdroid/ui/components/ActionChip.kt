package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
        shape = MaterialTheme.shapes.medium,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(48.dp),
            labelColor = MaterialTheme.colorScheme.onSurface,
            leadingIconContentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = null,
        onClick = onClick
    )
}