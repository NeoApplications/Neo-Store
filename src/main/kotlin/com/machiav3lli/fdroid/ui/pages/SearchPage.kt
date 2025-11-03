package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
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
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.data.entity.Source
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.SortFilterChip
import com.machiav3lli.fdroid.ui.components.TabButton
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesFour
import com.machiav3lli.fdroid.ui.compose.utils.addIfElse
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.MainVM
import com.machiav3lli.fdroid.viewmodels.SearchVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPage(
    viewModel: SearchVM = koinNeoViewModel(),
    mainVM: MainVM = koinNeoViewModel(),
) {
    val context = LocalContext.current
    val neoActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }

    val listState = rememberLazyListState()
    val installedList by viewModel.installed.collectAsState(emptyMap())
    val filteredProducts by viewModel.filteredProducts.collectAsState(emptyList())
    val query by viewModel.query.collectAsState()
    val source = viewModel.source.collectAsState()
    val sortFilter by viewModel.sortFilter.collectAsState()
    val dataState by mainVM.dataState.collectAsState()

    val currentTab by remember {
        derivedStateOf {
            listOf(Source.SEARCH, Source.SEARCH_INSTALLED, Source.SEARCH_NEW)
                .indexOf(source.value)
        }
    }

    val notModifiedSortFilter by remember(sortFilter) {
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
        withContext(Dispatchers.Default) {
            Preferences.subject.collect {
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
    }

    val searchBar: @Composable (() -> Unit) = {
        Column {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                SortFilterChip(
                    notModified = notModifiedSortFilter,
                    fullWidth = true,
                ) {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
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
                        viewModel.setSearchSource(Source.SEARCH)
                    }
                )
                TabButton(
                    text = stringResource(id = R.string.installed),
                    icon = Phosphor.ArrowSquareOut,
                    onClick = {
                        viewModel.setSearchSource(Source.SEARCH_INSTALLED)
                    }
                )
                TabButton(
                    text = stringResource(id = R.string.new_applications),
                    icon = Phosphor.CircleWavyWarning,
                    onClick = {
                        viewModel.setSearchSource(Source.SEARCH_NEW)
                    }
                )
            }
        }
    }

    val productsList: @Composable ((paddingValues: PaddingValues) -> Unit) =
        { paddingValues: PaddingValues ->
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
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
            ) {
                items(
                    items = filteredProducts,
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
            SortFilterSheet(NavItem.Search.destination) {
                scope.launch {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }
        }
    ) {
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
                    Column {
                        HorizontalDivider(thickness = 0.5.dp)
                        searchBar()
                    }
                }
            },
        ) { paddingValues ->
            if (filteredProducts.isEmpty() && query.isNotBlank())
                Column(
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
