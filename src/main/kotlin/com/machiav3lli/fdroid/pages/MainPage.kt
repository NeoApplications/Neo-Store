package com.machiav3lli.fdroid.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.machiav3lli.fdroid.POPUP_LONG
import com.machiav3lli.fdroid.POPUP_NONE
import com.machiav3lli.fdroid.POPUP_SHORT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.LatestSyncs
import com.machiav3lli.fdroid.service.worker.SyncRequest
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.Tooltip
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.NavRoute
import com.machiav3lli.fdroid.ui.navigation.NeoNavigationSuiteScaffold
import com.machiav3lli.fdroid.ui.navigation.SlidePager
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.getLocaleDateString
import com.machiav3lli.fdroid.viewmodels.AppSheetVM
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainPage(navController: NavHostController, pageIndex: Int) {
    val context = LocalContext.current
    val mActivity = context as NeoActivity
    val scope = rememberCoroutineScope()

    val successfulSyncs by MainApplication.db.getRepositoryDao().latestUpdatesFlow()
        .collectAsState(initial = LatestSyncs(0L, 0L))

    val showPopup = remember { mutableIntStateOf(POPUP_NONE) }

    val pages = persistentListOf(
        NavItem.Latest,
        NavItem.Explore,
        NavItem.Search,
        NavItem.Installed,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPage by remember { derivedStateOf { pages[pagerState.currentPage] } }
    val appPackage: MutableState<String?> = remember { mutableStateOf(null) }
    val appSheetVM by remember {
        derivedStateOf {
            AppSheetVM(
                MainApplication.db,
                appPackage.value ?: "",
            )
        }
    }

    BackHandler {
        mActivity.moveTaskToBack(true)
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
        NavigableListDetailPaneScaffold(
            navigator = mActivity.mainNavigator,
            listPane = {
                //AnimatedPane { } TODO re-add when fixing recomposition issue
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = {
                        TopBar(title = stringResource(id = R.string.application_name)) {
                            TopBarAction(
                                icon = Phosphor.ArrowsClockwise,
                                description = stringResource(id = R.string.sync_repositories),
                                onLongClick = {
                                    showPopup.intValue = POPUP_LONG
                                },
                                onClick = {
                                    if (System.currentTimeMillis() - Preferences[Preferences.Key.LastManualSyncTime] >= 10_000L) {
                                        Preferences[Preferences.Key.LastManualSyncTime] =
                                            System.currentTimeMillis()
                                        scope.launch { SyncWorker.enqueueAll(SyncRequest.MANUAL) }
                                    } else {
                                        showPopup.intValue = POPUP_SHORT
                                    }
                                }
                            )
                            TopBarAction(
                                icon = Phosphor.GearSix,
                                description = stringResource(id = R.string.settings)
                            ) {
                                navController.navigate(NavRoute.Prefs())
                            }

                            if (showPopup.intValue != POPUP_NONE) {
                                Tooltip(
                                    when (showPopup.intValue) {
                                        POPUP_LONG -> stringResource(
                                            id = R.string.last_successful_sync,
                                            context.getLocaleDateString(successfulSyncs.latest),
                                            context.getLocaleDateString(successfulSyncs.latestAll),
                                        )

                                        else       -> stringResource(id = R.string.wait_to_sync)
                                    },
                                    showPopup
                                )
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
                    )
                }
            },
            detailPane = {
                appPackage.value = mActivity.mainNavigator.currentDestination
                    ?.takeIf { it.pane == this.role }?.content?.toString()
                    ?.nullIfEmpty()

                appPackage.value?.let {
                    AnimatedPane {
                        AppPage(
                            viewModel = appSheetVM,
                            packageName = it,
                        )
                    }
                }
            }
        )
    }
}