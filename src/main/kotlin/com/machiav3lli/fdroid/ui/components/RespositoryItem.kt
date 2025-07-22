package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
            Text(
                text = repository.name.trim(),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1
            )
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