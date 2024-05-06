package com.machiav3lli.fdroid.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.service.worker.SyncRequest
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.Tooltip
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.components.common.BottomSheet
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.PagerNavBar
import com.machiav3lli.fdroid.ui.navigation.SlidePager
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavHostController, pageIndex: Int) {
    val context = LocalContext.current
    val mActivity = context as NeoActivity
    val mScope = rememberCoroutineScope()
    val showSearchSheet by mActivity.showSearchSheet.collectAsState()
    val searchSheetState = rememberModalBottomSheetState(true)

    val showPopup = remember { mutableStateOf(false) }

    val pages = listOf(
        NavItem.Latest,
        NavItem.Explore,
        NavItem.Installed,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })

    BackHandler {
        when {
            showSearchSheet -> {
                mScope.launch {
                    mActivity.setSearchQuery("")
                    searchSheetState.hide()
                }
                mActivity.showSearchSheet(false)
            }

            else            ->
                mActivity.moveTaskToBack(true)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        bottomBar = { PagerNavBar(pageItems = pages, pagerState = pagerState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                shape = MaterialTheme.shapes.extraLarge,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = {
                    mActivity.showSearchSheet(true)
                }
            ) {
                Icon(
                    imageVector = Phosphor.MagnifyingGlass,
                    contentDescription = stringResource(id = R.string.search)
                )
            }
        },
        topBar = {
            TopBar(title = stringResource(id = R.string.application_name)) {
                TopBarAction(
                    icon = Phosphor.ArrowsClockwise,
                    description = stringResource(id = R.string.sync_repositories)
                ) {
                    if (System.currentTimeMillis() - Preferences[Preferences.Key.LastManualSyncTime] >= 10_000L) {
                        Preferences[Preferences.Key.LastManualSyncTime] = System.currentTimeMillis()
                        SyncWorker.enqueueAll(SyncRequest.MANUAL)
                    } else {
                        showPopup.value = true
                    }
                }
                TopBarAction(
                    icon = Phosphor.GearSix,
                    description = stringResource(id = R.string.settings)
                ) {
                    navController.navigate(NavItem.Prefs.destination)
                }

                if (showPopup.value) {
                    Tooltip(stringResource(id = R.string.wait_to_sync), showPopup)
                }
            }
        }
    ) { paddingValues ->
        SlidePager(
            modifier = Modifier
                .padding(paddingValues)
                .blockBorder()
                .fillMaxSize(),
            pagerState = pagerState,
            pageItems = pages,
            navController = navController
        )

        if (showSearchSheet) {
            BottomSheet(
                sheetState = searchSheetState,
                onDismiss = {
                    mScope.launch { searchSheetState.hide() }
                    mActivity.showSearchSheet(false)
                },
            ) {
                MainApplication.mainActivity?.let {
                    SearchSheet(it.searchViewModel)
                }
            }
        }
    }
}