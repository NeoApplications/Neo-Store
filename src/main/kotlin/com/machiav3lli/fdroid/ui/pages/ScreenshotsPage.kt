package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.manager.network.createScreenshotUri
import com.machiav3lli.fdroid.ui.components.NetworkImage
import com.machiav3lli.fdroid.ui.components.ScreenshotItem
import com.machiav3lli.fdroid.ui.compose.CarouselIndicators

@Composable
fun ScreenshotsPage(
    screenshots: List<ScreenshotItem>,
    page: Int,
) {
    val pagerState = rememberPagerState(pageCount = { screenshots.size })

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        HorizontalPager(state = pagerState, beyondViewportPageCount = 3) { page ->
            val screenshot = screenshots[page]
            val image by remember(screenshot) {
                mutableStateOf(
                    createScreenshotUri(
                        screenshot.repository,
                        screenshot.screenShot,
                    ).toString()
                )
            }

            NetworkImage(
                modifier = Modifier.fillMaxSize(),
                data = image,
                contentScale = ContentScale.Fit,
            )
        }
        CarouselIndicators(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            size = screenshots.size,
            dimension = 16.dp,
            state = pagerState,
        )
    }

    LaunchedEffect(page) {
        pagerState.animateScrollToPage(page)
    }
}