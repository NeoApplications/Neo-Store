package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.POPUP_LONG
import com.machiav3lli.fdroid.POPUP_NONE
import com.machiav3lli.fdroid.POPUP_SHORT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.LatestSyncs
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.manager.work.BatchSyncWorker
import com.machiav3lli.fdroid.ui.components.ExpandedSearchView
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
    navigator: (NavRoute) -> Unit,
    pageIndex: Int,
    viewModel: MainVM = koinNeoViewModel()
) {
    val context = LocalContext.current
    val mActivity = LocalActivity.current as NeoActivity
    val focusManager = LocalFocusManager.current
    val panesNavigator = rememberListDetailPaneScaffoldNavigator<Any>()
    val scope = rememberCoroutineScope()

    val successfulSyncs by viewModel.successfulSyncs.collectAsState(initial = LatestSyncs(0L, 0L))

    val showPopup = remember { mutableIntStateOf(POPUP_NONE) }
    val openSyncDialog = remember { mutableStateOf(false) }

    val pages = persistentListOf(
        NavItem.Latest,
        NavItem.Explore,
        NavItem.Installed,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPageIndex = remember { derivedStateOf { pagerState.currentPage } }
    val navigatorState by viewModel.navigationState.collectAsStateWithLifecycle()
    val inSearchMode = rememberSaveable { mutableStateOf(false) }
    val query by viewModel.querySearch.collectAsState()

    BackHandler {
        mActivity.moveTaskToBack(true)
    }

    BackHandler(inSearchMode.value) {
        viewModel.setSearchQuery("")
        inSearchMode.value = false
        focusManager.clearFocus()
    }

    LaunchedEffect(true) {
        if (!Preferences[Preferences.Key.InitialSync])
            openSyncDialog.value = true
    }

    LaunchedEffect(navigatorState) {
        scope.launch {
            if (navigatorState.second != panesNavigator.currentDestination?.contentKey.toString())
                panesNavigator.navigateTo(navigatorState.first, navigatorState.second)
        }
    }

    NeoNavigationSuiteScaffold(
        pages = pages,
        selectedPage = currentPageIndex,
        backToPage = { viewModel.setNavigatorRole(ListDetailPaneScaffoldRole.List, "") },
        onItemClick = { index ->
            if (inSearchMode.value) inSearchMode.value = false
            scope.launch {
                pagerState.animateScrollToPage(index)
            }
        }
    ) {
        NavigableListDetailPaneScaffold(
            navigator = panesNavigator,
            listPane = {
                //AnimatedPane { } TODO re-add when recomposition issue fixed
                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    topBar = {
                        TopBar {
                            ExpandedSearchView(
                                query = query,
                                expanded = inSearchMode,
                                onQueryChanged = { newQuery ->
                                    if (newQuery != query) viewModel.setSearchQuery(newQuery)
                                },
                                onClose = {
                                    viewModel.setSearchQuery("")
                                },
                            )
                            TopBarAction(
                                modifier = Modifier.padding(top = 8.dp),
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
                                modifier = Modifier.padding(top = 8.dp),
                                icon = Phosphor.GearSix,
                                description = stringResource(id = R.string.settings)
                            ) {
                                navigator(NavRoute.Prefs())
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
                    Crossfade(inSearchMode) { searching ->
                        when {
                            searching.value -> {
                                Box(
                                    modifier = Modifier
                                        .padding(paddingValues)
                                        .blockBorderBottom()
                                        .fillMaxSize(),
                                ) {
                                    SearchPage()
                                }
                            }

                            else            -> SlidePager(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .blockBorderBottom()
                                    .fillMaxSize(),
                                pagerState = pagerState,
                                pageItems = pages,
                            )
                        }
                    }
                }
            },
            detailPane = {
                panesNavigator.currentDestination
                    ?.contentKey
                    ?.let {
                        it.toString().nullIfEmpty()?.let {
                            AnimatedPane {
                                AppPage(
                                    packageName = it.toString(),
                                    onDismiss = {
                                        scope.launch {
                                            panesNavigator.navigateBack(BackNavigationBehavior.PopUntilContentChange)
                                            viewModel.setNavigatorRole(
                                                panesNavigator.currentDestination!!.pane,
                                                panesNavigator.currentDestination!!.contentKey.toString()
                                            )
                                        }
                                    }
                                )
                            }
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