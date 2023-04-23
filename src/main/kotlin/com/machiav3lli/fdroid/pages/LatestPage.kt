package com.machiav3lli.fdroid.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.compose.utils.vertical
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.LatestVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatestPage(viewModel: LatestVM) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val scope = rememberCoroutineScope()
    val filteredPrimaryList by viewModel.filteredProducts.collectAsState()
    val secondaryList by viewModel.secondaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(null)
    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesFlow.collectAsState(emptyArray())
    var showSortSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)

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
                    Preferences.Key.LicensesFilterLatest,
                    Preferences.Key.SortOrderLatest,
                    Preferences.Key.SortOrderAscendingLatest,
                    -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterLatest],
                            Preferences[Preferences.Key.CategoriesFilterLatest],
                            Preferences[Preferences.Key.AntifeaturesFilterLatest],
                            Preferences[Preferences.Key.LicensesFilterLatest],
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
            .blockBorder()
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
                ) { showSortSheet = true }
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

    if (showSortSheet) {
        ModalBottomSheet(
            sheetState = sortSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = null,
            scrimColor = Color.Transparent,
            onDismissRequest = {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            }
        ) {
            SortFilterPage(NavItem.Latest.destination) {
                scope.launch { sortSheetState.hide() }
                showSortSheet = false
            }
        }
    }
}
