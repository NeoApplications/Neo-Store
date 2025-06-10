package com.machiav3lli.fdroid.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.manager.network.createScreenshotUri
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PlayCircle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri

data class ScreenshotItem(
    val screenShot: String,
    val repository: Repository,
)

@Composable
fun ScreenshotList(
    screenShots: List<ScreenshotItem>,
    video: String = "",
    modifier: Modifier = Modifier,
    onUriClick: (Uri) -> Unit,
    onScreenShotClick: (Int) -> Unit,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (video.isNotEmpty()) item(key = "video") {
            Surface(
                modifier = Modifier
                    .height(250.dp)
                    .aspectRatio(9 / 20f),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                contentColor = MaterialTheme.colorScheme.onSurface,
                onClick = { onUriClick(video.toUri()) },
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Phosphor.PlayCircle,
                        contentDescription = "Play video",
                        modifier = Modifier.size(72.dp),
                    )
                }
            }
        }
        itemsIndexed(screenShots, key = { _, it -> it.screenShot.toString() }) { index, it ->
            val image by produceState<String?>(initialValue = null, it) {
                launch(Dispatchers.IO) {
                    value = createScreenshotUri(
                        it.repository,
                        it.screenShot,
                    ).toString()
                }
            }

            NetworkImage(
                modifier = Modifier
                    .wrapContentWidth()
                    .requiredHeight(250.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onScreenShotClick(index) },
                contentScale = ContentScale.Fit,
                data = image,
                isScreenshot = true,
            )
        }
    }
}
