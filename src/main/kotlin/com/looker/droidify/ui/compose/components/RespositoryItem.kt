package com.looker.droidify.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.looker.droidify.database.entity.Repository

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepositoryItem(
    modifier: Modifier = Modifier,
    repository: Repository,
    onClick: (Repository) -> Unit = {},
    onLongClick: (Repository) -> Unit = {}
) {
    var (isEnabled, enable) = remember(repository.enabled) {
        mutableStateOf(repository.enabled)
    }
    val backgroundColor by animateColorAsState(
        targetValue = if (isEnabled) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.background
    )

    Surface(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
            RepositoryItemText(
                repositoryName = repository.name,
                repositoryDescription = repository.description
            )
            AnimatedVisibility(visible = isEnabled) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Repository Enabled"
                )
            }
        }
    }
}

@Composable
private fun RepositoryItemText(
    modifier: Modifier = Modifier,
    repositoryName: String,
    repositoryDescription: String?
) {
    Column(
        modifier = modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = repositoryName.trim(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
        repositoryDescription?.trim()?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

//@Preview
@Composable
fun RepositoryItemPreview() {
    RepositoryItem(
        repository = Repository(
            name = "Test Repo",
            description = "Test Repo description written by looker, You are welcome"
        ),
        onClick = {}
    )
}