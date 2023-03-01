package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.components.ActionChip
import com.machiav3lli.fdroid.ui.compose.components.CategoryChip
import com.machiav3lli.fdroid.ui.compose.components.ProductsListItem
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.SideNavBar
import com.machiav3lli.fdroid.ui.viewmodels.ExploreVM
import com.machiav3lli.fdroid.utility.onLaunchClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ExplorePage(viewModel: ExploreVM) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val installedList by viewModel.installed.collectAsState(null)
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesFlow.collectAsState(emptyArray())
    val categories by RepositoryUpdater.db.categoryDao.allNamesFlow.collectAsState(emptyList())
    val selectedCategory = remember(Preferences[Preferences.Key.CategoriesFilterExplore]) {
        mutableStateOf(Preferences[Preferences.Key.CategoriesFilterExplore])
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            mainActivityX.searchQuery.collect { newQuery ->
                viewModel.setSearchQuery(newQuery)
            }
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ReposFilterExplore,
                    Preferences.Key.CategoriesFilterExplore,
                    Preferences.Key.AntifeaturesFilterExplore,
                    Preferences.Key.SortOrderExplore,
                    Preferences.Key.SortOrderAscendingExplore,
                    -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterExplore],
                            Preferences[Preferences.Key.CategoriesFilterExplore],
                            Preferences[Preferences.Key.AntifeaturesFilterExplore],
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
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var favoriteFilter by remember {
                mutableStateOf(false)
            }

            CategoryChip(
                category = stringResource(id = R.string.favorite_applications),
                isSelected = favoriteFilter,
                onSelected = {
                    favoriteFilter = !favoriteFilter
                    viewModel.setSections(
                        if (it) Section.FAVORITE
                        else Section.All
                    )
                }
            )
            Spacer(modifier = Modifier.weight(1f))
            ActionChip(
                text = stringResource(id = R.string.sort_filter),
                icon = Phosphor.FunnelSimple
            ) { mainActivityX.navigateSortFilter(NavItem.Explore.destination) }
        }
        Row {
            SideNavBar(
                keys = listOf(FILTER_CATEGORY_ALL) + (categories.sorted()),
                selectedKey = selectedCategory,
            ) {
                Preferences[Preferences.Key.CategoriesFilterExplore] = it
                selectedCategory.value = it
                scope.launch {
                    listState.animateScrollToItem(0)
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Absolute.spacedBy(4.dp),
                state = listState,
            ) {
                items(
                    items = filteredProducts?.map { it.toItem(installedList?.get(it.packageName)) }
                        ?: emptyList()
                ) { item ->
                    ProductsListItem(
                        item = item,
                        repo = repositoriesMap[item.repositoryId],
                        isFavorite = favorites.contains(item.packageName),
                        onUserClick = {
                            mainActivityX.syncConnection.binder?.fetchExodusInfo(item.packageName)
                            mainActivityX.navigateProduct(it.packageName)
                        },
                        onFavouriteClick = {
                            viewModel.setFavorite(
                                it.packageName,
                                !favorites.contains(it.packageName)
                            )
                        },
                        installed = installedList?.get(item.packageName),
                        onActionClick = {
                            val installed = installedList?.get(it.packageName)
                            if (installed != null && installed.launcherActivities.isNotEmpty())
                                context.onLaunchClick(
                                    installed,
                                    mainActivityX.supportFragmentManager
                                )
                            else
                                mainActivityX.syncConnection.binder?.installApps(listOf(it))
                        }
                    )
                }
            }
        }
    }
}
