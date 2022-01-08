package com.looker.droidify.ui.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.looker.droidify.R

@Preview
@Composable
fun ProductRow(
    name: String = "Droid-ify",
    version: String = "69",
    summary: String = "A great F-Droid client",
    onUserClick: (String) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clickable(onClick = { onUserClick(name) })
            .padding(4.dp)
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        val imagePainter =
            painterResource(id = R.drawable.ic_launcher_foreground)
//            rememberImagePainter(CoilDownloader.createIconUri(item.packageName, item.icon, item.metadataIcon, repo.address, repo.authentication))
        Image(
            painter = imagePainter,
            modifier = Modifier
                .requiredSize(56.dp)
                .clip(shape = CircleShape),
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
                    text = name,
                    modifier = Modifier
                        .align(Alignment.CenterStart),
                    fontWeight = FontWeight.Bold,
                    softWrap = true,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = version,
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    style = MaterialTheme.typography.body2
                )
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    modifier = Modifier.fillMaxHeight(),
                    text = summary,
                    style = MaterialTheme.typography.body2
                )
            }

        }
    }
}

@Preview
@Composable
fun ProductColumn(
    name: String = "Droid-ifsdfsdfy",
    version: String = "69",
    onUserClick: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .clickable(onClick = { onUserClick(name) })
            .padding(4.dp)
            .requiredSize(72.dp, 96.dp)
            .background(color = MaterialTheme.colors.surface, shape = RoundedCornerShape(8.dp))
            .padding(4.dp)
    ) {
        val imagePainter =
            painterResource(id = R.drawable.ic_launcher_foreground)
//            rememberImagePainter(CoilDownloader.createIconUri(item.packageName, item.icon, item.metadataIcon, repo.address, repo.authentication))
        Image(
            painter = imagePainter,
            modifier = Modifier
                .requiredSize(56.dp)
                .clip(shape = CircleShape)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )

        Text(
            text = name,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.body2,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = version,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.overline,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }

    }
}