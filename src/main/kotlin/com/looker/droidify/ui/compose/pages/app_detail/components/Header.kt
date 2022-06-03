package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.entity.ActionState
import com.looker.droidify.entity.ComponentState
import com.looker.droidify.entity.DownloadState
import com.looker.droidify.ui.compose.components.MainActionButton
import com.looker.droidify.ui.compose.components.SecondaryActionButton
import com.looker.droidify.ui.compose.utils.NetworkImage
import com.looker.droidify.utility.extension.text.formatSize

@Composable
fun AppInfoHeader(
    modifier: Modifier = Modifier,
    versionCode: String,
    appSize: String,
    appDev: String,
    mainAction: ActionState?,
    secondaryAction: ComponentState? = null,
    onSource: () -> Unit = { },
    onSourceLong: () -> Unit = { },
    onAction: () -> Unit = { },
    onSecondaryAction: () -> Unit = { }
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderExtra(
                versionCode = versionCode,
                appSize = appSize,
                appDev = appDev,
                onSource = onSource,
                onSourceLong = onSourceLong
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SecondaryActionButton(packageState = secondaryAction, onClick = {
                    onSecondaryAction()
                })
                MainActionButton(
                    modifier = Modifier.weight(1f),
                    actionState = mainAction ?: ActionState.NoAction,
                    onClick = {
                        onAction()
                    }
                )
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
    actions: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 0.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Column {
            Row(
                modifier = modifier.padding(16.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NetworkImage(
                    modifier = Modifier.size(56.dp),
                    data = icon
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceAround
                ) {
                    Text(text = appName, style = MaterialTheme.typography.titleLarge)
                    Text(text = packageName, style = MaterialTheme.typography.bodyMedium)
                }
                Box { actions() }
            }

            AnimatedVisibility(visible = state is DownloadState) {
                DownloadProgress(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    totalSize = if (state is DownloadState.Downloading) state.total ?: 1L else 1L,
                    isIndeterminate = state !is DownloadState.Downloading,
                    downloaded = if (state is DownloadState.Downloading) state.downloaded else 0L,
                )
            }
        }
    }
}

@Composable
fun HeaderExtra(
    modifier: Modifier = Modifier,
    versionCode: String,
    appSize: String,
    appDev: String,
    onSource: () -> Unit,
    onSourceLong: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HeaderExtrasCard(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.version),
            text = versionCode
        )
        HeaderExtrasCard(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.size),
            text = appSize
        )
        HeaderExtrasCard(
            modifier = Modifier.weight(1f),
            title = stringResource(id = R.string.source_code),
            text = appDev,
            onClick = onSource,
            onLongClick = onSourceLong
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeaderExtrasCard(
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
    title: String,
    text: String,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            extra()
            Text(text = title, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            Text(text = text, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
    }
}

@Composable
fun DownloadProgress(
    modifier: Modifier = Modifier,
    totalSize: Long,
    downloaded: Long?,
    isIndeterminate: Boolean
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        if (isIndeterminate) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.Full),
            )
        } else {
            Text(
                text = "${downloaded?.formatSize()}/${totalSize.formatSize()}"
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.Full),
                progress = downloaded?.toFloat()?.div(totalSize) ?: 1f
            )
        }
    }
}