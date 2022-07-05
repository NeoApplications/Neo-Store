package com.machiav3lli.fdroid.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.Screenshot
import com.machiav3lli.fdroid.network.CoilDownloader
import com.machiav3lli.fdroid.ui.compose.utils.NetworkImage

data class ScreenshotItem(
    val screenShot: Screenshot,
    val repository: Repository,
    val packageName: String
)

fun Screenshot.toScreenshotItem(packageName: String, repository: Repository) =
    ScreenshotItem(this, repository, packageName)

@Composable
fun ScreenshotList(
    modifier: Modifier = Modifier,
    screenShots: List<ScreenshotItem>,
    onScreenShotClick: (Screenshot) -> Unit = {}
) {
    val screenShotList by remember { mutableStateOf(screenShots) }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(screenShotList) {

            var image by remember { mutableStateOf<String?>(null) }

            SideEffect {
                image = CoilDownloader.createScreenshotUri(
                    it.repository,
                    it.packageName,
                    it.screenShot
                ).toString()
            }

            NetworkImage(
                modifier = Modifier
                    .wrapContentWidth()
                    .requiredHeight(300.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { onScreenShotClick(it.screenShot) },
                data = image
            )
        }
    }
}