package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.machiav3lli.fdroid.network.CoilDownloader
import com.machiav3lli.fdroid.ui.compose.components.ScreenshotItem
import com.machiav3lli.fdroid.ui.compose.utils.ZoomableImage

@OptIn(ExperimentalPagerApi::class)
@Composable
fun ScreenshotsPage(
    screenshots: List<ScreenshotItem>,
    page: Int,
) {
    val pagerState = rememberPagerState()

    HorizontalPager(count = screenshots.size, state = pagerState) { page ->
        val screenshot = screenshots[page]
        var image by remember { mutableStateOf<String?>(null) }

        SideEffect {
            image = CoilDownloader.createScreenshotUri(
                screenshot.repository,
                screenshot.packageName,
                screenshot.screenShot
            ).toString()
        }

        Box {
            ZoomableImage(
                data = image,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    LaunchedEffect(page) {
        pagerState.scrollToPage(page)
    }
}