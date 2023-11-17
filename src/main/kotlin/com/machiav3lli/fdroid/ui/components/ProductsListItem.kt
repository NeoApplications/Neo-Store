package com.machiav3lli.fdroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.network.createIconUri
import com.machiav3lli.fdroid.ui.components.appsheet.ReleaseBadge
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraightFill
import com.machiav3lli.fdroid.utility.onLaunchClick

@Composable
fun ProductsListItem(
    item: ProductItem,
    repo: Repository? = null,
    isFavorite: Boolean = false,
    onUserClick: (ProductItem) -> Unit = {},
    onFavouriteClick: (ProductItem) -> Unit = {},
    installed: Installed? = null,
    onActionClick: (ProductItem) -> Unit = {},
) {
    val product by remember(item) { mutableStateOf(item) }
    val isExpanded = rememberSaveable { mutableStateOf(false) }

    ExpandableCard(
        isExpanded = isExpanded,
        onClick = { onUserClick(product) },
        expandedContent = {
            ExpandedItemContent(
                item = product,
                installed = installed,
                favourite = isFavorite,
                onFavourite = onFavouriteClick,
                onActionClicked = onActionClick
            )
        }
    ) {
        ProductItemContent(
            product = product,
            repo = repo,
            installed = installed,
            isExpanded = isExpanded,
        )
    }
}

@Composable
fun ProductItemContent(
    product: ProductItem,
    repo: Repository? = null,
    installed: Installed? = null,
    isExpanded: MutableState<Boolean> = mutableStateOf(false),
) {
    val imageData by remember(product, repo) {
        mutableStateOf(
            createIconUri(
                product.packageName,
                product.icon,
                product.metadataIcon,
                repo?.address,
                repo?.authentication
            ).toString()
        )
    }

    ListItem(
        modifier = Modifier.fillMaxWidth(),
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    modifier = Modifier
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                if (product.canUpdate) ReleaseBadge(
                    text = "${product.installedVersion} → ${product.version}",
                )
                else Text(
                    text = installed?.version ?: product.version,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        supportingContent = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = product.summary,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = if (isExpanded.value) Int.MAX_VALUE else 2,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
    )
}

@Composable
fun ProductCarouselItem(
    product: ProductItem,
    repo: Repository? = null,
    installed: Installed? = null,
    favourite: Boolean = false,
    onFavourite: (ProductItem) -> Unit = {},
    onUserClick: (ProductItem) -> Unit = {},
) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity
    val imageData by remember(product, repo) {
        mutableStateOf(
            createIconUri(
                product.packageName,
                product.icon,
                product.metadataIcon,
                repo?.address,
                repo?.authentication
            ).toString()
        )
    }

    val action = when {
        installed == null -> ActionState.Install
        installed.launcherActivities.isNotEmpty() -> ActionState.Launch
        else -> null
    }

    ListItem(
        modifier = Modifier
            .clip(MaterialTheme.shapes.large)
            .clickable { onUserClick(product) }
            .fillMaxSize(),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            Column(
                modifier = Modifier.fillMaxHeight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                NetworkImage(
                    modifier = Modifier.size(PRODUCT_CARD_ICON),
                    data = imageData
                )
            }
        },
        headlineContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = product.name,
                    modifier = Modifier
                        .weight(1f),
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall
                )
                if (product.canUpdate) ReleaseBadge(
                    text = "${product.installedVersion} → ${product.version}",
                ) else Text(
                    text = installed?.version ?: product.version,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        supportingContent = {
            Row {
                Text(
                    modifier = Modifier.weight(1f),
                    text = product.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                IconButton(
                    onClick = { onFavourite(product) }
                ) {
                    Icon(
                        imageVector = if (favourite) Phosphor.HeartStraightFill else Phosphor.HeartStraight,
                        contentDescription = stringResource(id = if (favourite) R.string.favorite_remove else R.string.favorite_add),
                        tint = if (favourite) Color.Red else MaterialTheme.colorScheme.outline
                    )
                }

                action?.let {
                    IconButton(
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        ),
                        onClick = {
                            when (action) {
                                is ActionState.Install -> MainApplication.wm.install(product)
                                is ActionState.Launch  -> installed?.let {
                                    context.onLaunchClick(
                                        it,
                                        neoActivity.supportFragmentManager
                                    )
                                }

                                else                   -> {}
                            }
                        }) {
                        Icon(
                            imageVector = action.icon,
                            contentDescription = stringResource(id = action.textId),
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun ExpandedItemContent(
    modifier: Modifier = Modifier,
    item: ProductItem,
    installed: Installed? = null,
    favourite: Boolean = false,
    onFavourite: (ProductItem) -> Unit = {},
    onActionClicked: (ProductItem) -> Unit = {},
) {
    Box(contentAlignment = Alignment.CenterEnd) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onFavourite(item) }) {
                Icon(
                    imageVector = if (favourite) Phosphor.HeartStraightFill else Phosphor.HeartStraight,
                    contentDescription = stringResource(id = if (favourite) R.string.favorite_remove else R.string.favorite_add),
                    tint = if (favourite) Color.Red else MaterialTheme.colorScheme.outline
                )
            }
            AnimatedVisibility(visible = installed == null || installed.launcherActivities.isNotEmpty()) {
                val action = when {
                    installed != null -> ActionState.Launch
                    else              -> ActionState.Install
                }
                ActionButton(
                    text = stringResource(id = action.textId),
                    icon = action.icon,
                    positive = true,
                    onClick = { onActionClicked(item) }
                )
            }
        }
    }
}

//@Preview
@Composable
fun ProductsListItemPreview() {
    ProductsListItem(ProductItem())
}