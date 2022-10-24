package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsVerticalRecycler
import com.machiav3lli.fdroid.ui.compose.components.ActionChip
import com.machiav3lli.fdroid.ui.compose.components.CategoryChip
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.viewmodels.MainNavFragmentViewModelX
import com.machiav3lli.fdroid.utility.onLaunchClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExplorePage(viewModel: MainNavFragmentViewModelX) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val products by viewModel.primaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(null)
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesLive.observeAsState(emptyArray())
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts by remember(products, searchQuery) {
        mutableStateOf(products?.matchSearchQuery(searchQuery))
    }

    SideEffect {
        CoroutineScope(Dispatchers.IO).launch {
            mainActivityX.searchQuery.collect { newQuery ->
                if (newQuery != searchQuery)
                    searchQuery = newQuery
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ReposFilterExplore,
                    Preferences.Key.CategoriesFilterExplore,
                    Preferences.Key.SortOrderExplore,
                    Preferences.Key.SortOrderAscendingExplore ->
                        viewModel.setUpdatedFilter(true)
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

            ActionChip(
                textId = R.string.sort_filter,
                icon = Phosphor.FunnelSimple
            ) { mainActivityX.navigateSortFilter(NavItem.Explore.destination) }
            Spacer(modifier = Modifier.weight(1f))
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
        }
        ProductsVerticalRecycler(
            productsList = filteredProducts,
            repositories = repositoriesMap,
            favorites = favorites,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onUserClick = { item ->
                mainActivityX.navigateProduct(item.packageName, item.developer)
            },
            onFavouriteClick = { item ->
                viewModel.setFavorite(
                    item.packageName,
                    !favorites.contains(item.packageName)
                )
            },
            getInstalled = { installedList?.get(it.packageName) }
        ) { item ->
            val installed = installedList?.get(item.packageName)
            if (installed != null && installed.launcherActivities.isNotEmpty())
                context.onLaunchClick(installed, mainActivityX.supportFragmentManager)
            else
                mainActivityX.syncConnection.binder?.installApps(listOf(item))
        }
    }
}
