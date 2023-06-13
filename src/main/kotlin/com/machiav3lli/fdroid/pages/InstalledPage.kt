package com.machiav3lli.fdroid.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.DownloadedItem
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretUp
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.InstalledVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledPage(viewModel: InstalledVM) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val scope = rememberCoroutineScope()
    val filteredPrimaryList by viewModel.filteredProducts.collectAsState()
    val secondaryList by viewModel.secondaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(emptyMap())
    val repositories by viewModel.repositories.collectAsState(null)
    var showDownloadsPage by remember { mutableStateOf(false) }
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesFlow.collectAsState(emptyArray())
    val lifecycleOwner = LocalLifecycleOwner.current
    val iconDetails by viewModel.iconDetails.collectAsState()
    val downloaded by viewModel.downloaded.collectAsState()
    val downloads = downloaded.filter { it.state is DownloadState.Downloading }
    var showSortSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)

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
                    Preferences.Key.ReposFilterInstalled,
                    Preferences.Key.CategoriesFilterInstalled,
                    Preferences.Key.AntifeaturesFilterInstalled,
                    Preferences.Key.LicensesFilterInstalled,
                    Preferences.Key.SortOrderInstalled,
                    Preferences.Key.SortOrderAscendingInstalled,
                    -> viewModel.setSortFilter(
                        listOf(
                            Preferences[Preferences.Key.ReposFilterInstalled],
                            Preferences[Preferences.Key.CategoriesFilterInstalled],
                            Preferences[Preferences.Key.AntifeaturesFilterInstalled],
                            Preferences[Preferences.Key.LicensesFilterInstalled],
                            Preferences[Preferences.Key.SortOrderInstalled],
                            Preferences[Preferences.Key.SortOrderAscendingInstalled],
                        ).toString()
                    )

                    else -> {}
                }
            }
        }
    }

    var updatesVisible by remember(secondaryList) { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier
            .blockBorder()
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            val fabColors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            )
            FilledTonalButton(
                modifier = Modifier.padding(4.dp),
                shape = MaterialTheme.shapes.large,
                colors = fabColors,
                onClick = { showDownloadsPage = !showDownloadsPage }
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (showDownloadsPage) Phosphor.ArrowSquareOut else Phosphor.Download,
                        contentDescription = stringResource(
                            id = if (showDownloadsPage) R.string.installed else R.string.downloads
                        )
                    )
                    Text(
                        text = stringResource(
                            id = if (showDownloadsPage) R.string.installed else R.string.downloads
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        AnimatedVisibility(
            visible = !showDownloadsPage,
            enter = slideInVertically { height -> height } + fadeIn(),
            exit = slideOutVertically { height -> -height } + fadeOut(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                item {
                    if (secondaryList.orEmpty().isNotEmpty()) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(
                                colors = ButtonDefaults.elevatedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                onClick = { updatesVisible = !updatesVisible }
                            ) {
                                Text(
                                    modifier = Modifier.padding(start = 4.dp),
                                    text = stringResource(id = R.string.updates),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    modifier = Modifier.size(18.dp),
                                    imageVector = if (updatesVisible) Phosphor.CaretUp else Phosphor.CaretDown,
                                    contentDescription = stringResource(id = R.string.updates)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            ActionChip(
                                text = stringResource(id = R.string.update_all),
                                icon = Phosphor.Download,
                            ) {
                                secondaryList?.let {
                                    mainActivityX.syncConnection.binder?.updateApps(
                                        it.map(
                                            Product::toItem
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                item {
                    if (updatesVisible && secondaryList.orEmpty().isNotEmpty()) {
                        ProductsHorizontalRecycler(
                            productsList = secondaryList,
                            repositories = repositoriesMap,
                            installedMap = installedList,
                            rowsNumber = secondaryList?.size?.coerceIn(1, 2) ?: 1,
                        ) { item ->
                            mainActivityX.navigateProduct(item.packageName)
                        }
                    }
                }
                if (downloads.isNotEmpty()) item {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.downloading)
                    )
                }
                if (downloads.isNotEmpty()) items(items = downloads
                    .sortedByDescending { it.changed }
                ) { item ->
                    DownloadedItem(
                        item = item,
                        iconDetails = iconDetails[item.packageName],
                        repo = repositoriesMap[item.state.repoId],
                        state = item.state
                    ) {
                        mainActivityX.navigateProduct(item.packageName)
                    }
                }
                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.installed_applications),
                            modifier = Modifier.weight(1f),
                        )
                        ActionChip(
                            text = stringResource(id = R.string.sort_filter),
                            icon = Phosphor.FunnelSimple
                        ) { showSortSheet = true }
                    }
                }
                items(
                    filteredPrimaryList?.map { it.toItem(installedList[it.packageName]) }
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
                        onFavouriteClick = { pi ->
                            viewModel.setFavorite(
                                pi.packageName,
                                !favorites.contains(pi.packageName)
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
        AnimatedVisibility(
            visible = showDownloadsPage,
            enter = slideInVertically { height -> -height } + fadeIn(),
            exit = slideOutVertically { height -> height } + fadeOut(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
            ) {
                items(items = downloaded.sortedByDescending { it.changed }) { item ->
                    DownloadedItem(
                        item = item,
                        iconDetails = iconDetails[item.packageName],
                        repo = repositoriesMap[item.state.repoId],
                        state = item.state
                    ) {
                        mainActivityX.navigateProduct(item.packageName)
                    }
                }
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
                SortFilterSheet(NavItem.Installed.destination) {
                    scope.launch { sortSheetState.hide() }
                    showSortSheet = false
                }
            }
        }
    }
}
