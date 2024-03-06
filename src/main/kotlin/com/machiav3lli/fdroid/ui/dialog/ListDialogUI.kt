package com.machiav3lli.fdroid.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.components.ProductItemContent
import com.machiav3lli.fdroid.ui.compose.utils.blockShadow

@Composable
fun <T> ListDialogUI(
    titleText: String,
    items: List<T>?,
    itemContent: @Composable (T) -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .blockShadow(),
                contentAlignment = if (items.isNullOrEmpty()) Alignment.Center
                else Alignment.TopStart
            ) {
                when {
                    items == null      -> Text(
                        text = stringResource(id = R.string.loading_list),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    items.isNotEmpty() -> {
                        LazyColumn(
                            modifier = Modifier.wrapContentHeight(),
                        ) {
                            items(items = items) { itemContent(it) }
                        }
                    }

                    else               -> Text(
                        text = stringResource(id = R.string.no_applications_available),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

        }
    }
}

@Composable
fun ProductsListDialogUI(
    repositoryId: Long,
    title: String,
) {
    val apps by MainApplication.db.productDao.productsForRepositoryFlow(repositoryId)
        .collectAsState(initial = null)
    val repo by MainApplication.db.repositoryDao.getFlow(repositoryId)
        .collectAsState(initial = null)

    ListDialogUI(
        titleText = title,
        items = apps,
    ) {
        ProductItemContent(
            product = it.toItem(),
            repo = repo,
        )
    }
}