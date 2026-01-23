package com.machiav3lli.fdroid.ui.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.UpdateListItem
import com.machiav3lli.fdroid.ui.components.CombinedUpdateCard
import com.machiav3lli.fdroid.ui.components.DownloadsCard
import com.machiav3lli.fdroid.ui.components.PRODUCT_CARD_HEIGHT
import com.machiav3lli.fdroid.ui.components.PRODUCT_CAROUSEL_HEIGHT
import com.machiav3lli.fdroid.ui.components.ProductCard
import com.machiav3lli.fdroid.ui.components.ProductCarouselItem
import kotlinx.coroutines.launch

@Composable
fun ProductsHorizontalRecycler(
    modifier: Modifier = Modifier,
    productsList: List<ProductItem>,
    repositories: Map<Long, Repository>,
    rowsNumber: Int = 2,
    onUserClick: (ProductItem) -> Unit = {},
) {
    LazyHorizontalStaggeredGrid(
        modifier = modifier
            .fillMaxWidth()
            .height(PRODUCT_CARD_HEIGHT * rowsNumber + 16.dp),
        rows = StaggeredGridCells.Fixed(rowsNumber),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalItemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        items(productsList, key = { it.packageName }) { item ->
            ProductCard(item, repositories[item.repositoryId], onUserClick)
        }
    }
}

@Composable
fun UpdatesHorizontalRecycler(
    modifier: Modifier = Modifier,
    productsList: List<UpdateListItem>,
    repositories: Map<Long, Repository>,
    rowsNumber: Int = 2,
    onUserClick: (UpdateListItem) -> Unit = {},
) {
    LazyHorizontalStaggeredGrid(
        modifier = modifier
            .fillMaxWidth()
            .height(PRODUCT_CARD_HEIGHT * rowsNumber + 16.dp),
        rows = StaggeredGridCells.Fixed(rowsNumber),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalItemSpacing = 8.dp,
        contentPadding = PaddingValues(horizontal = 8.dp),
    ) {
        items(
            items = productsList,
            key = { it.key }
        ) { item ->
            when (item) {
                is UpdateListItem.UpdateItem -> {
                    CombinedUpdateCard(
                        product = item.product,
                        download = item.download,
                        repo = repositories[item.product.repositoryId],
                        onUserClick = {
                            onUserClick(item)
                        }
                    )
                }

                is UpdateListItem.DownloadOnlyItem -> {
                    DownloadsCard(
                        download = item.download,
                        // TODO add
                        iconDetails = null,
                        repo = repositories[item.download.repositoryId],
                        state = item.download.state,
                        onUserClick = {
                            onUserClick(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProductsCarousel(
    modifier: Modifier = Modifier,
    productsList: List<ProductItem>,
    repositories: Map<Long, Repository>,
    favorites: List<String>,
    onFavouriteClick: (ProductItem) -> Unit,
    onActionClick: (ProductItem, ActionState) -> Unit = { _, _ -> },
    onUserClick: (ProductItem) -> Unit = {},
) {
    val state = rememberPagerState { productsList.size }
    val size by remember(productsList) {
        derivedStateOf { productsList.size }
    }

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
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 8.dp,
            beyondViewportPageCount = 3,
        ) {
            productsList.getOrNull(it)?.let { item ->
                ProductCarouselItem(
                    item,
                    repositories[item.repositoryId],
                    favorites.contains(item.packageName),
                    onFavouriteClick,
                    onActionClick,
                    onUserClick,
                )
            }
        }
        CarouselIndicators(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            size = size,
            state = state,
        )
    }
}

@Composable
fun CarouselIndicators(
    modifier: Modifier,
    size: Int = 1,
    dimension: Dp = 8.dp,
    state: PagerState,
) {
    val scope = rememberCoroutineScope()
    val currentPage by remember { derivedStateOf { state.currentPage } }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(dimension / 4, Alignment.CenterHorizontally),
    ) {
        items(size) { i ->
            val isSelected by remember {
                derivedStateOf {
                    currentPage == i
                }
            }
            val color by animateColorAsState(
                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer,
                label = "indicatorColor"
            )
            val width by animateDpAsState(
                targetValue = if (isSelected) dimension.times(2) else dimension,
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
