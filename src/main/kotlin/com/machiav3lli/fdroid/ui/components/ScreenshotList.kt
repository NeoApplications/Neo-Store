package com.machiav3lli.fdroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.Screenshot
import com.machiav3lli.fdroid.manager.network.createScreenshotUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ScreenshotItem(
    val screenShot: Screenshot,
    val repository: Repository,
    val packageName: String,
)

fun Screenshot.toScreenshotItem(packageName: String, repository: Repository) =
    ScreenshotItem(this, repository, packageName)

@Composable
fun ScreenshotList(
    modifier: Modifier = Modifier,
    screenShots: List<ScreenshotItem>,
    onScreenShotClick: (Int) -> Unit,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(screenShots) { index, it ->
            val image by produceState<String?>(initialValue = null, it) {
                launch(Dispatchers.IO) {
                    value = createScreenshotUri(
                        it.repository,
                        it.packageName,
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
