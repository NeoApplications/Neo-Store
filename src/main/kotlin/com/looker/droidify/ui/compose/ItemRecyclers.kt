package com.looker.droidify.ui.compose

import android.util.Log
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.looker.droidify.database.Product

@Composable
fun ProductsVerticalRecycler(productsList: List<Product>) {
    LazyColumn(
        verticalArrangement = spacedBy(2.dp)
    ) {
        items(productsList) { product: Product ->
            product.data_item?.let { item ->
                ProductRow(item.name, item.version, item.summary, onUserClick = {
                    Log.d(this.toString(), "You clicked $it")
                })
            }
        }
    }
}

@Composable
fun ProductsHorizontalRecycler(productsList: List<Product>) {
    LazyRow(
        horizontalArrangement = spacedBy(2.dp)
    ) {
        items(productsList) { product: Product ->
            product.data_item?.let { item ->
                ProductColumn(item.name, item.version, onUserClick = {
                    Log.d(this.toString(), "You clicked $it")
                })
            }
        }
    }
}