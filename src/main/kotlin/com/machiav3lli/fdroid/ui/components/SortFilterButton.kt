package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Asterisk
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple

@Composable
fun SortFilterChip(
    notModified: Boolean,
    fullWidth: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.TopStart,
    ) {
        ActionChip(
            text = stringResource(id = R.string.sort_filter),
            icon = Phosphor.FunnelSimple,
            fullWidth = fullWidth,
            onClick = onClick
        )

        if (!notModified) {
            Icon(
                modifier = Modifier.align(Alignment.TopEnd),
                imageVector = Phosphor.Asterisk,
                contentDescription = stringResource(id = R.string.state_modified),
            )
        }
    }
}

@Composable
fun SortFilterButton(
    notModified: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.TopStart,
    ) {
        FloatingActionButton(
            shape = MaterialTheme.shapes.extraLarge,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(0.dp),
            onClick = onClick
        ) {
            Icon(
                imageVector = Phosphor.FunnelSimple,
                contentDescription = stringResource(id = R.string.sort_filter)
            )
        }

        if (!notModified) {
            Icon(
                modifier = Modifier.align(Alignment.TopEnd),
                imageVector = Phosphor.Asterisk,
                contentDescription = stringResource(id = R.string.state_modified),
            )
        }
    }
}
