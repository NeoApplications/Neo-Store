package com.machiav3lli.fdroid.ui.components.appsheet

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.RELEASE_STATE_INSTALLED
import com.machiav3lli.fdroid.RELEASE_STATE_NONE
import com.machiav3lli.fdroid.RELEASE_STATE_SUGGESTED
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.RBLog
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldCheck
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldSlash
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldWarning
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.text.formatSize
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.TimeZone

@Composable
fun ReleaseItem(
    modifier: Modifier = Modifier,
    release: Release,
    repository: Repository,
    rbLog: RBLog?,
    releaseState: Int = RELEASE_STATE_NONE,
    onDownloadClick: (Release) -> Unit = {},
    onShareClick: (Release) -> Unit = {},
) {
    val currentRelease by remember { mutableStateOf(release) }
    val isInstalled = releaseState == RELEASE_STATE_INSTALLED
    val isSuggested = releaseState == RELEASE_STATE_SUGGESTED
    val container by animateColorAsState(
        targetValue = if (isSuggested or isInstalled)
            MaterialTheme.colorScheme.surfaceContainerHighest
        else MaterialTheme.colorScheme.surfaceContainer, label = "containerColor"
    )
    val border by animateColorAsState(
        targetValue = if (isSuggested or isInstalled)
            MaterialTheme.colorScheme.primaryContainer
        else Color.Transparent, label = "borderColor"
    )

    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, border, MaterialTheme.shapes.large)
            .clip(MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(
            containerColor = container,
        ),
        headlineContent = {
            ReleaseTitleWithBadge(
                version = currentRelease.version
            ) {
                if (currentRelease.platforms.size == 1) {
                    ReleaseBadge(
                        text = currentRelease.platforms.first()
                    )
                }
                if (isSuggested or isInstalled) {
                    val badgeText = remember { mutableIntStateOf(R.string.suggested) }
                    LaunchedEffect(isInstalled, isSuggested) {
                        badgeText.intValue =
                            if (isInstalled) R.string.app_installed else R.string.suggested
                    }
                    ReleaseBadge(
                        text = stringResource(id = badgeText.intValue)
                    )
                }
                if (rbLog != null) Icon(
                    imageVector = when (rbLog.reproducible) {
                        true  -> Phosphor.ShieldCheck
                        false -> Phosphor.ShieldSlash
                        else  -> Phosphor.ShieldWarning
                    },
                    contentDescription = stringResource(id = R.string.rb_badge),
                    tint = when (rbLog.reproducible) {
                        true  -> MaterialTheme.colorScheme.primary
                        false -> MaterialTheme.colorScheme.tertiaryContainer
                        else  -> MaterialTheme.colorScheme.error
                    }
                )
            }
        },
        supportingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ReleaseItemBottomText(
                    repository = repository.name,
                    date = if (Android.sdk(Build.VERSION_CODES.O)) {
                        LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(currentRelease.added),
                            TimeZone.getDefault().toZoneId()
                        ).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    } else ""
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentRelease.size.formatSize(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { onShareClick(currentRelease) }) {
                        Icon(
                            imageVector = Phosphor.ShareNetwork,
                            contentDescription = stringResource(id = R.string.share),
                        )
                    }
                    if (!Preferences[Preferences.Key.KidsMode]) {
                        ActionButton(
                            text = stringResource(id = R.string.install),
                            icon = Phosphor.Download,
                            positive = true,
                            onClick = { onDownloadClick(currentRelease) }
                        )
                    }
                }
            }
        }

    )
}

@Composable
fun ReleaseTitleWithBadge(
    modifier: Modifier = Modifier,
    version: String,
    badges: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = version,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
        )
        badges()
    }
}

@Composable
fun ReleaseItemBottomText(
    modifier: Modifier = Modifier,
    repository: String,
    date: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            modifier = modifier.weight(1f),
            text = stringResource(id = R.string.provided_by_FORMAT, repository),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
        )
        Text(text = date, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
    }
}

@Composable
fun ReleaseBadge(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.inverseSurface,
    onColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
) {
    Surface(
        modifier = modifier
            .background(color, ShapeDefaults.Large)
            .padding(6.dp, 2.dp),
        color = color,
    ) {
        Text(
            text = text,
            color = onColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
        )
    }
}
