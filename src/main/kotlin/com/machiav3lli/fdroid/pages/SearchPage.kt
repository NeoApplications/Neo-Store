package com.machiav3lli.fdroid.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.machiav3lli.fdroid.service.worker.ExodusWorker
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
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
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val installedList by viewModel.installed.collectAsState(emptyMap())
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by neoActivity.db.getExtrasDao().getFavoritesFlow().collectAsState(emptyArray())
    val query by neoActivity.searchQuery.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)

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

    Column(
        Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            ActionChip(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.sort_filter),
                fullWidth = true,
                icon = Phosphor.FunnelSimple
            ) {
                showSortSheet = true
            }
        }
        LazyColumn(
            modifier = Modifier
                .blockBorder()
                .weight(1f, true),
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
        Row(
            modifier = Modifier.padding(
                top = 8.dp,
                start = 8.dp,
                end = 8.dp,
            ),
            horizontalArrangement = Arrangement.Center,
        ) {
            WideSearchField(
                query = query,
                onClose = {
                    neoActivity.setSearchQuery("")
                    viewModel.setSearchQuery("")
                },
                onQueryChanged = { newQuery ->
                    if (newQuery != query) {
                        neoActivity.setSearchQuery(newQuery)
                        viewModel.setSearchQuery(newQuery)
                    }
                }
            )
        }
    }

    if (showSortSheet) {
        ModalBottomSheet(
            sheetState = sortSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            scrimColor = Color.Transparent,
            onDismissRequest = {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            }
        ) {
            SortFilterSheet(NavItem.Search.destination) {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            }
        }
    }
}
