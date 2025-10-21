package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.SortFilterChip
import com.machiav3lli.fdroid.ui.compose.ProductsCarousel
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.LatestVM
import com.machiav3lli.fdroid.viewmodels.MainVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestPage(
    viewModel: LatestVM = koinNeoViewModel(),
    mainVM: MainVM = koinNeoViewModel(),
) {
    val context = LocalContext.current
    val neoActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()

    val installedList by viewModel.installed.collectAsState(emptyMap())
    val secondaryList by viewModel.newProducts.collectAsState(emptyList())
    val primaryList by viewModel.updatedProducts.collectAsState(emptyList())
    val repositoriesMap by mainVM.reposMap.collectAsState()
    val favorites by viewModel.favorites.collectAsState(emptyArray())

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }
    val sortFilter by viewModel.sortFilter.collectAsState()
    val notModifiedSortFilter by remember(sortFilter) {
        derivedStateOf {
            Preferences[Preferences.Key.SortOrderAscendingLatest] == Preferences.Key.SortOrderAscendingLatest.default.value &&
                    Preferences[Preferences.Key.ReposFilterLatest] == Preferences.Key.ReposFilterLatest.default.value &&
                    Preferences[Preferences.Key.CategoriesFilterLatest] == Preferences.Key.CategoriesFilterLatest.default.value &&
                    Preferences[Preferences.Key.LicensesFilterLatest] == Preferences.Key.LicensesFilterLatest.default.value &&
                    Preferences[Preferences.Key.AntifeaturesFilterLatest] == Preferences.Key.AntifeaturesFilterLatest.default.value &&
                    Preferences[Preferences.Key.TargetSDKLatest] == Preferences.Key.TargetSDKLatest.default.value &&
                    Preferences[Preferences.Key.MinSDKLatest] == Preferences.Key.MinSDKLatest.default.value
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ReposFilterLatest,
                    Preferences.Key.CategoriesFilterLatest,
                    Preferences.Key.AntifeaturesFilterLatest,
                    Preferences.Key.LicensesFilterLatest,
                    Preferences.Key.SortOrderLatest,
                    Preferences.Key.SortOrderAscendingLatest,
                    Preferences.Key.TargetSDKLatest,
                    Preferences.Key.MinSDKLatest,
                        -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterLatest],
                            Preferences[Preferences.Key.CategoriesFilterLatest],
                            Preferences[Preferences.Key.AntifeaturesFilterLatest],
                            Preferences[Preferences.Key.LicensesFilterLatest],
                            Preferences[Preferences.Key.SortOrderLatest],
                            Preferences[Preferences.Key.SortOrderAscendingLatest],
                            Preferences[Preferences.Key.TargetSDKLatest],
                            Preferences[Preferences.Key.MinSDKLatest],
                        ).toString()
                    )

                    else -> {}
                }
            }
        }
    }

    BackHandler(scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        sheetShape = MaterialTheme.shapes.extraSmall,
        sheetContent = {
            SortFilterSheet(NavItem.Latest.destination) {
                scope.launch {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // TODO merge into one items-block
            if (!Preferences[Preferences.Key.HideNewApps]) item(key = "newAppsTitle") {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.new_applications),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                }
            }
            if (!Preferences[Preferences.Key.HideNewApps]) item(key = "newAppsCard") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (Preferences[Preferences.Key.AltNewApps]) {
                        ProductsHorizontalRecycler(
                            modifier = Modifier.weight(1f),
                            productsList = secondaryList,
                            repositories = repositoriesMap,
                        ) { item ->
                            neoActivity.navigateProduct(item.packageName)
                        }
                    } else {
                        ProductsCarousel(
                            modifier = Modifier.weight(1f),
                            productsList = secondaryList,
                            repositories = repositoriesMap,
                            favorites = favorites,
                            onFavouriteClick = {
                                mainVM.setFavorite(
                                    it.packageName,
                                    !favorites.contains(it.packageName)
                                )
                            },
                            onActionClick = { item, action ->
                                val installed = installedList[item.packageName]
                                val installFun = {
                                    NeoApp.wm.install(
                                        Pair(item.packageName, item.repositoryId)
                                    )
                                }

                                when (action) {
                                    is ActionState.Install -> {
                                        if (Preferences[Preferences.Key.DownloadShowDialog]) {
                                            dialogKey.value =
                                                DialogKey.Download(item.name, installFun)
                                            openDialog.value = true
                                        } else installFun()
                                    }

                                    is ActionState.Launch  -> installed?.let {
                                        context.onLaunchClick(
                                            it,
                                            neoActivity.supportFragmentManager
                                        )
                                    }

                                    else                   -> {}
                                }
                            },
                            onUserClick = { item ->
                                neoActivity.navigateProduct(item.packageName)
                            },
                        )
                    }
                }
            }
            item(key = "updatedAppsTitle") {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.recently_updated),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                    )
                    SortFilterChip(notModified = notModifiedSortFilter) {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                }
            }
            items(
                items = primaryList,
                key = { it.packageName },
            ) { item ->
                ProductsListItem(
                    item = item,
                    repo = repositoriesMap[item.repositoryId],
                    isFavorite = favorites.contains(item.packageName),
                    onUserClick = {
                        neoActivity.navigateProduct(it.packageName)
                    },
                    onFavouriteClick = {
                        mainVM.setFavorite(
                            it.packageName,
                            !favorites.contains(it.packageName)
                        )
                    },
                    installed = installedList[item.packageName],
                    onActionClick = {
                        val installed = installedList[it.packageName]
                        val action = {
                            NeoApp.wm.install(
                                Pair(it.packageName, it.repositoryId)
                            )
                        }
                        if (installed != null && installed.launcherActivities.isNotEmpty())
                            context.onLaunchClick(
                                installed,
                                neoActivity.supportFragmentManager
                            )
                        else if (Preferences[Preferences.Key.DownloadShowDialog]) {
                            dialogKey.value = DialogKey.Download(it.name, action)
                            openDialog.value = true
                        } else action()
                    }
                )
            }
        }
    }

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            when (dialogKey.value) {
                is DialogKey.Download -> KeyDialogUI(
                    key = dialogKey.value,
                    openDialog = openDialog,
                    primaryAction = {
                        if (Preferences[Preferences.Key.ActionLockDialog] != Preferences.ActionLock.None)
                            neoActivity.launchLockPrompt {
                                (dialogKey.value as DialogKey.Download).action()
                                openDialog.value = false
                            }
                        else {
                            (dialogKey.value as DialogKey.Download).action()
                            openDialog.value = false
                        }
                    },
                    onDismiss = {
                        dialogKey.value = null
                        openDialog.value = false
                    }
                )

                else                  -> {}
            }
        }
    }
}
