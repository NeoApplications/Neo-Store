package com.machiav3lli.fdroid.pages

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.entity.appCategoryIcon
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.service.worker.ExodusWorker
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.CategoriesList
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesFour
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraight
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.ExploreVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(viewModel: ExploreVM) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val installedList by viewModel.installed.collectAsState(emptyMap())
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by neoActivity.db.getExtrasDao().getFavoritesFlow().collectAsState(emptyArray())
    val categories by RepositoryUpdater.db.getCategoryDao()
        .getAllNamesFlow().collectAsState(emptyList())
    val selectedCategory = rememberSaveable {
        mutableStateOf("")
    }
    var showSortSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)

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
                    -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterExplore],
                            Preferences[Preferences.Key.CategoriesFilterExplore],
                            Preferences[Preferences.Key.AntifeaturesFilterExplore],
                            Preferences[Preferences.Key.LicensesFilterExplore],
                            Preferences[Preferences.Key.SortOrderExplore],
                            Preferences[Preferences.Key.SortOrderAscendingExplore],
                        ).toString()
                    )

                    else -> {}
                }
            }
        }
    }

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
                    icon = Phosphor.CirclesFour,
                    description = stringResource(id = R.string.categories)
                ) {
                    Preferences[Preferences.Key.CategoriesFilterExplore] = ""
                    selectedCategory.value = ""
                    viewModel.setSections(Section.All)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            ActionChip(
                text = stringResource(id = R.string.sort_filter),
                fullWidth = true,
                icon = Phosphor.FunnelSimple
            ) {
                showSortSheet = true
            }
        }
        Row {
            val favString = stringResource(id = R.string.favorite_applications)
            if (Preferences[Preferences.Key.ShowCategoriesBar]) CategoriesList(
                items = listOf(
                    Pair(favString, Phosphor.HeartStraight)
                ) + (categories.sorted().map { Pair(it, it.appCategoryIcon) }),
                selectedKey = selectedCategory,
            ) {
                when (it) {
                    favString -> {
                        Preferences[Preferences.Key.CategoriesFilterExplore] = FILTER_CATEGORY_ALL
                        selectedCategory.value = favString
                        viewModel.setSections(Section.FAVORITE)
                    }

                    else      -> {
                        Preferences[Preferences.Key.CategoriesFilterExplore] = it
                        selectedCategory.value = it
                        viewModel.setSections(Section.All)
                    }
                }
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(
                    items = filteredProducts?.map { it.toItem(installedList[it.packageName]) }
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
                            if (installed != null && installed.launcherActivities.isNotEmpty())
                                context.onLaunchClick(
                                    installed,
                                    neoActivity.supportFragmentManager
                                )
                            else
                                MainApplication.wm.install(it)
                        }
                    )
                }
            }
        }

    }

    if (showSortSheet) {
        ModalBottomSheet(
            sheetState = sortSheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            scrimColor = Color.Transparent,
            dragHandle = null,
            onDismissRequest = {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            },
        ) {
            SortFilterSheet(NavItem.Explore.destination) {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            }
        }
    }
}
