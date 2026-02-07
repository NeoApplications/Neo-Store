package com.machiav3lli.fdroid.ui.pages

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.ui.components.ActionChip
import com.machiav3lli.fdroid.ui.components.DownloadedItem
import com.machiav3lli.fdroid.ui.components.ProductsListItem
import com.machiav3lli.fdroid.ui.components.SegmentedTabButton
import com.machiav3lli.fdroid.ui.components.SortFilterChip
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Eraser
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.onLaunchClick
import com.machiav3lli.fdroid.viewmodels.InstalledVM
import com.machiav3lli.fdroid.viewmodels.MainVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstalledPage(
    viewModel: InstalledVM = koinNeoViewModel(),
    mainVM: MainVM = koinNeoViewModel(),
) {
    val installedTab = rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        Preferences.addPreferencesChangeListener {
            when (it) {
                Preferences.Key.ReposFilterInstalled,
                Preferences.Key.CategoriesFilterInstalled,
                Preferences.Key.AntifeaturesFilterInstalled,
                Preferences.Key.LicensesFilterInstalled,
                Preferences.Key.SortOrderInstalled,
                Preferences.Key.SortOrderAscendingInstalled,
                Preferences.Key.TargetSDKInstalled,
                Preferences.Key.MinSDKInstalled,
                    -> viewModel.setSortFilter(
                    listOf(
                        Preferences[Preferences.Key.ReposFilterInstalled],
                        Preferences[Preferences.Key.CategoriesFilterInstalled],
                        Preferences[Preferences.Key.AntifeaturesFilterInstalled],
                        Preferences[Preferences.Key.LicensesFilterInstalled],
                        Preferences[Preferences.Key.SortOrderInstalled],
                        Preferences[Preferences.Key.SortOrderAscendingInstalled],
                        Preferences[Preferences.Key.TargetSDKInstalled],
                        Preferences[Preferences.Key.MinSDKInstalled],
                    ).toString()
                )

                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .padding(top = 4.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.extraLarge,
                )
                .padding(horizontal = 4.dp),
        ) {
            SegmentedTabButton(
                text = stringResource(id = R.string.installed),
                icon = Phosphor.ArrowSquareOut,
                selected = {
                    installedTab.intValue == 0
                },
                onClick = {
                    installedTab.intValue = 0
                },
            )
            SegmentedTabButton(
                text = stringResource(id = R.string.downloads),
                icon = Phosphor.Download,
                selected = {
                    installedTab.intValue == 1
                },
                onClick = {
                    installedTab.intValue = 1
                },
            )
        }

        when (installedTab.intValue) {
            0 -> InstallsPage(viewModel, mainVM)
            1 -> DownloadedPage(viewModel)
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstallsPage(viewModel: InstalledVM, mainVM: MainVM) {
    val context = LocalContext.current
    val neoActivity = LocalActivity.current as NeoActivity

    val pageState by viewModel.installedPageState.collectAsStateWithLifecycle()
    val dataState by mainVM.dataState.collectAsStateWithLifecycle()

    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }
    val notModifiedSortFilter by remember(pageState.sortFilter) {
        derivedStateOf {
            Preferences[Preferences.Key.SortOrderInstalled] == Preferences.Key.SortOrderInstalled.default.value &&
                    Preferences[Preferences.Key.SortOrderAscendingInstalled] == Preferences.Key.SortOrderAscendingInstalled.default.value &&
                    Preferences[Preferences.Key.ReposFilterInstalled] == Preferences.Key.ReposFilterInstalled.default.value &&
                    Preferences[Preferences.Key.CategoriesFilterInstalled] == Preferences.Key.CategoriesFilterInstalled.default.value &&
                    Preferences[Preferences.Key.LicensesFilterInstalled] == Preferences.Key.LicensesFilterInstalled.default.value &&
                    Preferences[Preferences.Key.AntifeaturesFilterInstalled] == Preferences.Key.AntifeaturesFilterInstalled.default.value &&
                    Preferences[Preferences.Key.TargetSDKInstalled] == Preferences.Key.TargetSDKInstalled.default.value &&
                    Preferences[Preferences.Key.MinSDKInstalled] == Preferences.Key.MinSDKInstalled.default.value
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item(key = "installedTitle") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(id = R.string.installed_applications),
                        modifier = Modifier.padding(start = 8.dp),
                        maxLines = 2,
                    )
                    SortFilterChip(notModified = notModifiedSortFilter) {
                        neoActivity.navigateSortFilterSheet(NavItem.Installed)
                    }
                }
            }
            items(pageState.installedProducts, key = { it.packageName }) { item ->
                ProductsListItem(
                    item = item,
                    repo = dataState.reposMap[item.repositoryId],
                    isFavorite = dataState.favorites.contains(item.packageName),
                    onUserClick = {
                        neoActivity.navigateProduct(it.packageName)
                    },
                    onFavouriteClick = { pi ->
                        mainVM.setFavorite(
                            pi.packageName,
                            !dataState.favorites.contains(pi.packageName)
                        )
                    },
                    installed = pageState.installedMap[item.packageName],
                    onActionClick = {
                        val installed = pageState.installedMap[it.packageName]
                        val action = {
                            NeoApp.wm.install(
                                Pair(it.packageName, it.repositoryId)
                            )
                        }
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
                is DialogKey.Download -> KeyDialogUI(
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

                else                  -> {}
            }
        }
    }
}

@SuppressLint("UnusedCrossfadeTargetStateParameter")
@Composable
fun DownloadedPage(viewModel: InstalledVM) {
    val neoActivity = LocalActivity.current as NeoActivity
    val pageState by viewModel.downloadedPageState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(vertical = 8.dp),
    ) {
        item(key = "downloadedTitle") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(id = R.string.downloads),
                    modifier = Modifier.padding(start = 8.dp),
                    maxLines = 2,
                )
                Crossfade(pageState.sortedDownloaded.isNotEmpty()) { isNotEmpty ->
                    if (isNotEmpty) ActionChip(
                        text = stringResource(id = R.string.erase_all),
                        icon = Phosphor.Eraser,
                    ) {
                        pageState.sortedDownloaded.forEach {
                            viewModel.eraseDownloaded(it)
                        }
                    }
                }
            }
        }
        items(pageState.sortedDownloaded, key = { it.itemKey }) { item ->
            val state by remember(item) {
                derivedStateOf { item.state }
            }
            val iconDetails = pageState.iconDetails[item.packageName]

            DownloadedItem(
                download = item,
                iconDetails = iconDetails,
                repo = pageState.reposMap[iconDetails?.repositoryId
                    ?: item.repositoryId],
                state = state,
                onUserClick = { neoActivity.navigateProduct(item.packageName) },
                onEraseClick = { viewModel.eraseDownloaded(item) },
            )
        }
    }
}
