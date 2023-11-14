package com.machiav3lli.fdroid.pages

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.service.worker.SyncRequest
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MagnifyingGlass
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
    val expanded = remember {
        mutableStateOf(false)
    }
    var showSearchSheet by remember { mutableStateOf(false) }
    val searchSheetState = rememberModalBottomSheetState(true)

    val pages = listOf(
        NavItem.Latest,
        NavItem.Explore,
        NavItem.Installed,
        NavItem.Search,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })

    BackHandler {
        when {
            expanded.value -> {
                mScope.launch { mActivity.setSearchQuery("") }
                expanded.value = false
            }

            else           ->
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
                contentColor = MaterialTheme.colorScheme.primary,
                onClick = {
                    showSearchSheet = true
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
                AnimatedVisibility(!expanded.value) {
                    TopBarAction(
                        icon = Phosphor.ArrowsClockwise,
                        description = stringResource(id = R.string.sync_repositories)
                    ) {
                        SyncWorker.enqueueAll(SyncRequest.MANUAL)
                    }
                }
                AnimatedVisibility(!expanded.value) {
                    TopBarAction(
                        icon = Phosphor.GearSix,
                        description = stringResource(id = R.string.settings)
                    ) {
                        navController.navigate(NavItem.Prefs.destination)
                    }
                }
            }
        }
    ) { paddingValues ->
        SlidePager(
            modifier = Modifier.padding(paddingValues),
            pagerState = pagerState,
            pageItems = pages,
            navController = navController
        )

        if (showSearchSheet) {
            ModalBottomSheet(
                sheetState = searchSheetState,
                containerColor = MaterialTheme.colorScheme.background,
                scrimColor = Color.Transparent,
                dragHandle = null,
                onDismissRequest = {
                    mScope.launch { searchSheetState.hide() }
                    showSearchSheet = false
                },
            ) {
                MainApplication.mainActivity?.let {
                    SearchSheet(it.searchViewModel)
                }
            }
        }
    }
}