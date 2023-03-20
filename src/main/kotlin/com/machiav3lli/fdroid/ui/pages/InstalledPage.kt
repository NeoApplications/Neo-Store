package com.machiav3lli.fdroid.ui.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.DownloadService
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.components.ActionChip
import com.machiav3lli.fdroid.ui.compose.components.DownloadedItem
import com.machiav3lli.fdroid.ui.compose.components.DownloadsListItem
import com.machiav3lli.fdroid.ui.compose.components.ProductsListItem
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretUp
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.FunnelSimple
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.viewmodels.InstalledVM
import com.machiav3lli.fdroid.utility.onLaunchClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledPage(viewModel: InstalledVM) {
    val context = LocalContext.current
    val mainActivityX = context as MainActivityX
    val filteredPrimaryList by viewModel.filteredProducts.collectAsState()
    val secondaryList by viewModel.secondaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(null)
    val repositories by viewModel.repositories.collectAsState(null)
    var showDownloadsPage by remember { mutableStateOf(false) }
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by mainActivityX.db.extrasDao.favoritesFlow.collectAsState(emptyArray())
    val lifecycleOwner = LocalLifecycleOwner.current
    val downloads = viewModel.downloadsMap
    val iconDetails by viewModel.iconDetails.collectAsState()
    val downloaded by viewModel.downloaded.collectAsState()

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

    DisposableEffect(key1 = lifecycleOwner) {
        val downloadConnection = Connection(DownloadService::class.java, onBind = { _, binder ->
            CoroutineScope(Dispatchers.Default).launch {
                binder.stateSubject
                    .collectLatest {
                        viewModel.updateDownloadState(it)
                    }
            }
        })

        downloadConnection.bind(context)
        onDispose {
            downloadConnection.unbind(context)
        }
    }

    var updatesVisible by remember(secondaryList) { mutableStateOf(true) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
            Column(
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                AnimatedVisibility(visible = secondaryList.orEmpty().isNotEmpty()) {
                    Column {
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
                        AnimatedVisibility(visible = updatesVisible) {
                            ProductsHorizontalRecycler(
                                modifier = Modifier.weight(1f),
                                productsList = secondaryList,
                                repositories = repositoriesMap,
                                rowsNumber = secondaryList?.size?.coerceIn(1, 2) ?: 1,
                            ) { item ->
                                mainActivityX.navigateProduct(item.packageName)
                            }
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    if (downloads.isNotEmpty()) item {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = stringResource(id = R.string.downloading)
                        )
                    }
                    if (downloads.isNotEmpty()) items(downloads.toList()) { (packageName, value) ->
                        val (item, state) = value
                        val installed = installedList?.get(packageName)
                        if (state.version != installed?.version) DownloadsListItem(
                            item = item,
                            state = state,
                            repo = repositoriesMap[value.first.repositoryId],
                            installed = installedList?.get(packageName)
                        ) {
                            mainActivityX.navigateProduct(item.packageName)
                        }
                        else viewModel.updateDownloadState(
                            DownloadService.State.Cancel(
                                item.packageName,
                                item.name,
                                state.version,
                                state.cacheFileName,
                                state.repoId,
                            )
                        )
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
                            ) { mainActivityX.navigateSortFilter(NavItem.Installed.destination) }
                        }
                    }
                    items(
                        filteredPrimaryList?.map { it.toItem() } ?: emptyList()
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
        }
        AnimatedVisibility(
            visible = showDownloadsPage,
            enter = slideInVertically { height -> -height } + fadeIn(),
            exit = slideOutVertically { height -> height } + fadeOut(),
        ) {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                ) {
                    items(items = downloaded.sortedByDescending { it.changed }) { item ->
                        DownloadedItem(
                            item = item,
                            iconDetails = iconDetails[item.packageName],
                            repo = repositoriesMap[item.state.repoId],
                            state = item.state,
                            installed = installedList?.get(item.packageName)
                        ) {
                            mainActivityX.navigateProduct(item.packageName)
                        }
                    }
                }
            }
        }
    }
}
