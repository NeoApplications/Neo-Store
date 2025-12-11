package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.House
import com.machiav3lli.fdroid.ui.compose.utils.blockBorderBottom
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.NeoNavigationSuiteScaffold
import com.machiav3lli.fdroid.ui.navigation.SlidePager
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PrefsPage(pageIndex: Int, navigateUp: () -> Unit) {
    val scope = rememberCoroutineScope()

    val pages = persistentListOf(
        NavItem.PersonalPrefs,
        NavItem.UpdatesPrefs,
        NavItem.ReposPrefs,
        NavItem.OtherPrefs,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPageIndex = remember { derivedStateOf { pagerState.currentPage } }
    val currentPage by remember { derivedStateOf { pages[currentPageIndex.value] } }

    NeoNavigationSuiteScaffold(
        pages = pages,
        selectedPage = currentPageIndex,
        onItemClick = { index ->
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TopBar(
                    title = stringResource(id = currentPage.title),
                    navigationAction = {
                        TopBarAction(
                            modifier = Modifier.padding(top = 8.dp),
                            icon = Phosphor.House,
                            description = stringResource(id = R.string.main_page)
                        ) {
                            navigateUp()
                        }
                    }
                )
            }
        ) { paddingValues ->
            SlidePager(
                modifier = Modifier
                    .padding(paddingValues)
                    .blockBorderBottom()
                    .fillMaxSize(),
                pagerState = pagerState,
                pageItems = pages,
                preComposePages = 0,
            )
        }
    }
}