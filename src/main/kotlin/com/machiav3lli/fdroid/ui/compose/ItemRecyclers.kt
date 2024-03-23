package com.machiav3lli.fdroid.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.ui.components.PRODUCT_CARD_HEIGHT
import com.machiav3lli.fdroid.ui.components.PRODUCT_CAROUSEL_HEIGHT
import com.machiav3lli.fdroid.ui.components.ProductCard
import com.machiav3lli.fdroid.ui.components.ProductCarouselItem
import com.machiav3lli.fdroid.ui.components.RepositoryItem
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroupHeading
import kotlinx.coroutines.launch

@Composable
fun ProductsHorizontalRecycler(
    modifier: Modifier = Modifier,
    productsList: List<Product>?,
    repositories: Map<Long, Repository>,
    installedMap: Map<String, Installed> = emptyMap(),
    rowsNumber: Int = 2,
    onUserClick: (ProductItem) -> Unit = {},
) {
    LazyHorizontalStaggeredGrid(
        modifier = modifier
            .fillMaxWidth()
            .height(PRODUCT_CARD_HEIGHT * rowsNumber + 8.dp),
        rows = StaggeredGridCells.Fixed(rowsNumber),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalItemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        items(productsList ?: emptyList()) { product ->
            product.toItem(installedMap[product.packageName]).let { item ->
                ProductCard(item, repositories[item.repositoryId], onUserClick)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductsCarousel(
    modifier: Modifier = Modifier,
    productsList: List<Product>?,
    repositories: Map<Long, Repository>,
    installedMap: Map<String, Installed> = emptyMap(),
    favorites: Array<String>,
    onFavouriteClick: (ProductItem) -> Unit,
    onActionClick: (ProductItem) -> Unit = {},
    onUserClick: (ProductItem) -> Unit = {},
) {
    val state = rememberPagerState { productsList?.size ?: 0 }

    Box(
        modifier = modifier
            .fillMaxSize()
            .height(PRODUCT_CAROUSEL_HEIGHT)
    ) {
        HorizontalPager(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 16.dp),
            state = state,
            contentPadding = PaddingValues(horizontal = 8.dp),
            pageSpacing = 8.dp,
            beyondBoundsPageCount = 3,
        ) {
            productsList?.get(it)?.let { product ->
                product.toItem(installedMap[product.packageName]).let { item ->
                    ProductCarouselItem(
                        item,
                        repositories[item.repositoryId],
                        installedMap[product.packageName],
                        favorites.contains(item.packageName),
                        onFavouriteClick,
                        onActionClick,
                        onUserClick,
                    )
                }
            }
        }
        CarouselIndicators(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            size = productsList?.size ?: 1,
            state = state,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselIndicators(
    modifier: Modifier,
    size: Int = 1,
    dimension: Dp = 8.dp,
    state: PagerState,
) {
    val scope = rememberCoroutineScope()

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimension / 4, Alignment.CenterHorizontally),
    ) {
        items(size) { i ->
            val color by animateColorAsState(
                targetValue = if (state.currentPage == i) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer,
                label = "indicatorColor"
            )
            val width by animateDpAsState(
                targetValue = if (state.currentPage == i) dimension.times(2) else dimension,
                label = "indicatorWidth"
            )
            Box(
                modifier = Modifier
                    .size(height = dimension, width = width)
                    .clip(CircleShape)
                    .background(color = color)
                    .clickable { scope.launch { state.animateScrollToPage(i) } }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RepositoriesRecycler(
    modifier: Modifier = Modifier,
    repositoriesList: List<Repository>,
    onClick: (Repository) -> Unit = {},
    onLongClick: (Repository) -> Unit = {},
) {
    val partedRrepos by remember(repositoriesList) {
        mutableStateOf(repositoriesList.partition { it.enabled })
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            PreferenceGroupHeading(heading = stringResource(id = R.string.enabled))
        }
        items(items = partedRrepos.first, key = { it.id }) {
            RepositoryItem(
                modifier = Modifier.animateItemPlacement(),
                repository = it,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
        item {
            PreferenceGroupHeading(heading = stringResource(id = R.string.disabled))
        }
        items(items = partedRrepos.second, key = { it.id }) {
            RepositoryItem(
                modifier = Modifier.animateItemPlacement(),
                repository = it,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
    }
}
