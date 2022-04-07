package com.looker.droidify.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.ui.compose.utils.ExpandableCard
import com.looker.droidify.ui.compose.utils.NetworkImage

@Composable
fun ProductsListItem(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {},
    onFavouriteClick: (ProductItem) -> Unit = {},
    onInstallClick: (ProductItem) -> Unit = {}
) {
    val imageData by remember(item, repo) {
        mutableStateOf(
            CoilDownloader.createIconUri(
                item.packageName,
                item.icon,
                item.metadataIcon,
                repo?.address,
                repo?.authentication
            ).toString()
        )
    }

    ExpandableCard(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        onClick = { onUserClick(item) },
        expandedContent = {
            ExpandedItemContent(
                item = item,
                onFavourite = onFavouriteClick,
                onInstallClicked = onInstallClick
            )
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NetworkImage(
                modifier = Modifier.size(56.dp),
                data = imageData
            )

            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.4f),
                ) {
                    Text(
                        text = item.name,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .weight(1f),
                        softWrap = true,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = item.version,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(),
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
                    contentDescription = "Install"
                )
                Text(text = "Install")
            }
        }
    }
}

@Preview
@Composable
fun ProductsListItemPreview() {
    ProductsListItem(ProductItem())
}