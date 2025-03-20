package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.POPUP_LONG
import com.machiav3lli.fdroid.POPUP_NONE
import com.machiav3lli.fdroid.POPUP_SHORT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.LatestSyncs
import com.machiav3lli.fdroid.manager.work.BatchSyncWorker
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.ui.components.Tooltip
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.utils.blockBorderBottom
import com.machiav3lli.fdroid.ui.dialog.ActionsDialogUI
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.NavRoute
import com.machiav3lli.fdroid.ui.navigation.NeoNavigationSuiteScaffold
import com.machiav3lli.fdroid.ui.navigation.SlidePager
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.getLocaleDateString
import com.machiav3lli.fdroid.viewmodels.MainVM
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainPage(
    navController: NavHostController,
    pageIndex: Int,
    viewModel: MainVM = koinNeoViewModel()
) {
    val context = LocalContext.current
    val mActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()

    val successfulSyncs by NeoApp.db.getRepositoryDao().latestUpdatesFlow()
        .collectAsState(initial = LatestSyncs(0L, 0L))

    val showPopup = remember { mutableIntStateOf(POPUP_NONE) }
    val openSyncDialog = remember { mutableStateOf(false) }

    val pages = persistentListOf(
        NavItem.Latest,
        NavItem.Explore,
        NavItem.Search,
        NavItem.Installed,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPageIndex = remember { derivedStateOf { pagerState.currentPage } }
    val appPackage: MutableState<String?> = remember { mutableStateOf(null) }
    val navigatorState by viewModel.navigationState.collectAsStateWithLifecycle()

    BackHandler {
        mActivity.moveTaskToBack(true)
    }

    LaunchedEffect(true) {
        if (!Preferences[Preferences.Key.InitialSync])
            openSyncDialog.value = true
    }

    LaunchedEffect(navigatorState) {
        scope.launch {
            mActivity.mainNavigator.navigateTo(navigatorState.first, navigatorState.second)
        }
    }

    NeoNavigationSuiteScaffold(
        pages = pages,
        selectedPage = currentPageIndex,
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
                                        scope.launch { BatchSyncWorker.enqueue(SyncRequest.MANUAL) }
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
                            .blockBorderBottom()
                            .fillMaxSize(),
                        pagerState = pagerState,
                        pageItems = pages,
                    )
                }
            },
            detailPane = {
                appPackage.value = mActivity.mainNavigator.currentDestination
                    ?.takeIf { it.pane == this.paneRole }?.contentKey?.toString()
                    ?.nullIfEmpty()

                appPackage.value?.let {
                    AnimatedPane {
                        AppPage(
                            packageName = it,
                        )
                    }
                }
            }
        )
    }

    if (openSyncDialog.value) {
        BaseDialog(openDialogCustom = openSyncDialog) {
            ActionsDialogUI(
                titleText = stringResource(id = R.string.confirmation),
                messageText = stringResource(id = R.string.initial_sync_repositories),
                primaryText = stringResource(id = R.string.sync_repositories),
                primaryIcon = Phosphor.ArrowsClockwise,
                primaryAction = {
                    scope.launch { BatchSyncWorker.enqueue(SyncRequest.MANUAL) }
                },
                onDismiss = {
                    Preferences[Preferences.Key.InitialSync] = true
                    openSyncDialog.value = false
                },
                dismissTextId = R.string.skip,
            )
        }
    }
}