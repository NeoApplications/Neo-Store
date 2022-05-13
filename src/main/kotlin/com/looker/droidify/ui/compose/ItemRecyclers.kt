package com.looker.droidify.ui.compose

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.looker.droidify.R
import com.looker.droidify.database.entity.Installed
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.ui.compose.components.ProductCard
import com.looker.droidify.ui.compose.components.ProductsListItem
import com.looker.droidify.ui.compose.components.RepositoryItem

@Composable
fun ProductsVerticalRecycler(
    productsList: List<Product>?,
    repositories: Map<Long, Repository>,
    modifier: Modifier = Modifier.fillMaxSize(),
    onUserClick: (ProductItem) -> Unit = {},
    onFavouriteClick: (ProductItem) -> Unit = {},
    onInstallClick: (ProductItem) -> Unit = {}
    getInstalled: (ProductItem) -> Installed? = {null},
) {
    VerticalItemList(list = productsList, modifier = modifier) {
        it.toItem().let { item ->
            ProductsListItem(
                item,
                repositories[item.repositoryId],
                onUserClick,
                onFavouriteClick,
                onInstallClick
                getInstalled.invoke(item),
            )
        }
    }
}

@Composable
fun ProductsHorizontalRecycler(
    productsList: List<Product>?,
    repositories: Map<Long, Repository>,
    onUserClick: (ProductItem) -> Unit = {}
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = spacedBy(2.dp)
    ) {
        items(productsList ?: emptyList()) { product ->
            product.toItem().let { item ->
                ProductCard(item, repositories[item.repositoryId], onUserClick)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepositoriesRecycler(
    repositoriesList: List<Repository>?,
    onClick: (Repository) -> Unit = {},
    onLongClick: (Repository) -> Unit = {}
) {
    VerticalItemList(list = repositoriesList, itemKey = { it.id }) {
        RepositoryItem(
            modifier = Modifier.animateItemPlacement(),
            repository = it,
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}

@Composable
fun <T> VerticalItemList(
    modifier: Modifier = Modifier.fillMaxSize(),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    list: List<T>?,
    itemKey: ((T) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor),
        contentAlignment = if (list.isNullOrEmpty()) Alignment.Center else Alignment.TopStart
    ) {
        if (!list.isNullOrEmpty()) {
            LazyColumn(verticalArrangement = spacedBy(4.dp)) {
                items(items = list, key = itemKey, itemContent = itemContent)
            }
        } else Text(
            text = stringResource(id = R.string.no_applications_available),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}