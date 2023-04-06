package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Check

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepositoryItem(
    modifier: Modifier = Modifier,
    repository: Repository,
    onClick: (Repository) -> Unit = {},
    onLongClick: (Repository) -> Unit = {}
) {
    val (isEnabled, enable) = remember(repository.enabled) {
        mutableStateOf(repository.enabled)
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.surfaceColorAtElevation(32.dp)
        else MaterialTheme.colorScheme.background
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .combinedClickable(
                onClick = {
                    enable(!isEnabled)
                    onClick(repository.copy(enabled = !repository.enabled))
                },
                onLongClick = { onLongClick(repository) }
            ),
        color = backgroundColor,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 16.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = repository.name.trim(),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isEnabled) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                repository.description.trim().let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
            AnimatedVisibility(visible = isEnabled) {
                Icon(
                    imageVector = Phosphor.Check,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Repository Enabled"
                )
            }
        }
    }
}