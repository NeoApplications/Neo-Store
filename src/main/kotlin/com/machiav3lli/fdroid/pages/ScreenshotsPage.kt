package com.machiav3lli.fdroid.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.network.CoilDownloader
import com.machiav3lli.fdroid.ui.components.NetworkImage
import com.machiav3lli.fdroid.ui.components.ScreenshotItem
import com.machiav3lli.fdroid.ui.components.SecondaryActionButton
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleLeft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleRight
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ScreenshotsPage(
    screenshots: List<ScreenshotItem>,
    page: Int,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val currentPage by remember(pagerState.currentPage) {
        mutableStateOf(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {

        HorizontalPager(pageCount = screenshots.size, state = pagerState) { page ->
            val screenshot = screenshots[page]
            var image by remember { mutableStateOf<String?>(null) }

            SideEffect {
                image = CoilDownloader.createScreenshotUri(
                    screenshot.repository,
                    screenshot.packageName,
                    screenshot.screenShot
                ).toString()
            }

            NetworkImage(
                modifier = Modifier.fillMaxSize(),
                data = image,
                contentScale = ContentScale.Fit,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Spacer(modifier = Modifier.width(12.dp))
            AnimatedVisibility(
                visible = currentPage != 0
            ) {
                SecondaryActionButton(
                    icon = Phosphor.ArrowCircleLeft,
                    description = stringResource(id = R.string.previous)
                ) {
                    scope.launch { pagerState.animateScrollToPage(currentPage - 1) }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = currentPage < screenshots.size - 1) {
                SecondaryActionButton(
                    icon = Phosphor.ArrowCircleRight,
                    description = stringResource(id = R.string.next)
                ) {
                    scope.launch { pagerState.animateScrollToPage(currentPage + 1) }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
        }
    }

    LaunchedEffect(page) {
        pagerState.scrollToPage(page)
    }
}