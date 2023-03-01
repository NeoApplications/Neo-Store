package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.components.ActionChip
import com.machiav3lli.fdroid.ui.compose.components.ProductsListItem
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.compose.utils.vertical
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.viewmodels.LatestVM
import com.machiav3lli.fdroid.utility.onLaunchClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun LatestPage(viewModel: LatestVM) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val filteredPrimaryList by viewModel.filteredProducts.collectAsState()
    val secondaryList by viewModel.secondaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(null)
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesFlow.collectAsState(emptyArray())

    LaunchedEffect(Unit) {
        mainActivityX.syncConnection.bind(context)
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
                    Preferences.Key.ReposFilterLatest,
                    Preferences.Key.CategoriesFilterLatest,
                    Preferences.Key.AntifeaturesFilterLatest,
                    Preferences.Key.SortOrderLatest,
                    Preferences.Key.SortOrderAscendingLatest,
                    -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterLatest],
                            Preferences[Preferences.Key.CategoriesFilterLatest],
                            Preferences[Preferences.Key.AntifeaturesFilterLatest],
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
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(id = R.string.new_applications),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier
                        .vertical()
                        .rotate(-90f)
                        .padding(8.dp),
                )
                ProductsHorizontalRecycler(
                    modifier = Modifier.weight(1f),
                    productsList = secondaryList,
                    repositories = repositoriesMap
                ) { item ->
                    mainActivityX.navigateProduct(item.packageName)
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
                ) { mainActivityX.navigateSortFilter(NavItem.Latest.destination) }
            }
        }
        items(
            items = filteredPrimaryList?.map { it.toItem() } ?: emptyList(),
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
