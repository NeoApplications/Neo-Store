package com.looker.droidify.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.looker.droidify.entity.Screenshot
import com.looker.droidify.ui.compose.theme.LocalShapes
import com.looker.droidify.ui.compose.utils.NetworkImage

@Composable
fun ScreenshotList(
    modifier: Modifier = Modifier,
    screenShots: List<Screenshot>,
    onScreenShotClick: (Screenshot) -> Unit
) {
    val screenShotList by remember { mutableStateOf(screenShots) }
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(screenShotList) {
            NetworkImage(
                modifier = Modifier
                    .wrapContentWidth()
                    .requiredHeight(120.dp)
                    .clickable { onScreenShotClick(it) },
                data = it.path,
                shape = RoundedCornerShape(LocalShapes.current.large)
            )
        }
    }
}