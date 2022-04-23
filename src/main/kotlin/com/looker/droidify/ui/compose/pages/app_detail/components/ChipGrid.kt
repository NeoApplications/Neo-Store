package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.looker.droidify.ui.compose.utils.CustomChip
import com.looker.droidify.ui.compose.utils.StaggeredGrid

@Composable
fun PermissionGrid(
    modifier: Modifier = Modifier,
    permissions: List<String>
) {
    StaggeredGrid(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        rows = 2
    ) {
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
    StaggeredGrid(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        rows = 2
    ) {
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