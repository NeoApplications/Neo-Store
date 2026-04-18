package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.compose.CarouselIndicators
import kotlinx.coroutines.launch

@Composable
fun OnboardingPage(onComplete: () -> Unit) {
    val scope = rememberCoroutineScope()
    val state = rememberPagerState { 3 }

    fun animateToPage(page: Int) {
        Preferences[Preferences.Key.OnboardedPage] = page
        scope.launch { state.animateScrollToPage(page) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopBar(
                title = stringResource(
                    id = R.string.setup_FORMAT,
                    when (state.currentPage) {
                        2 -> stringResource(id = R.string.repositories)
                        1 -> stringResource(id = R.string.settings)
                        else -> stringResource(id = R.string.permissions)
                    }
                )
            ) {
                CarouselIndicators(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    size = state.pageCount,
                    state = state,
                    enableScrolling = false,
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                modifier = Modifier.weight(1f),
                state = state,
                userScrollEnabled = false,
            ) { page ->
                when (page) {
                    0 -> OnboardingPermsPage {
                        if (Preferences[Preferences.Key.OnboardedPage] > 2)
                            onComplete()
                        else animateToPage(1)
                    }

                    1 -> OnboardingPrefsPage { animateToPage(2) }
                    2 -> OnboardingReposPage {
                        Preferences[Preferences.Key.OnboardedPage] = 3
                        onComplete()
                    }
                }
            }
        }
    }
}
