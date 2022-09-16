package com.machiav3lli.fdroid.ui.compose.components.appsheet

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.ui.compose.utils.CustomChip
import com.machiav3lli.fdroid.ui.compose.utils.StaggeredGrid

// TODO: Convert Permissions and AntiFeatures to Custom Interface

@Composable
fun PermissionGrid(
    modifier: Modifier = Modifier,
    permissions: List<String>
) {
    StaggeredGrid(modifier = modifier.horizontalScroll(rememberScrollState())) {
        permissions.forEach {
            CustomChip(modifier = Modifier.padding(horizontal = 2.dp), text = it)
        }
    }
}

@Composable
fun AntiFeaturesGrid(
    modifier: Modifier = Modifier,
    antiFeatures: List<String>
) {
    StaggeredGrid(modifier = modifier.horizontalScroll(rememberScrollState())) {
        antiFeatures.forEach {
            CustomChip(
                modifier = Modifier.padding(horizontal = 2.dp),
                text = it,
                containerColor = MaterialTheme.colorScheme.errorContainer,
                borderColor = MaterialTheme.colorScheme.error
            )
        }
    }
}