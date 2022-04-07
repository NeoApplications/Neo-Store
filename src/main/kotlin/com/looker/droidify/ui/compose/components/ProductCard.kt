package com.looker.droidify.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.ui.compose.utils.NetworkImage

@Composable
fun ProductCard(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {}
) {

    val imageData by remember(item, repo) {
        mutableStateOf(
            CoilDownloader.createIconUri(
                item.packageName,
                item.icon,
                item.metadataIcon,
                repo?.address,
                repo?.authentication
            ).toString()
        )
    }

    Column(
        modifier = Modifier
            .padding(4.dp)
            .requiredSize(80.dp, 116.dp)
            .clip(shape = RoundedCornerShape(8.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .clickable(onClick = { onUserClick(item) }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NetworkImage(
            modifier = Modifier.size(64.dp),
            data = imageData
        )

        Text(
            modifier = Modifier.padding(4.dp, 2.dp),
            text = item.name,
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            modifier = Modifier.padding(4.dp, 1.dp),
            text = item.version,
            style = MaterialTheme.typography.labelSmall,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun ProductCardPreview() {
    ProductCard(ProductItem())
}