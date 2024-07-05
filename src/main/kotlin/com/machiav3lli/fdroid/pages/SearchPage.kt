package com.machiav3lli.fdroid.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.DialogKey
import com.machiav3lli.fdroid.entity.Source
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.SortFilterButton
import com.machiav3lli.fdroid.ui.components.TabButton
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.ui.components.common.BottomSheet
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesFour
import com.machiav3lli.fdroid.ui.compose.utils.addIfElse
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.SearchVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(viewModel: SearchVM) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val installedList by viewModel.installed.collectAsState(emptyMap())
    val filteredProducts by viewModel.filteredProducts.collectAsState(emptyList())
    val repositories by viewModel.repositories.collectAsState(emptyList())
    val repositoriesMap = remember(repositories) {
        mutableMapOf(*repositories.map { repo -> Pair(repo.id, repo) }.toTypedArray())
    }
    val favorites by neoActivity.db.getExtrasDao().getFavoritesFlow().collectAsState(emptyArray())
    val query by neoActivity.searchQuery.collectAsState()
    val source = viewModel.source.collectAsState()
    val currentTab by remember {
        derivedStateOf {
            listOf(Source.SEARCH, Source.SEARCH_INSTALLED, Source.SEARCH_NEW)
                .indexOf(source.value)
        }
    }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }

    val sortFilter by viewModel.sortFilter.collectAsState()
    val notModifiedSortFilter by remember(sortFilter) {
        derivedStateOf {
            Preferences[Preferences.Key.SortOrderSearch] == Preferences.Key.SortOrderSearch.default.value &&
                    Preferences[Preferences.Key.SortOrderAscendingSearch] == Preferences.Key.SortOrderAscendingSearch.default.value &&
                    Preferences[Preferences.Key.ReposFilterSearch] == Preferences.Key.ReposFilterSearch.default.value &&
                    Preferences[Preferences.Key.CategoriesFilterSearch] == Preferences.Key.CategoriesFilterSearch.default.value &&
                    Preferences[Preferences.Key.LicensesFilterSearch] == Preferences.Key.LicensesFilterSearch.default.value &&
                    Preferences[Preferences.Key.AntifeaturesFilterSearch] == Preferences.Key.AntifeaturesFilterSearch.default.value
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ReposFilterSearch,
                    Preferences.Key.CategoriesFilterSearch,
                    Preferences.Key.AntifeaturesFilterSearch,
                    Preferences.Key.LicensesFilterSearch,
                    Preferences.Key.SortOrderSearch,
                    Preferences.Key.SortOrderAscendingSearch,
                    Preferences.Key.SearchApps,
                    -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterSearch],
                            Preferences[Preferences.Key.CategoriesFilterSearch],
                            Preferences[Preferences.Key.AntifeaturesFilterSearch],
                            Preferences[Preferences.Key.LicensesFilterSearch],
                            Preferences[Preferences.Key.SortOrderSearch],
                            Preferences[Preferences.Key.SortOrderAscendingSearch],
                        ).toString()
                    )

                    else -> {}
                }
            }
        }
    }

    val searchBar: @Composable (() -> Unit) = {
        Column {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WideSearchField(
                    modifier = Modifier.weight(1f),
                    query = query,
                    onClose = {
                        neoActivity.setSearchQuery("")
                    },
                    onQueryChanged = { newQuery ->
                        if (newQuery != query) neoActivity.setSearchQuery(newQuery)
                    },
                    focusOnCompose = false,
                )
                SortFilterButton(notModified = notModifiedSortFilter) {
                    showSortSheet = true
                }
            }
            PrimaryTabRow(
                containerColor = Color.Transparent,
                selectedTabIndex = currentTab,
                divider = {}
            ) {
                TabButton(
                    text = stringResource(id = R.string.all),
                    icon = Phosphor.CirclesFour,
                    onClick = {
                        viewModel.setSource(Source.SEARCH)
                    }
                )
                TabButton(
                    text = stringResource(id = R.string.installed),
                    icon = Phosphor.ArrowSquareOut,
                    onClick = {
                        viewModel.setSource(Source.SEARCH_INSTALLED)
                    }
                )
                TabButton(
                    text = stringResource(id = R.string.new_applications),
                    icon = Phosphor.CircleWavyWarning,
                    onClick = {
                        viewModel.setSource(Source.SEARCH_NEW)
                    }
                )
            }
        }
    }

    LaunchedEffect(key1 = query) {
        viewModel.setSearchQuery(query)
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            if (!Preferences[Preferences.Key.BottomSearchBar]) {
                Column {
                    searchBar()
                    HorizontalDivider()
                }
            }
        },
        bottomBar = {
            if (Preferences[Preferences.Key.BottomSearchBar]) {
                Column {
                    HorizontalDivider()
                    searchBar()
                }
            }
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .addIfElse(
                    Preferences[Preferences.Key.BottomSearchBar],
                    factory = {
                        padding(bottom = paddingValues.calculateBottomPadding())
                    },
                    elseFactory = {
                        padding(top = paddingValues.calculateTopPadding())
                    }
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            state = listState,
        ) {
            items(
                items = filteredProducts,
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
    }

    if (showSortSheet) {
        BottomSheet(
            sheetState = sortSheetState,
            onDismiss = {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            },
        ) {
            SortFilterSheet(NavItem.Search.destination) {
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
