package com.looker.droidify.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.looker.droidify.R
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.ui.compose.theme.AppTheme

@Composable
fun ProductRow(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = { onUserClick(item) })
            .padding(8.dp)
    ) {
        // TODO: Fix the issue where coil doesn't fallback to the palceholder
        val imagePainter =
            if (repo != null) rememberImagePainter(
                CoilDownloader.createIconUri(
                    item.packageName,
                    item.icon,
                    item.metadataIcon,
                    repo.address,
                    repo.authentication
                ), builder = {
                    placeholder(R.drawable.ic_application_default)
                    error(R.drawable.ic_application_default)
                }
            ) else painterResource(id = R.drawable.ic_application_default)
        Image(
            painter = imagePainter,
            modifier = Modifier
                .requiredSize(56.dp)
                .clip(shape = RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
                .requiredHeight(56.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = item.name,
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    fontWeight = FontWeight.Bold,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = item.version,
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.body2
                )
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.fillMaxHeight(),
                    text = item.summary,
                    style = MaterialTheme.typography.body2
                )
            }

        }
    }
}

@Composable
fun ProductColumn(
    item: ProductItem,
    repo: Repository? = null,
    onUserClick: (ProductItem) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .requiredSize(72.dp, 96.dp)
            .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
            .clip(shape = RoundedCornerShape(8.dp))
            .clickable(onClick = { onUserClick(item) })
            .padding(4.dp)
    ) {
        // TODO: Fix the issue where coil doesn't fallback to the palceholder
        val imagePainter = if (repo != null)
            rememberImagePainter(
                CoilDownloader.createIconUri(
                    item.packageName,
                    item.icon,
                    item.metadataIcon,
                    repo.address,
                    repo.authentication
                ), builder = {
                    placeholder(R.drawable.ic_application_default)
                    error(R.drawable.ic_application_default)
                }
            )
        else painterResource(id = R.drawable.ic_application_default)
        Image(
            painter = imagePainter,
            modifier = Modifier
                .requiredSize(56.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Text(
            text = item.name,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = item.version,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.overline,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
}

@Preview
@Composable
fun ProductColumnPreview() {
    AppTheme(darkTheme = false) {
        ProductColumn(ProductItem())
    }
}

@Preview
@Composable
fun ProductColumnDarkPreview() {
    AppTheme(darkTheme = true) {
        ProductColumn(ProductItem())
    }
}

@Preview
@Composable
fun ProductRowPreview() {
    AppTheme(darkTheme = false) {
        ProductRow(ProductItem())
    }
}

@Preview
@Composable
fun ProductRowDarkPreview() {
    AppTheme(darkTheme = true) {
        ProductRow(ProductItem())
    }
}