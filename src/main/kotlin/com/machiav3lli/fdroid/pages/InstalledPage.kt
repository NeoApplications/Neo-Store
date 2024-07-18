package com.machiav3lli.fdroid.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import com.machiav3lli.fdroid.entity.DialogKey
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.DownloadedItem
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.SegmentedTabButton
import com.machiav3lli.fdroid.ui.components.SortFilterChip
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretDown
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CaretUp
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.InstalledVM
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InstalledPage(viewModel: InstalledVM) {
    val scope = rememberCoroutineScope()

    val pages: ImmutableList<@Composable () -> Unit> = persistentListOf(
        { InstallsPage(viewModel) },
        { DownloadedPage(viewModel) }
    )
    val pagerState = rememberPagerState { pages.size }
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }

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

    Column(
        Modifier
            .background(Color.Transparent)
            .fillMaxSize(),
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
        ) {
            SegmentedTabButton(
                text = stringResource(id = R.string.installed),
                icon = Phosphor.ArrowSquareOut,
                selected = { currentPage == 0 },
                index = 0,
                count = 2,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            )
            SegmentedTabButton(
                text = stringResource(id = R.string.downloads),
                icon = Phosphor.Download,
                selected = { currentPage == 1 },
                index = 1,
                count = 2,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            )
        }
        HorizontalPager(
            state = pagerState,
            beyondBoundsPageCount = 1
        ) { index ->
            pages[index].invoke()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallsPage(viewModel: InstalledVM) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity
    val scope = rememberCoroutineScope()

    val installedList by viewModel.installed.collectAsState(emptyMap())
    val updates by viewModel.updates.collectAsState(emptyList())
    val installedItems by viewModel.installedProducts.collectAsState(emptyList())
    val repositories by viewModel.repositories.collectAsState(emptyList())
    val repositoriesMap = remember(repositories) {
        mutableMapOf(*repositories.map { repo -> Pair(repo.id, repo) }.toTypedArray())
    }
    val favorites by neoActivity.db.getExtrasDao().getFavoritesFlow()
        .collectAsState(emptyArray())

    val updatesAvailable by remember {
        derivedStateOf {
            updates.isNotEmpty()
        }
    }
    val iconDetails by viewModel.iconDetails.collectAsState()
    val downloaded = viewModel.downloaded.collectAsState()
    val downloads = remember {
        derivedStateOf {
            downloaded.value.filter { it.state is DownloadState.Downloading }
        }
    }
    val downloadsRunning by remember {
        derivedStateOf { downloads.value.isNotEmpty() }
    }

    val sortedDownloads by remember {
        derivedStateOf { downloads.value.sortedBy { it.state.name } }
    }


    var updatesVisible by remember { mutableStateOf(true) }

    val scaffoldState = rememberBottomSheetScaffoldState()
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }
    val sortFilter by viewModel.sortFilter.collectAsState()
    val notModifiedSortFilter by remember(sortFilter) {
        derivedStateOf {
            Preferences[Preferences.Key.SortOrderInstalled] == Preferences.Key.SortOrderInstalled.default.value &&
                    Preferences[Preferences.Key.SortOrderAscendingInstalled] == Preferences.Key.SortOrderAscendingInstalled.default.value &&
                    Preferences[Preferences.Key.ReposFilterInstalled] == Preferences.Key.ReposFilterInstalled.default.value &&
                    Preferences[Preferences.Key.CategoriesFilterInstalled] == Preferences.Key.CategoriesFilterInstalled.default.value &&
                    Preferences[Preferences.Key.LicensesFilterInstalled] == Preferences.Key.LicensesFilterInstalled.default.value &&
                    Preferences[Preferences.Key.AntifeaturesFilterInstalled] == Preferences.Key.AntifeaturesFilterInstalled.default.value
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 0.dp,
        sheetDragHandle = null,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        sheetContent = {
            SortFilterSheet(NavItem.Installed.destination) {
                scope.launch {
                    scaffoldState.bottomSheetState.partialExpand()
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            if (updatesAvailable) {
                item {
                    val cardColor by animateColorAsState(
                        targetValue = if (updatesVisible) MaterialTheme.colorScheme.surfaceContainerHighest
                        else Color.Transparent,
                        label = "cardColor"
                    )

                    Surface(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        ),
                        shape = MaterialTheme.shapes.large,
                        color = cardColor,
                    ) {
                        Column(
                            Modifier.padding(4.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                ElevatedButton(
                                    colors = ButtonDefaults.elevatedButtonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ),
                                    onClick = { updatesVisible = !updatesVisible }
                                ) {
                                    Text(
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
                                AnimatedVisibility(updatesVisible) {
                                    ActionChip(
                                        text = stringResource(id = R.string.update_all),
                                        icon = Phosphor.Download,
                                    ) {
                                        val action = {
                                            MainApplication.wm.update(
                                                *updates.toTypedArray()
                                            )
                                        }
                                        if (Preferences[Preferences.Key.DownloadShowDialog]) {
                                            dialogKey.value =
                                                DialogKey.BatchDownload(
                                                    updates.map(ProductItem::name), action
                                                )
                                            openDialog.value = true
                                        } else action()
                                    }
                                }
                            }
                            AnimatedVisibility(updatesVisible) {
                                ProductsHorizontalRecycler(
                                    productsList = updates,
                                    repositories = repositoriesMap,
                                    rowsNumber = updates.size.coerceIn(1, 2),
                                ) { item ->
                                    neoActivity.navigateProduct(item.packageName)
                                }
                            }
                        }
                    }
                }
            }
            if (downloadsRunning) {
                item {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(id = R.string.downloading)
                    )
                }
                items(sortedDownloads, key = { it.itemKey }) { item ->
                    DownloadedItem(
                        download = item,
                        iconDetails = iconDetails[item.packageName],
                        repo = repositoriesMap[item.state.repoId],
                        state = item.state,
                    ) {
                        neoActivity.navigateProduct(item.packageName)
                    }
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
                    SortFilterChip(notModified = notModifiedSortFilter) {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                }
            }
            items(installedItems, key = { it.packageName }) { item ->
                ProductsListItem(
                    item = item,
                    repo = repositoriesMap[item.repositoryId],
                    isFavorite = favorites.contains(item.packageName),
                    onUserClick = {
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
                        val action = { MainApplication.wm.install(it) }
                        if (installed != null && installed.launcherActivities.isNotEmpty())
                            context.onLaunchClick(
                                installed,
                                neoActivity.supportFragmentManager
                            )
                        else if (Preferences[Preferences.Key.DownloadShowDialog]) {
                            dialogKey.value = DialogKey.Download(it.name, action)
                            openDialog.value = true
                        } else action()
                    }
                )
            }
        }
    }

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            when (dialogKey.value) {
                is DialogKey.Download      -> KeyDialogUI(
                    key = dialogKey.value,
                    openDialog = openDialog,
                    primaryAction = {
                        if (Preferences[Preferences.Key.ActionLockDialog] != Preferences.ActionLock.None)
                            neoActivity.launchLockPrompt {
                                (dialogKey.value as DialogKey.Download).action()
                                openDialog.value = false
                            }
                        else {
                            (dialogKey.value as DialogKey.Download).action()
                            openDialog.value = false
                        }
                    },
                    onDismiss = {
                        dialogKey.value = null
                        openDialog.value = false
                    }
                )

                is DialogKey.BatchDownload -> KeyDialogUI(
                    key = dialogKey.value,
                    openDialog = openDialog,
                    primaryAction = {
                        if (Preferences[Preferences.Key.ActionLockDialog] != Preferences.ActionLock.None)
                            neoActivity.launchLockPrompt {
                                (dialogKey.value as DialogKey.BatchDownload).action()
                                openDialog.value = false
                            }
                        else {
                            (dialogKey.value as DialogKey.BatchDownload).action()
                            openDialog.value = false
                        }
                    },
                    onDismiss = {
                        dialogKey.value = null
                        openDialog.value = false
                    }
                )

                else                       -> {}
            }
        }
    }
}

@Composable
fun DownloadedPage(viewModel: InstalledVM) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity

    val repositories by viewModel.repositories.collectAsState(null)
    val repositoriesMap by remember {
        derivedStateOf {
            repositories?.associateBy { repo -> repo.id } ?: emptyMap()
        }
    }
    val iconDetails by viewModel.iconDetails.collectAsState()
    val downloaded = viewModel.downloaded.collectAsState()
    val sortedDownloaded by remember {
        derivedStateOf {
            downloaded.value.sortedByDescending { it.changed / 60_000L }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        items(sortedDownloaded, key = { it.itemKey }) { item ->
            val state by remember(item) {
                derivedStateOf { item.state }
            }

            DownloadedItem(
                download = item,
                iconDetails = iconDetails[item.packageName],
                repo = repositoriesMap[state.repoId],
                state = state,
                onUserClick = { neoActivity.navigateProduct(item.packageName) },
                onEraseClick = { viewModel.eraseDownloaded(item) },
            )
        }
    }
}
