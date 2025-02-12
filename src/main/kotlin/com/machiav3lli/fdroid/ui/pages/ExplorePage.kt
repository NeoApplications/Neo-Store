package com.machiav3lli.fdroid.ui.pages

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.data.entity.Page
import com.machiav3lli.fdroid.data.entity.Source
import com.machiav3lli.fdroid.data.entity.appCategoryIcon
import com.machiav3lli.fdroid.data.index.RepositoryUpdater
import com.machiav3lli.fdroid.ui.components.CategoriesList
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.SortFilterChip
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ListBullets
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utils.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.MainVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(viewModel: MainVM = koinViewModel()) {
    // TODO fix crash on closing detailPane when in AnimatedPane
    val context = LocalContext.current
    val neoActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val installedList by viewModel.installed.collectAsState(emptyMap())
    val filteredProducts by viewModel.productsExplore.collectAsState(emptyList())
    val repositories = viewModel.repositories.collectAsState(emptyList())
    val repositoriesMap by remember {
        derivedStateOf {
            repositories.value.associateBy { repo -> repo.id }
        }
    }
    val favorites by neoActivity.db.getExtrasDao().getFavoritesFlow().collectAsState(emptyArray())
    val categories by RepositoryUpdater.db.getCategoryDao()
        .getAllNamesFlow().collectAsState(emptyList())
    val selectedCategory = rememberSaveable {
        mutableStateOf("")
    }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }

    val sortFilter by viewModel.sortFilterExplore.collectAsState()
    val notModifiedSortFilter by remember(sortFilter) {
        derivedStateOf {
            Preferences[Preferences.Key.SortOrderExplore] == Preferences.Key.SortOrderExplore.default.value &&
                    Preferences[Preferences.Key.SortOrderAscendingExplore] == Preferences.Key.SortOrderAscendingExplore.default.value &&
                    Preferences[Preferences.Key.ReposFilterExplore] == Preferences.Key.ReposFilterExplore.default.value &&
                    Preferences[Preferences.Key.LicensesFilterExplore] == Preferences.Key.LicensesFilterExplore.default.value &&
                    Preferences[Preferences.Key.AntifeaturesFilterExplore] == Preferences.Key.AntifeaturesFilterExplore.default.value &&
                    Preferences[Preferences.Key.TargetSDKExplore] == Preferences.Key.TargetSDKExplore.default.value &&
                    Preferences[Preferences.Key.MinSDKExplore] == Preferences.Key.MinSDKExplore.default.value
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ReposFilterExplore,
                    Preferences.Key.CategoriesFilterExplore,
                    Preferences.Key.AntifeaturesFilterExplore,
                    Preferences.Key.LicensesFilterExplore,
                    Preferences.Key.SortOrderExplore,
                    Preferences.Key.SortOrderAscendingExplore,
                    Preferences.Key.TargetSDKExplore,
                    Preferences.Key.MinSDKExplore,
                        -> viewModel.setSortFilter(
                        Page.EXPLORE,
                        listOf(
                            Preferences[Preferences.Key.ReposFilterExplore],
                            Preferences[Preferences.Key.CategoriesFilterExplore],
                            Preferences[Preferences.Key.AntifeaturesFilterExplore],
                            Preferences[Preferences.Key.LicensesFilterExplore],
                            Preferences[Preferences.Key.SortOrderExplore],
                            Preferences[Preferences.Key.SortOrderAscendingExplore],
                            Preferences[Preferences.Key.TargetSDKExplore],
                            Preferences[Preferences.Key.MinSDKExplore],
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
            SortFilterSheet(NavItem.Explore.destination) {
                scope.launch {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }
        }
    ) {
        Column(
            Modifier
                .background(Color.Transparent)
                .fillMaxSize(),
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                AnimatedVisibility(selectedCategory.value.isNotEmpty()) {
                    TopBarAction(
                        icon = Phosphor.ListBullets,
                        description = stringResource(id = R.string.categories)
                    ) {
                        Preferences[Preferences.Key.CategoriesFilterExplore] = ""
                        selectedCategory.value = ""
                        viewModel.setExploreSource(Source.NONE)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                SortFilterChip(
                    notModified = notModifiedSortFilter,
                    fullWidth = true,
                ) {
                    scope.launch {
                        scaffoldState.bottomSheetState.expand()
                    }
                }
            }
            Column {
                val favString = stringResource(id = R.string.favorite_applications)
                CategoriesList(
                    items = listOf(
                        Pair(favString, Phosphor.HeartStraight),
                    ) + (categories.sorted().map { Pair(it, it.appCategoryIcon) }),
                    selectedKey = selectedCategory,
                ) {
                    when (it) {
                        favString -> {
                            Preferences[Preferences.Key.CategoriesFilterExplore] =
                                FILTER_CATEGORY_ALL
                            selectedCategory.value = favString
                            viewModel.setExploreSource(Source.FAVORITES)
                        }

                        else      -> {
                            Preferences[Preferences.Key.CategoriesFilterExplore] = it
                            selectedCategory.value = it
                            viewModel.setExploreSource(Source.AVAILABLE)
                        }
                    }
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    state = listState,
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(items = filteredProducts, key = { it.packageName }) { item ->
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
                                val action = { NeoApp.wm.install(it) }
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
