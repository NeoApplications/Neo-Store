package com.looker.droidify.ui.compose.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.ui.compose.theme.LocalShapes

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepositoryItem(
    modifier: Modifier = Modifier,
    repository: Repository,
    onClick: () -> Unit,
    onLongClick: (() -> Unit) = {}
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (repository.enabled) MaterialTheme.colorScheme.surfaceVariant
        else MaterialTheme.colorScheme.background
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        color = backgroundColor,
        shape = RoundedCornerShape(LocalShapes.current.large)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 16.dp
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RepositoryItemText(
                repositoryName = repository.name,
                repositoryDescription = repository.description
            )
            AnimatedVisibility(visible = repository.enabled) {
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
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = repositoryName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        repositoryDescription?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview
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