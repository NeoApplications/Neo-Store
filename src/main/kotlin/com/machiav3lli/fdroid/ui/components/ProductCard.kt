package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidthIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.network.CoilDownloader

val PRODUCT_CARD_ICON = 48.dp
val PRODUCT_CARD_HEIGHT = 64.dp
val PRODUCT_CARD_WIDTH = 220.dp

@Composable
fun ProductCard(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {},
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
            .requiredHeight(PRODUCT_CARD_HEIGHT)
            .requiredWidthIn(max = PRODUCT_CARD_WIDTH),
        tonalElevation = 4.dp,
        shape = MaterialTheme.shapes.medium,
        onClick = { onUserClick(product) }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
        ) {
            NetworkImage(
                modifier = Modifier.size(PRODUCT_CARD_ICON),
                data = imageData
            )

            Column(Modifier.height(IntrinsicSize.Min)) {
                Text(
                    modifier = Modifier.padding(4.dp, 1.dp),
                    text = product.name,
                    style = MaterialTheme.typography.bodySmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    modifier = Modifier.padding(4.dp, 0.dp),
                    text = product.version,
                    style = MaterialTheme.typography.labelSmall,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

}

//@Preview
@Composable
fun ProductCardPreview() {
    ProductCard(ProductItem())
}