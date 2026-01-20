package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.data.entity.Source
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.SortFilterChip
import com.machiav3lli.fdroid.ui.components.TabButton
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesFour
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.MainVM
import com.machiav3lli.fdroid.viewmodels.SearchVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    viewModel: SearchVM = koinNeoViewModel(),
    mainVM: MainVM = koinNeoViewModel(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val neoActivity = LocalActivity.current as NeoActivity
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }

    val listState = rememberLazyListState()
    val pageState by viewModel.pageState.collectAsStateWithLifecycle()
    val dataState by mainVM.dataState.collectAsStateWithLifecycle()

    val currentTab by remember {
        derivedStateOf {
            listOf(Source.SEARCH, Source.SEARCH_INSTALLED, Source.SEARCH_NEW)
                .indexOf(pageState.source)
        }
    }

    val notModifiedSortFilter by remember(pageState.sortFilter) {
        derivedStateOf {
            Preferences[Preferences.Key.SortOrderSearch] == Preferences.Key.SortOrderSearch.default.value &&
                    Preferences[Preferences.Key.SortOrderAscendingSearch] == Preferences.Key.SortOrderAscendingSearch.default.value &&
                    Preferences[Preferences.Key.ReposFilterSearch] == Preferences.Key.ReposFilterSearch.default.value &&
                    Preferences[Preferences.Key.CategoriesFilterSearch] == Preferences.Key.CategoriesFilterSearch.default.value &&
                    Preferences[Preferences.Key.LicensesFilterSearch] == Preferences.Key.LicensesFilterSearch.default.value &&
                    Preferences[Preferences.Key.AntifeaturesFilterSearch] == Preferences.Key.AntifeaturesFilterSearch.default.value &&
                    Preferences[Preferences.Key.TargetSDKSearch] == Preferences.Key.TargetSDKSearch.default.value &&
                    Preferences[Preferences.Key.MinSDKSearch] == Preferences.Key.MinSDKSearch.default.value
        }
    }

    LaunchedEffect(Unit) {
        Preferences.addPreferencesChangeListener {
            when (it) {
                Preferences.Key.ReposFilterSearch,
                Preferences.Key.CategoriesFilterSearch,
                Preferences.Key.AntifeaturesFilterSearch,
                Preferences.Key.LicensesFilterSearch,
                Preferences.Key.SortOrderSearch,
                Preferences.Key.SortOrderAscendingSearch,
                Preferences.Key.TargetSDKSearch,
                Preferences.Key.MinSDKSearch,
                    -> viewModel.setSortFilter(
                    listOf(
                        Preferences[Preferences.Key.ReposFilterSearch],
                        Preferences[Preferences.Key.CategoriesFilterSearch],
                        Preferences[Preferences.Key.AntifeaturesFilterSearch],
                        Preferences[Preferences.Key.LicensesFilterSearch],
                        Preferences[Preferences.Key.SortOrderSearch],
                        Preferences[Preferences.Key.SortOrderAscendingSearch],
                        Preferences[Preferences.Key.TargetSDKSearch],
                        Preferences[Preferences.Key.MinSDKSearch],
                    ).toString()
                )

                else -> {}
            }
        }
    }

    val searchBar: @Composable (() -> Unit) = {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopBar(
                withTopBarInsets = !Preferences[Preferences.Key.BottomSearchBar],
            ) {
                WideSearchField(
                    query = pageState.query,
                    modifier = Modifier.fillMaxWidth(),
                    showCloseButton = true,
                    onQueryChanged = { newQuery ->
                        if (newQuery != pageState.query)
                            viewModel.setSearchQuery(newQuery)
                    },
                    onCleanQuery = {
                        viewModel.setSearchQuery("")
                    },
                    onClose = onDismiss
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                contentPadding = PaddingValues(horizontal = 8.dp),
            ) {
                item {
                    TabButton(
                        text = stringResource(id = R.string.all),
                        icon = Phosphor.CirclesFour,
                        selected = currentTab == 0,
                        onClick = {
                            viewModel.setSearchSource(Source.SEARCH)
                        },
                    )
                }
                item {
                    TabButton(
                        text = stringResource(id = R.string.installed),
                        icon = Phosphor.ArrowSquareOut,
                        selected = currentTab == 1,
                        onClick = {
                            viewModel.setSearchSource(Source.SEARCH_INSTALLED)
                        },
                    )
                }
                item {
                    TabButton(
                        text = stringResource(id = R.string.new_applications),
                        icon = Phosphor.CircleWavyWarning,
                        selected = currentTab == 2,
                        onClick = {
                            viewModel.setSearchSource(Source.SEARCH_NEW)
                        },
                    )
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                SortFilterChip(
                    notModified = notModifiedSortFilter,
                    fullWidth = true,
                ) {
                    neoActivity.navigateSortFilterSheet(NavItem.Search)
                }
            }
        }
    }

    val productsList: @Composable ((paddingValues: PaddingValues) -> Unit) =
        { paddingValues: PaddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
            ) {
                items(
                    items = pageState.filteredProducts,
                    key = { it.packageName },
                ) { item ->
                    ProductsListItem(
                        item = item,
                        repo = dataState.reposMap[item.repositoryId],
                        isFavorite = dataState.favorites.contains(item.packageName),
                        onUserClick = {
                            neoActivity.navigateProduct(it.packageName)
                        },
                        onFavouriteClick = {
                            mainVM.setFavorite(
                                it.packageName,
                                !dataState.favorites.contains(it.packageName)
                            )
                        },
                        installed = pageState.installedMap[item.packageName],
                        onActionClick = {
                            val installed = pageState.installedMap[it.packageName]
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

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            if (!Preferences[Preferences.Key.BottomSearchBar]) {
                Column {
                    searchBar()
                    HorizontalDivider(thickness = 0.5.dp)
                }
            }
        },
        bottomBar = {
            if (Preferences[Preferences.Key.BottomSearchBar]) {
                Column(
                    modifier = Modifier.windowInsetsPadding(BottomAppBarDefaults.windowInsets)
                ) {
                    HorizontalDivider(thickness = 0.5.dp)
                    searchBar()
                }
            }
        },
    ) { paddingValues ->
        if (pageState.filteredProducts.isEmpty() && pageState.query.isNotBlank())
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = R.string.application_not_found)
                )
            }
        else
            productsList(paddingValues)
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
