package com.looker.droidify.ui.compose.pages.app_detail.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.ui.compose.components.InstallButton
import com.looker.droidify.ui.compose.theme.LocalShapes
import com.looker.droidify.ui.compose.utils.*
import com.looker.droidify.utility.extension.text.formatSize

@Composable
fun Header(
    modifier: Modifier = Modifier,
    icon: String?,
    appName: String,
    packageName: String,
    versionCode: String,
    appSize: String,
    appDev: String
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LocalShapes.current.large)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            HeaderTitle(
                modifier = Modifier.fillMaxWidth(),
                icon = icon,
                appName = appName,
                packageName = packageName
            )
            HeaderExtra(
                versionCode = versionCode,
                appSize = appSize,
                appDev = appDev
            )
            DownloadProgress(totalSize = 69420)
            var buttonState by remember { mutableStateOf<ButtonStates>(Connecting) }

            InstallButton(buttonState = buttonState) {
                buttonState = when (it) {
                    Connecting -> Download
                    Download -> Downloading
                    Downloading -> Install
                    Install -> Installing
                    Installing -> Launch
                    Launch -> Connecting
                }
            }
        }
    }
}

@Composable
fun HeaderTitle(
    modifier: Modifier = Modifier,
    icon: String? = null,
    appName: String,
    packageName: String,
    actions: @Composable () -> Unit = {}
) {
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
}

@Composable
fun HeaderExtra(
    modifier: Modifier = Modifier,
    versionCode: String,
    appSize: String,
    appDev: String
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
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
            text = appDev
        )
    }
}

@Composable
fun HeaderExtrasCard(
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
    title: String,
    text: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            extra()
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Text(text = text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun DownloadProgress(
    modifier: Modifier = Modifier,
    downloading: Float = 0f,
    totalSize: Long
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = totalSize.times(downloading).toLong().formatSize() + "/" + totalSize.formatSize()
        )
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), progress = downloading)
    }
}