package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.POPUP_LONG
import com.machiav3lli.fdroid.POPUP_NONE
import com.machiav3lli.fdroid.POPUP_SHORT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.LatestSyncs
import com.machiav3lli.fdroid.data.entity.ColoringState
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.manager.work.BatchSyncWorker
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.ExpandingFadingCard
import com.machiav3lli.fdroid.ui.components.FilledRoundButton
import com.machiav3lli.fdroid.ui.components.RoundButton
import com.machiav3lli.fdroid.ui.components.Tooltip
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.compose.UpdatesHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.MagnifyingGlass
import com.machiav3lli.fdroid.ui.compose.utils.blockBorderBottom
import com.machiav3lli.fdroid.ui.dialog.ActionsDialogUI
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.NavRoute
import com.machiav3lli.fdroid.ui.navigation.NeoNavigationSuiteScaffold
import com.machiav3lli.fdroid.ui.navigation.SlidePager
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.extension.partitionTypes
import com.machiav3lli.fdroid.utils.getLocaleDateString
import com.machiav3lli.fdroid.viewmodels.MainVM
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun MainPage(
    navigator: (NavRoute) -> Unit,
    pageIndex: Int,
    viewModel: MainVM = koinNeoViewModel(),
) {
    val context = LocalContext.current
    val mActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()

    val successfulSyncs by viewModel.successfulSyncs.collectAsStateWithLifecycle(LatestSyncs())
    val dataState by viewModel.dataState.collectAsStateWithLifecycle()
    val updatesDownloads by viewModel.combinedUpdatesList.collectAsStateWithLifecycle()
    val (updates, activeDownloads) = updatesDownloads.partitionTypes()
    var updaterExpanded by rememberSaveable { mutableStateOf(false) }

    val openDialog = remember { mutableStateOf(false) }
    // TOD Move sync dialog here too
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }

    val showPopup = remember { mutableIntStateOf(POPUP_NONE) }
    val openSyncDialog = remember { mutableStateOf(false) }

    val pages = persistentListOf(
        NavItem.Latest,
        NavItem.Explore,
        NavItem.Installed,
    )
    val pagerState = rememberPagerState(initialPage = pageIndex, pageCount = { pages.size })
    val currentPageIndex = remember { derivedStateOf { pagerState.currentPage } }
    val currentPage by remember { derivedStateOf { pages[currentPageIndex.value] } }

    LaunchedEffect(true) {
        if (!Preferences[Preferences.Key.InitialSync])
            openSyncDialog.value = true
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
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                TopBar(
                    title = stringResource(id = currentPage.title),
                ) {
                    RoundButton(
                        modifier = Modifier.padding(top = 8.dp),
                        icon = Phosphor.MagnifyingGlass,
                        description = stringResource(id = R.string.search)
                    ) {
                        mActivity.showSearchPage()
                    }
                    RoundButton(
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
                    RoundButton(
                        modifier = Modifier.padding(top = 8.dp),
                        icon = Phosphor.GearSix,
                        description = stringResource(id = R.string.settings)
                    ) {
                        navigator(NavRoute.Prefs())
                    }
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
            },
            floatingActionButton = {
                if (updatesDownloads.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(start = 28.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExpandingFadingCard(
                            expanded = updaterExpanded,
                            collapsedView = {
                                val text = when {
                                    updates.isNotEmpty() -> pluralStringResource(
                                        id = R.plurals.updated_apps,
                                        count = updates.size,
                                        updates.size
                                    )

                                    else                 -> stringResource(R.string.downloading)
                                }
                                ExtendedFloatingActionButton(
                                    text = { Text(text = text) },
                                    icon = {
                                        Icon(
                                            imageVector = if (updaterExpanded) Phosphor.CaretDown else Phosphor.CircleWavyWarning,
                                            contentDescription = text
                                        )
                                    },
                                    elevation = FloatingActionButtonDefaults.elevation(
                                        0.dp
                                    ),
                                    onClick = { updaterExpanded = !updaterExpanded }
                                )
                            },
                            expandedView = {
                                Column(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        ActionButton(
                                            text = stringResource(id = R.string.update_all),
                                            icon = Phosphor.Download,
                                            modifier = Modifier.weight(1f),
                                            coloring = ColoringState.Positive,
                                        ) {
                                            val action = {
                                                NeoApp.wm.update(
                                                    *updates
                                                        .map {
                                                            Pair(
                                                                it.product.packageName,
                                                                it.product.repositoryId,
                                                            )
                                                        }
                                                        .toTypedArray()
                                                )
                                            }
                                            if (Preferences[Preferences.Key.DownloadShowDialog]) {
                                                dialogKey.value =
                                                    DialogKey.BatchDownload(
                                                        updates.map { it.product.name },
                                                        action
                                                    )
                                                openDialog.value = true
                                            } else action()
                                        }
                                        FilledRoundButton(
                                            description = stringResource(id = R.string.cancel),
                                            icon = Phosphor.CaretDown,
                                        ) {
                                            updaterExpanded = !updaterExpanded
                                        }
                                    }
                                    UpdatesHorizontalRecycler(
                                        productsList = updatesDownloads,
                                        repositories = dataState.reposMap,
                                        rowsNumber = updatesDownloads.size.coerceIn(1, 2),
                                    ) { item ->
                                        mActivity.navigateProduct(item.packageName)
                                    }
                                }
                            }
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
                preComposePages = 0
            )
        }
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

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            when (dialogKey.value) {
                is DialogKey.BatchDownload -> KeyDialogUI(
                    key = dialogKey.value,
                    openDialog = openDialog,
                    primaryAction = {
                        if (Preferences[Preferences.Key.ActionLockDialog] != Preferences.ActionLock.None)
                            mActivity.launchLockPrompt {
                                (dialogKey.value as DialogKey.BatchDownload).action()
                                openDialog.value = false
                            }
                        else {
                            (dialogKey.value as DialogKey.BatchDownload).action()
                            openDialog.value = false
                        }
                    },
                    onDismiss = {
                        dialogKey.value = null
                        openDialog.value = false
                    }
                )

                else                       -> {}
            }

        }
    }
}