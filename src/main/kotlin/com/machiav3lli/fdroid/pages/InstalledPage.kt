package com.machiav3lli.fdroid.pages

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.service.worker.ExodusWorker
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.DownloadedItem
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.TabButton
import com.machiav3lli.fdroid.ui.components.TabIndicator
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InstalledPage(viewModel: InstalledVM) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity
    val scope = rememberCoroutineScope()
    val filteredPrimaryList by viewModel.filteredProducts.collectAsState()
    val secondaryList by viewModel.secondaryProducts.collectAsState(null)
    val installedList by viewModel.installed.collectAsState(emptyMap())
    val repositories by viewModel.repositories.collectAsState(null)
    val pagerState = rememberPagerState { 2 }
    val repositoriesMap by remember(repositories) {
        mutableStateOf(repositories?.associateBy { repo -> repo.id } ?: emptyMap())
    }
    val favorites by neoActivity.db.getExtrasDao().getFavoritesFlow().collectAsState(emptyArray())
    val iconDetails by viewModel.iconDetails.collectAsState()
    val downloaded by viewModel.downloaded.collectAsState()
    val downloads = downloaded.filter { it.state is DownloadState.Downloading }
    var showSortSheet by remember { mutableStateOf(false) }
    val sortSheetState = rememberModalBottomSheetState(true)

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
    ) { _ ->
        Column {
            TabRow(selectedTabIndex = pagerState.currentPage,
                indicator = { tabPositions ->
                    TabIndicator(tabPositions[pagerState.currentPage])
                }
            ) {
                TabButton(
                    text = stringResource(id = R.string.installed),
                    icon = Phosphor.ArrowSquareOut,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                TabButton(
                    text = stringResource(id = R.string.downloads),
                    icon = Phosphor.Download,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }
            HorizontalPager(state = pagerState, userScrollEnabled = false) { index ->
                when (index) {
                    0 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            item {
                                if (secondaryList.orEmpty().isNotEmpty()) {
                                    Row(
                                        modifier = Modifier.padding(
                                            horizontal = 8.dp,
                                            vertical = 4.dp
                                        ),
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
                                                MainApplication.wm.update(
                                                    *it.map(Product::toItem).toTypedArray()
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
                                        neoActivity.navigateProduct(item.packageName)
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
                                .sortedBy { it.state.name }
                            ) { item ->
                                DownloadedItem(
                                    item = item,
                                    iconDetails = iconDetails[item.packageName],
                                    repo = repositoriesMap[item.state.repoId],
                                    state = item.state,
                                ) {
                                    neoActivity.navigateProduct(item.packageName)
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
                                        ExodusWorker.fetchExodusInfo(item.packageName)
                                        neoActivity.navigateProduct(it.packageName)
                                    },
                                    onFavouriteClick = { pi ->
                                        viewModel.setFavorite(
                                            pi.packageName,
                                            !favorites.contains(pi.packageName)
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

                    1 -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(items = downloaded.sortedByDescending { it.changed }) { item ->
                                DownloadedItem(
                                    item = item,
                                    iconDetails = iconDetails[item.packageName],
                                    repo = repositoriesMap[item.state.repoId],
                                    state = item.state,
                                    onUserClick = { neoActivity.navigateProduct(item.packageName) },
                                    onEraseClick = { viewModel.eraseDownloaded(item) },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSortSheet) {
            ModalBottomSheet(
                sheetState = sortSheetState,
                containerColor = MaterialTheme.colorScheme.background,
                scrimColor = Color.Transparent,
                dragHandle = null,
                onDismissRequest = {
                    scope.launch { sortSheetState.hide() }
                    showSortSheet = false
                },
            ) {
                SortFilterSheet(NavItem.Installed.destination) {
                    scope.launch { sortSheetState.hide() }
                    showSortSheet = false
                }
            }
        }
    }
}
