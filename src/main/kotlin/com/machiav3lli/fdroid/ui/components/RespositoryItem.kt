package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Badge
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.database.entity.Repository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepositoryItem(
    modifier: Modifier = Modifier,
    repository: Repository,
    onSwitch: (Repository) -> Unit = {},
    onClick: (Repository) -> Unit = {},
) {
    val (isEnabled, enable) = remember(repository.enabled) {
        mutableStateOf(repository.enabled)
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainer,
        label = "backgroundColor",
    )

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable {
                onClick(repository)
            },
        colors = ListItemDefaults.colors(
            containerColor = backgroundColor,
        ),
        headlineContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = repository.name.trim(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                if (repository.trusted) Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(stringResource(R.string.trusted_label))
                }
            }
        },
        supportingContent = {
            repository.description.trim().let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
        },
        trailingContent = {
            Switch(
                checked = isEnabled,
                colors = SwitchDefaults.colors(uncheckedBorderColor = Color.Transparent),
                onCheckedChange = {
                    enable(!isEnabled)
                    onSwitch(repository)
                }
            )
        }
    )
}