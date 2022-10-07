package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.components.ProductsListItem
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
fun LatestPage(viewModel: MainNavFragmentViewModelX) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val primaryList by viewModel.primaryProducts.observeAsState(null)
    val secondaryList by viewModel.secondaryProducts.observeAsState(null)
    val installedList by viewModel.installed.observeAsState(null)
    val repositories by viewModel.repositories.observeAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesLive.observeAsState(emptyArray())

    SideEffect {
        mainActivityX.syncConnection.bind(context)
        CoroutineScope(Dispatchers.IO).launch {
            mainActivityX.searchQuery.collect { newQuery ->
                if (newQuery != viewModel.searchQuery.value)
                    viewModel.searchQuery.postValue(newQuery)
            }
        }
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ReposFilterLatest,
                    Preferences.Key.CategoriesFilterLatest,
                    Preferences.Key.SortOrderLatest,
                    Preferences.Key.SortOrderAscendingLatest ->
                        viewModel.updatedFilter.postValue(true)
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
            Text(
                text = stringResource(id = R.string.new_applications),
                modifier = Modifier.padding(8.dp)
            )
            ProductsHorizontalRecycler(secondaryList, repositoriesMap) { item ->
                mainActivityX.navigateProduct(item.packageName)
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
                SuggestionChip(
                    shape = MaterialTheme.shapes.medium,
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    onClick = {
                        mainActivityX.navigateSortFilter(NavItem.Latest.destination)
                    },
                    icon = {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Phosphor.FunnelSimple,
                            contentDescription = stringResource(id = R.string.sort_filter)
                        )
                    },
                    label = {
                        Text(text = stringResource(id = R.string.sort_filter))
                    }
                )
            }
        }
        items(
            items = primaryList?.map { it.toItem() } ?: emptyList(),
        ) { item ->
            ProductsListItem(
                item = item,
                repo = repositoriesMap[item.repositoryId],
                isFavorite = favorites.contains(item.packageName),
                onUserClick = { mainActivityX.navigateProduct(it.packageName) },
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
