package com.machiav3lli.fdroid.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.NeoNavigationSuiteScaffold
import com.machiav3lli.fdroid.ui.navigation.SlidePager
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@Composable
fun PrefsPage(navController: NavHostController, pageIndex: Int) {
    val context = LocalContext.current
    val mActivity = context as NeoActivity
    val scope = rememberCoroutineScope()

    val pages = persistentListOf(
        NavItem.PersonalPrefs,
        NavItem.UpdatesPrefs,
        NavItem.ReposPrefs,
        NavItem.OtherPrefs,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPage by remember { derivedStateOf { pages[pagerState.currentPage] } }

    BackHandler {
        navController.navigateUp()
    }

    NeoNavigationSuiteScaffold(
        pages = pages,
        selectedPage = currentPage,
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
                TopBar(title = stringResource(id = currentPage.title))
            }
        ) { paddingValues ->
            SlidePager(
                modifier = Modifier
                    .padding(paddingValues)
                    .blockBorder()
                    .fillMaxSize(),
                pagerState = pagerState,
                pageItems = pages,
            )
        }
    }
}