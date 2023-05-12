package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.ui.components.ProductsListItem

@Composable
fun <T> ListDialogUI(
    titleText: String,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
) {
    Card(
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
            // TODO add wait/empty indicators
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false)
            ) {
                items(items = items) { itemContent(it) }
            }
        }
    }
}

@Composable
fun ProductsListDialogUI(
    repositoryId: Long,
    title: String,
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val apps by MainApplication.db.productDao.productsForRepositoryFlow(repositoryId)
        .collectAsState(initial = emptyList())
    val repo by MainApplication.db.repositoryDao.getFlow(repositoryId)
        .collectAsState(initial = null)

    ListDialogUI(
        titleText = title,
        items = apps,
    ) {
        ProductsListItem(
            item = it.toItem(),
            repo = repo,
            // TODO add installed
        )
    }
}