package com.machiav3lli.fdroid.ui.components.appsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.ui.components.MainActionButton
import com.machiav3lli.fdroid.ui.components.NetworkImage
import com.machiav3lli.fdroid.ui.components.PRODUCT_CARD_ICON
import com.machiav3lli.fdroid.ui.components.SecondaryActionButton
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.utility.extension.text.formatDateTime
import com.machiav3lli.fdroid.utility.extension.text.formatSize

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppInfoHeader(
    modifier: Modifier = Modifier,
    mainAction: ActionState?,
    possibleActions: Set<ActionState>,
    onAction: (ActionState?) -> Unit = { },
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mainAction != ActionState.Bookmark || mainAction != ActionState.Bookmarked) {
                val secondAction =
                    possibleActions.find { it == ActionState.Bookmark || it == ActionState.Bookmarked }
                SecondaryActionButton(packageState = secondAction) {
                    onAction(secondAction)
                }
            }
            MainActionButton(
                modifier = Modifier.weight(1f),
                actionState = mainAction ?: ActionState.Install,
                onClick = {
                    onAction(mainAction)
                }
            )
        }
        val secondaryActions = possibleActions
            .minus(ActionState.Bookmark)
            .minus(ActionState.Bookmarked)
        AnimatedVisibility(visible = secondaryActions.isNotEmpty()) {
            FlowRow(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                secondaryActions.forEach {
                    SecondaryActionButton(packageState = it) {
                        onAction(it)
                    }
                }
            }
        }
    }
}

@Composable
fun TopBarHeader(
    modifier: Modifier = Modifier,
    icon: String? = null,
    appName: String,
    packageName: String,
    state: DownloadState? = null,
    actions: @Composable () -> Unit = {},
) {
    Column(
        modifier.fillMaxWidth(),
    ) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(
                containerColor = Color.Transparent,
            ),
            leadingContent = {
                NetworkImage(
                    modifier = Modifier.size(PRODUCT_CARD_ICON),
                    data = icon
                )
            },
            headlineContent = {
                Text(text = appName, style = MaterialTheme.typography.titleMedium)
            },
            supportingContent = {
                Text(text = packageName, style = MaterialTheme.typography.bodyMedium)
            },
            trailingContent = {
                actions()
            }
        )

        AnimatedVisibility(visible = state?.isActive ?: false) {
            DownloadProgress(
                modifier = Modifier.padding(horizontal = 4.dp),
                totalSize = if (state is DownloadState.Downloading) state.total ?: 1L else 1L,
                isIndeterminate = state !is DownloadState.Downloading,
                downloaded = if (state is DownloadState.Downloading) state.read else 0L,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    description: String = "",
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(8.dp),
        color = Color.Transparent,
    ) {
        Icon(
            modifier = Modifier.size(PRODUCT_CARD_ICON - 16.dp),
            imageVector = icon,
            contentDescription = description,
        )
    }
}

@Composable
fun DownloadProgress(
    modifier: Modifier = Modifier,
    totalSize: Long,
    downloaded: Long?,
    isIndeterminate: Boolean,
    finishedTime: Long = 0L,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        when {
            isIndeterminate -> {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeDefaults.Large),
                )
            }

            totalSize < 1L -> {
                Text(
                    text = stringResource(
                        id = if (totalSize == 0L) R.string.canceled
                        else R.string.error
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            downloaded == totalSize && totalSize == 1L -> {
                Text(
                    text = "${stringResource(id = R.string.finished)} ${finishedTime.formatDateTime()}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            else -> {
                Text(
                    text = "${downloaded?.formatSize()}/${totalSize.formatSize()}",
                    style = MaterialTheme.typography.bodySmall,
                )
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ShapeDefaults.Large),
                    progress = downloaded?.toFloat()?.div(totalSize) ?: 1f
                )
            }
        }
    }
}

@Composable
fun WarningCard(message: String) {
    ListItem(
        modifier = Modifier.clip(MaterialTheme.shapes.large),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
        leadingContent = {
            Icon(
                imageVector = Phosphor.CircleWavyWarning,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                contentDescription = message,
            )
        },
        headlineContent = {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
    )
}
