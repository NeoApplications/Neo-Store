package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
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
    val currentRelease by remember { mutableStateOf(release) }
    val highlight by animateDpAsState(targetValue = if (isSuggested or isInstalled) 8.dp else 0.dp)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.large,
        tonalElevation = highlight
    ) {
        ReleaseItemContent(
            release = currentRelease,
            isSuggested = isSuggested,
            isInstalled = isInstalled,
            onDownloadClick = onDownloadClick
        )
    }
}

@Composable
fun ReleaseItemContent(
    modifier: Modifier = Modifier,
    isSuggested: Boolean = false,
    isInstalled: Boolean = false,
    release: Release,
    onDownloadClick: (Release) -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        IconButton(onClick = { onDownloadClick(release) }) {
            Icon(
                imageVector = Icons.Rounded.Download,
                contentDescription = "Download this version"
            )
        }
        Box(
            modifier = modifier
                .height(74.dp)
                .fillMaxWidth()
        ) {
            ReleaseTitleWithBadge(
                modifier = Modifier.align(Alignment.CenterStart),
                version = release.version,
                repository = release.targetSdkVersion.toString()
            ) {
                AnimatedVisibility(
                    visible = isSuggested or isInstalled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val badgeText = remember { mutableStateOf(R.string.suggested) }
                    LaunchedEffect(isInstalled, isSuggested) {
                        badgeText.value =
                            if (isSuggested && isInstalled) R.string.installed else R.string.suggested
                    }
                    ReleaseBadge(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(id = badgeText.value)
                    )
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

@Composable
fun ReleaseTitleWithBadge(
    modifier: Modifier = Modifier,
    version: String,
    repository: String,
    badges: @Composable RowScope.() -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.width(Dp.Hairline))
            Text(text = version, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(id = R.string.provided_by_FORMAT, repository),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(Dp.Hairline))
        }
        badges()
    }
}

@Composable
fun ReleaseItemEndText(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.width(Dp.Hairline))
        Text(text = title, style = MaterialTheme.typography.bodySmall)
        Text(text = subtitle, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.width(Dp.Hairline))
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
        modifier = modifier
            .background(color, Shapes.Full)
            .padding(8.dp),
        color = color
    ) {
        Text(text = text, color = onColor)
    }
}