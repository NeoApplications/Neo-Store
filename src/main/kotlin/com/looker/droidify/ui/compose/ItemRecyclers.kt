package com.looker.droidify.ui.compose

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.ui.compose.components.ProductCard
import com.looker.droidify.ui.compose.components.ProductsListItem

@Composable
fun ProductsVerticalRecycler(
    productsList: List<Product>?,
    repositories: Map<Long, Repository>,
    onUserClick: (ProductItem) -> Unit = {}
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        LazyColumn(
            verticalArrangement = spacedBy(2.dp)
        ) {
            items(productsList ?: emptyList()) { product ->
                product.item.let { item ->
                    ProductsListItem(item, repositories[item.repositoryId], onUserClick)
                }
            }
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
        horizontalArrangement = spacedBy(2.dp)
    ) {
        items(productsList ?: emptyList()) { product ->
            product.item.let { item ->
                ProductCard(item, repositories[item.repositoryId], onUserClick)
            }
        }
    }
}