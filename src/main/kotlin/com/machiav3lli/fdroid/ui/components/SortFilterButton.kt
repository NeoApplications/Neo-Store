package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
