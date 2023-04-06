package com.machiav3lli.fdroid.ui.components.appsheet

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.RELEASE_STATE_INSTALLED
import com.machiav3lli.fdroid.RELEASE_STATE_NONE
import com.machiav3lli.fdroid.RELEASE_STATE_SUGGESTED
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

@Composable
fun ReleaseItem(
    modifier: Modifier = Modifier,
    release: Release,
    repository: Repository,
    releaseState: Int = RELEASE_STATE_NONE,
    onDownloadClick: (Release) -> Unit = {},
    onShareClick: (Release) -> Unit = {},
) {
    val currentRelease by remember { mutableStateOf(release) }
    val isInstalled = releaseState == RELEASE_STATE_INSTALLED
    val isSuggested = releaseState == RELEASE_STATE_SUGGESTED
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
            repository = repository,
            isSuggested = isSuggested,
            isInstalled = isInstalled,
            onDownloadClick = onDownloadClick,
            onShareClick = onShareClick
        )
    }
}

@Composable
fun ReleaseItemContent(
    modifier: Modifier = Modifier,
    release: Release,
    repository: Repository,
    isSuggested: Boolean = false,
    isInstalled: Boolean = false,
    onDownloadClick: (Release) -> Unit = {},
    onShareClick: (Release) -> Unit = {}
) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            ReleaseTitleWithBadge(
                version = release.version
            ) {
                AnimatedVisibility(
                    visible = release.platforms.size == 1,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ReleaseBadge(
                        modifier = Modifier.padding(top = 8.dp),
                        text = release.platforms.first()
                    )
                }
                AnimatedVisibility(
                    visible = isSuggested or isInstalled,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val badgeText = remember { mutableStateOf(R.string.suggested) }
                    LaunchedEffect(isInstalled, isSuggested) {
                        badgeText.value =
                            if (isInstalled) R.string.app_installed else R.string.suggested
                    }
                    ReleaseBadge(
                        modifier = Modifier.padding(top = 8.dp),
                        text = stringResource(id = badgeText.value)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            ReleaseItemBottomText(
                repository = repository.name,
                date = if (Android.sdk(Build.VERSION_CODES.O)) {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(release.added),
                        TimeZone.getDefault().toZoneId()
                    ).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                } else ""
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = release.size.formatSize(), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { onShareClick(release) }) {
                    Icon(
                        imageVector = Phosphor.ShareNetwork,
                        contentDescription = stringResource(id = R.string.share),
                    )
                }
                ActionButton(
                    text = stringResource(id = R.string.install),
                    icon = Phosphor.Download,
                    positive = true,
                    onClick = { onDownloadClick(release) }
                )
            }
        }
    }
}

@Composable
fun ReleaseTitleWithBadge(
    modifier: Modifier = Modifier,
    version: String,
    badges: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = version, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.weight(1f))
        badges()
    }
}

@Composable
fun ReleaseItemBottomText(
    modifier: Modifier = Modifier,
    repository: String,
    date: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = modifier.weight(1f),
            text = stringResource(id = R.string.provided_by_FORMAT, repository),
            style = MaterialTheme.typography.labelMedium,
            maxLines = 2
        )
        Text(text = date, style = MaterialTheme.typography.labelSmall)
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
            .background(color, ShapeDefaults.Large)
            .padding(6.dp, 2.dp),
        color = color
    ) {
        Text(text = text, color = onColor, style = MaterialTheme.typography.labelMedium)
    }
}
