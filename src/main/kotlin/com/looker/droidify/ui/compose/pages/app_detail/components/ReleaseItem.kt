package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.database.entity.Release
import com.looker.droidify.utility.extension.text.formatSize

@Composable
fun ReleaseItem(
    modifier: Modifier = Modifier,
    isSuggested: Boolean = false,
    isInstalled: Boolean = false,
    release: Release,
    onDownloadClick: (Release) -> Unit = {}
) {
    val highlight by animateDpAsState(targetValue = if (isSuggested or isInstalled) 12.dp else 0.dp)
    val currentRelease by remember { mutableStateOf(release) }
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = highlight
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = { onDownloadClick(currentRelease) }) {
                Icon(
                    imageVector = Icons.Rounded.Download,
                    contentDescription = "Download Release"
                )
            }
            Box {
                ReleaseTitleWithBadge(
                    modifier = Modifier.align(Alignment.CenterStart),
                    title = release.version,
                    subtitle = release.targetSdkVersion.toString()
                ) {
                    AnimatedVisibility(visible = isSuggested) {
                        ReleaseBadge(text = stringResource(id = R.string.suggested))
                    }
                }
                ReleaseItemEndText(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    title = release.added.toString(),
                    subtitle = release.size.formatSize()
                )
            }
        }
    }
}

@Composable
fun BoxScope.ReleaseTitleWithBadge(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    badges: @Composable RowScope.() -> Unit = {}
) {
    Column(
        modifier = modifier.matchParentSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            badges()
        }
        Text(text = subtitle, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun BoxScope.ReleaseItemEndText(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier.matchParentSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = title, style = MaterialTheme.typography.bodySmall)
        Text(text = subtitle, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ReleaseBadge(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.secondaryContainer,
    onColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(
        modifier = modifier.padding(8.dp),
        color = color,
        shape = Shapes.Full
    ) {
        Text(text = text, color = onColor)
    }
}