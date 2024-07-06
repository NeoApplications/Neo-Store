package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.database.entity.IconDetails
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.network.createIconUri
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.ui.components.appsheet.DownloadProgress
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Eraser

@Composable
fun DownloadsListItem(
    product: ProductItem,
    repo: Repository? = null,
    installed: Installed? = null,
    state: DownloadState,
    onUserClick: (ProductItem) -> Unit = {},
) {
    var imageData by remember { mutableStateOf<String?>(null) }

    SideEffect {
        imageData = createIconUri(
            product.packageName,
            product.icon,
            product.metadataIcon,
            repo?.address,
            repo?.authentication
        ).toString()
    }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onUserClick(product) }
            .fillMaxWidth(),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        leadingContent = {
            NetworkImage(
                modifier = Modifier.size(PRODUCT_CARD_ICON),
                data = imageData
            )
        },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = product.name,
                    modifier = Modifier
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (installed != null && installed.version != state.version) "${product.installedVersion} → ${state.version}"
                    else state.version,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
        supportingContent = {
            DownloadProgress(
                totalSize = if (state is DownloadState.Downloading) state.total
                    ?: 1L else 1L,
                isIndeterminate = state !is DownloadState.Downloading,
                downloaded = if (state is DownloadState.Downloading) state.read else 0L,
            )
        },
    )
}

@Composable
fun DownloadedItem(
    download: Downloaded,
    iconDetails: IconDetails?,
    repo: Repository? = null,
    state: DownloadState,
    onEraseClick: (() -> Unit)? = null,
    onUserClick: (Downloaded) -> Unit = {},
) {
    var imageData by remember { mutableStateOf<String?>(null) }

    SideEffect {
        imageData = createIconUri(
            download.packageName,
            iconDetails?.icon ?: "",
            iconDetails?.metadataIcon ?: "",
            repo?.address,
            repo?.authentication
        ).toString()
    }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onUserClick(download) }
            .fillMaxWidth(),
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent,
        ),
        leadingContent = {
            NetworkImage(
                modifier = Modifier.size(PRODUCT_CARD_ICON),
                data = imageData
            )
        },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = download.state.name,
                    modifier = Modifier
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = state.version,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        supportingContent = {
            DownloadProgress(
                totalSize = when (state) {
                    is DownloadState.Downloading -> state.total ?: 1L
                    is DownloadState.Cancel      -> 0L
                    is DownloadState.Error       -> -1L
                    else                         -> 1L
                },
                isIndeterminate = state is DownloadState.Pending || state is DownloadState.Connecting,
                downloaded = when (state) {
                    is DownloadState.Downloading -> state.read
                    is DownloadState.Success     -> 1L
                    else                         -> 0L
                },
                finishedTime = download.changed,
            )
        },
        trailingContent = {
            onEraseClick?.let {
                IconButton(onClick = onEraseClick) {
                    Icon(
                        imageVector = Phosphor.Eraser,
                        contentDescription = stringResource(id = R.string.delete),
                    )
                }
            }
        }
    )
}
