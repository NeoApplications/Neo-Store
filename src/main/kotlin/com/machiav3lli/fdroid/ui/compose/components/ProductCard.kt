package com.machiav3lli.fdroid.ui.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.network.CoilDownloader
import com.machiav3lli.fdroid.ui.compose.utils.NetworkImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {}
) {

    val product by remember(item) { mutableStateOf(item) }
    val imageData by remember(product, repo) {
        mutableStateOf(
            CoilDownloader.createIconUri(
                product.packageName,
                product.icon,
                product.metadataIcon,
                repo?.address,
                repo?.authentication
            ).toString()
        )
    }
    Surface(
        modifier = Modifier
            .padding(4.dp)
            .requiredSize(80.dp, 116.dp),
        tonalElevation = 8.dp,
        shape = MaterialTheme.shapes.medium,
        onClick = { onUserClick(product) }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            NetworkImage(
                modifier = Modifier.size(64.dp),
                data = imageData
            )

            Text(
                modifier = Modifier.padding(4.dp, 2.dp),
                text = product.name,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                modifier = Modifier.padding(4.dp, 1.dp),
                text = product.version,
                style = MaterialTheme.typography.labelSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

}

//@Preview
@Composable
fun ProductCardPreview() {
    ProductCard(ProductItem())
}