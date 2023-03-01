package com.machiav3lli.fdroid.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
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
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.database.entity.IconDetails
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.network.CoilDownloader
import com.machiav3lli.fdroid.service.DownloadService
import com.machiav3lli.fdroid.ui.compose.components.appsheet.DownloadProgress
import com.machiav3lli.fdroid.ui.compose.utils.NetworkImage

@Composable
fun DownloadsListItem(
    item: ProductItem,
    repo: Repository? = null,
    installed: Installed? = null,
    state: DownloadService.State,
    onUserClick: (ProductItem) -> Unit = {},
) {
    val product by remember(item) { mutableStateOf(item) }
    val imageData by remember(product, repo) {
        mutableStateOf(
            CoilDownloader.createIconUri(
                product.packageName,
                product.icon,
                product.metadataIcon,
                repo?.address,
                repo?.authentication
            ).toString()
        )
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onUserClick(product) }
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NetworkImage(
            modifier = Modifier.size(64.dp),
            data = imageData
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .height(64.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
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
            DownloadProgress(
                modifier = Modifier.padding(horizontal = 4.dp),
                totalSize = if (state is DownloadService.State.Downloading) state.total
                    ?: 1L else 1L,
                isIndeterminate = state !is DownloadService.State.Downloading,
                downloaded = if (state is DownloadService.State.Downloading) state.read else 0L,
            )
        }
    }
}

@Composable
fun DownloadedItem(
    item: Downloaded,
    iconDetails: IconDetails?,
    repo: Repository? = null,
    installed: Installed? = null,
    state: DownloadService.State,
    onUserClick: (Downloaded) -> Unit = {},
) {
    val download by remember(item) { mutableStateOf(item) }
    val imageData by remember(download, repo) {
        mutableStateOf(
            CoilDownloader.createIconUri(
                download.packageName,
                iconDetails?.icon ?: "",
                iconDetails?.metadataIcon ?: "",
                repo?.address,
                repo?.authentication
            ).toString()
        )
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onUserClick(download) }
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NetworkImage(
            modifier = Modifier.size(48.dp),
            data = imageData
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .height(48.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Text(
                    text = download.state.name,
                    modifier = Modifier
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = if (installed != null && installed.version != state.version) "${download.version} → ${state.version}"
                    else state.version,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            DownloadProgress(
                modifier = Modifier.padding(horizontal = 4.dp),
                totalSize = if (state is DownloadService.State.Downloading) state.total
                    ?: 1L else 1L,
                isIndeterminate = state is DownloadService.State.Pending || state is DownloadService.State.Connecting,
                downloaded = when (state) {
                    is DownloadService.State.Downloading -> state.read
                    is DownloadService.State.Success     -> 1L
                    else                                 -> 0L
                },
            )
        }
    }
}
