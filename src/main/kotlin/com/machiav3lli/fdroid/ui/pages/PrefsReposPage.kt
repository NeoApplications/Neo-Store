package com.machiav3lli.fdroid.ui.pages

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.ui.compose.icons.phosphor.Plus
import com.machiav3lli.fdroid.INTENT_ACTION_BINARY_EYE
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.manager.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.RepositoryItem
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroupHeading
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.QrCode
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import com.machiav3lli.fdroid.viewmodels.SheetNavigationData
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PrefsReposPage(viewModel: PrefsVM = koinViewModel()) {
    val mActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()
    val paneNavigator = rememberListDetailPaneScaffoldNavigator<Any>()

    val repos by viewModel.filteredRepositories.collectAsState()
    val partedRepos by remember {
        derivedStateOf {
            repos.partition { it.enabled }
        }
    }

    val query by viewModel.reposSearchQuery.collectAsState()
    val sheetData: MutableState<SheetNavigationData?> = remember { mutableStateOf(null) }
    val intentAddress = viewModel.address.collectAsState()
    val intentFingerprint = viewModel.fingerprint.collectAsState()

    DisposableEffect(key1 = intentAddress.value, key2 = intentFingerprint.value) {
        if (intentAddress.value.isNotEmpty()) {
            scope.launch {
                paneNavigator.navigateTo(
                    ListDetailPaneScaffoldRole.Detail,
                    SheetNavigationData(
                        viewModel.addNewRepository(
                            address = intentAddress.value,
                            fingerprint = intentFingerprint.value
                        ),
                        true
                    )
                )
            }
        }
        onDispose {
            if (intentAddress.value.isNotEmpty()) viewModel.setIntent("", "")
        }
    }

    NavigableListDetailPaneScaffold(
        navigator = paneNavigator,
        listPane = {
            //AnimatedPane { } TODO re-add when fixing recomposition issue
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                floatingActionButton = {
                    if (Intent(INTENT_ACTION_BINARY_EYE).resolveActivity(mActivity.packageManager) != null) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FloatingActionButton(
                                shape = MaterialTheme.shapes.extraLarge,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                onClick = {
                                    scope.launch {
                                        paneNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            SheetNavigationData(
                                                viewModel.addNewRepository(),
                                                true
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Phosphor.Plus,
                                    contentDescription = stringResource(id = R.string.add_repository)
                                )
                            }
                            FloatingActionButton(
                                shape = MaterialTheme.shapes.extraLarge,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                onClick = mActivity::openScanner
                            ) {
                                Icon(
                                    imageVector = Phosphor.QrCode,
                                    contentDescription = stringResource(id = R.string.scan_qr_code)
                                )
                            }
                        }
                    } else {
                        ExtendedFloatingActionButton(
                            shape = MaterialTheme.shapes.extraLarge,
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = {
                                scope.launch {
                                    paneNavigator.navigateTo(
                                        ListDetailPaneScaffoldRole.Detail,
                                        SheetNavigationData(
                                            viewModel.addNewRepository(),
                                            true
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Phosphor.Plus,
                                contentDescription = stringResource(id = R.string.add_repository)
                            )
                        }
                    }
                }
            ) { _ ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        WideSearchField(
                            modifier = Modifier.fillMaxWidth(),
                            query = query,
                            focusOnCompose = false,
                            onClose = { viewModel.setSearchQuery("") },
                            onQueryChanged = { newQuery ->
                                if (newQuery != query) viewModel.setSearchQuery(newQuery)
                            }
                        )
                    }
                    item {
                        PreferenceGroupHeading(heading = stringResource(id = R.string.enabled))
                    }
                    items(items = partedRepos.first, key = { it.id }) {
                        RepositoryItem(
                            modifier = Modifier.animateItem(),
                            repository = it,
                            onClick = { repo ->
                                viewModel.viewModelScope.launch {
                                    SyncWorker.enableRepo(repo, !repo.enabled)
                                }
                            },
                            onLongClick = { repo ->
                                paneNavigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    SheetNavigationData(repo.id, false)
                                )
                            }
                        )
                    }
                    item {
                        PreferenceGroupHeading(heading = stringResource(id = R.string.disabled))
                    }
                    items(items = partedRepos.second, key = { it.id }) {
                        RepositoryItem(
                            modifier = Modifier.animateItem(),
                            repository = it,
                            onClick = { repo ->
                                viewModel.viewModelScope.launch {
                                    SyncWorker.enableRepo(repo, !repo.enabled)
                                }
                            },
                            onLongClick = { repo ->
                                paneNavigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Detail,
                                    SheetNavigationData(repo.id, false)
                                )
                            }
                        )
                    }
                }
            }
        },
        detailPane = {
            sheetData.value = paneNavigator.currentDestination
                ?.takeIf { it.pane == this.role }?.content
                ?.let { it as? SheetNavigationData }

            sheetData.value?.let {
                AnimatedPane {
                    RepoPage(
                        repositoryId = it.repositoryId,
                        initEditMode = it.editMode,
                        onDismiss = {
                            paneNavigator.navigateTo(ListDetailPaneScaffoldRole.List)
                        }
                    ) { newRepo -> viewModel.updateRepo(newRepo) }
                }
            }
        }
    )
}
