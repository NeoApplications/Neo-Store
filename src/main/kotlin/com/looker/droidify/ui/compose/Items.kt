package com.looker.droidify.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.ui.compose.components.ExpandableCard
import com.looker.droidify.ui.compose.components.NetworkImage
import com.looker.droidify.ui.compose.theme.AppTheme

@Composable
fun ProductRow(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {}
) {
    val imageData by remember(item, repo) {
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

    ExpandableCard(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        onClick = { onUserClick(item) },
        expandedContent = { ExpandedItemContent(item = item) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                modifier = Modifier.size(64.dp),
                data = imageData
            )

            Column(
                modifier = Modifier.requiredHeight(64.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
                    Text(
                        text = item.name,
                        modifier = Modifier.align(Alignment.CenterStart),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = item.version,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        modifier = Modifier.fillMaxHeight(),
                        text = item.summary,
                        style = MaterialTheme.typography.bodySmall,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2
                    )
                }
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

    val imageData by remember(item, repo) {
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
            .requiredSize(80.dp, 116.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable(onClick = { onUserClick(item) }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NetworkImage(
            modifier = Modifier.size(64.dp),
            data = imageData
        )

        Text(
            modifier = Modifier.padding(4.dp, 2.dp),
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                modifier = Modifier.padding(4.dp, 1.dp),
                text = item.version,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ExpandedItemContent(
    modifier: Modifier = Modifier,
    item: ProductItem,
    favourite: Boolean = false,
    onFavourite: (ProductItem) -> Unit = {},
    onInstallClicked: (ProductItem) -> Unit = {}
) {
    Box(contentAlignment = Alignment.CenterEnd) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onFavourite(item) }) {
                Icon(
                    imageVector = if (favourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Add to Favourite",
                    tint = if (favourite) Color.Red else MaterialTheme.colorScheme.outline
                )
            }
            FilledTonalButton(
                colors = buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                onClick = { onInstallClicked(item) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_download),
                    contentDescription = "Add to Favourite"
                )
                Text(text = "Install")
            }
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