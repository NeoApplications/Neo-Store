package com.looker.droidify.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.ui.compose.components.NetworkImage
import com.looker.droidify.ui.compose.theme.AppTheme

@Composable
fun ProductRow(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {}
) {

    val imageData by remember {
        mutableStateOf(
            CoilDownloader.createIconUri(
                item.packageName,
                item.icon,
                item.metadataIcon,
                repo?.address,
                repo?.authentication
            )
        )
    }

    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = { onUserClick(item) })
            .padding(8.dp)
    ) {
        NetworkImage(
            modifier = Modifier.size(56.dp),
            data = imageData
        )

        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
                .requiredHeight(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    fontWeight = FontWeight.Bold,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = item.version,
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.fillMaxHeight(),
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun ProductColumn(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {}
) {

    val imageData by remember {
        mutableStateOf(
            CoilDownloader.createIconUri(
                item.packageName,
                item.icon,
                item.metadataIcon,
                repo?.address,
                repo?.authentication
            )
        )
    }

    Column(
        modifier = Modifier
            .padding(4.dp)
            .requiredSize(72.dp, 96.dp)
            .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = { onUserClick(item) })
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NetworkImage(
            modifier = Modifier.size(56.dp),
            data = imageData
        )

        Text(
            text = item.name,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = item.version,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
fun ProductColumnPreview() {
    AppTheme(darkTheme = false) {
        ProductColumn(ProductItem())
    }
}

@Preview
@Composable
fun ProductColumnDarkPreview() {
    AppTheme(darkTheme = true) {
        ProductColumn(ProductItem())
    }
}

@Preview
@Composable
fun ProductRowPreview() {
    AppTheme(darkTheme = false) {
        ProductRow(ProductItem())
    }
}

@Preview
@Composable
fun ProductRowDarkPreview() {
    AppTheme(darkTheme = true) {
        ProductRow(ProductItem())
    }
}