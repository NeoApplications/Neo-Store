package com.machiav3lli.fdroid.pages

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.DialogKey
import com.machiav3lli.fdroid.service.worker.ExodusWorker
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.common.BottomSheet
import com.machiav3lli.fdroid.ui.compose.ProductsCarousel
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.LatestVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestPage(viewModel: LatestVM) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity
    val scope = rememberCoroutineScope()
    val filteredPrimaryList by viewModel.filteredProducts.collectAsState()
    val secondaryList by viewModel.secondaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(emptyMap())
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by neoActivity.db.getExtrasDao().getFavoritesFlow().collectAsState(emptyArray())
    var showSortSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }

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
                    -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterLatest],
                            Preferences[Preferences.Key.CategoriesFilterLatest],
                            Preferences[Preferences.Key.AntifeaturesFilterLatest],
                            Preferences[Preferences.Key.LicensesFilterLatest],
                            Preferences[Preferences.Key.SortOrderLatest],
                            Preferences[Preferences.Key.SortOrderAscendingLatest],
                        ).toString()
                    )

                    else -> {}
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.new_applications),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (Preferences[Preferences.Key.AltNewApps]) {
                    ProductsHorizontalRecycler(
                        modifier = Modifier.weight(1f),
                        productsList = secondaryList,
                        repositories = repositoriesMap,
                        installedMap = installedList,
                    ) { item ->
                        neoActivity.navigateProduct(item.packageName)
                    }
                } else {
                    ProductsCarousel(
                        modifier = Modifier.weight(1f),
                        productsList = secondaryList,
                        repositories = repositoriesMap,
                        installedMap = installedList,
                        favorites = favorites,
                        onFavouriteClick = {
                            viewModel.setFavorite(
                                it.packageName,
                                !favorites.contains(it.packageName)
                            )
                        },
                        onActionClick = {
                            val installed = installedList[it.packageName]
                            val action = { MainApplication.wm.install(it) }
                            if (installed != null && installed.launcherActivities.isNotEmpty())
                                context.onLaunchClick(
                                    installed,
                                    neoActivity.supportFragmentManager
                                )
                            else if (Preferences[Preferences.Key.DownloadShowDialog]) {
                                dialogKey.value = DialogKey.Download(it.name, action)
                                openDialog.value = true
                            } else action()
                        },
                        onUserClick = { item ->
                            neoActivity.navigateProduct(item.packageName)
                        },
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.recently_updated),
                    modifier = Modifier.weight(1f),
                )
                ActionChip(
                    text = stringResource(id = R.string.sort_filter),
                    icon = Phosphor.FunnelSimple
                ) { showSortSheet = true }
            }
        }
        items(
            items = filteredPrimaryList?.map { it.toItem(installedList[it.packageName]) }
                ?: emptyList(),
        ) { item ->
            ProductsListItem(
                item = item,
                repo = repositoriesMap[item.repositoryId],
                isFavorite = favorites.contains(item.packageName),
                onUserClick = {
                    ExodusWorker.fetchExodusInfo(item.packageName)
                    neoActivity.navigateProduct(it.packageName)
                },
                onFavouriteClick = {
                    viewModel.setFavorite(
                        it.packageName,
                        !favorites.contains(it.packageName)
                    )
                },
                installed = installedList[item.packageName],
                onActionClick = {
                    val installed = installedList[it.packageName]
                    val action = { MainApplication.wm.install(it) }
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

    if (showSortSheet) {
        BottomSheet(
            sheetState = sortSheetState,
            onDismiss = {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            },
        ) {
            SortFilterSheet(NavItem.Latest.destination) {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
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
