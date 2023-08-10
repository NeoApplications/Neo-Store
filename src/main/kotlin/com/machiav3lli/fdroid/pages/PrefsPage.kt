package com.machiav3lli.fdroid.pages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.PagerNavBar
import com.machiav3lli.fdroid.ui.navigation.SlidePager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PrefsPage(navController: NavHostController, pageIndex: Int) {
    val pages = listOf(
        NavItem.PersonalPrefs,
        NavItem.UpdatesPrefs,
        NavItem.ReposPrefs,
        NavItem.OtherPrefs,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPage by remember(pagerState.currentPage) { mutableStateOf(pages[pagerState.currentPage]) }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = { PagerNavBar(pageItems = pages, pagerState = pagerState) },
        topBar = {
            TopBar(title = stringResource(id = currentPage.title))
        }
    ) { paddingValues ->
        SlidePager(
            modifier = Modifier.padding(paddingValues),
            pagerState = pagerState,
            pageItems = pages,
            navController = navController
        )
    }
}