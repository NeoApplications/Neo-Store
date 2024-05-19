package com.machiav3lli.fdroid.pages

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.machiav3lli.backup.ui.compose.icons.phosphor.Plus
import com.machiav3lli.fdroid.INTENT_ACTION_BINARY_EYE
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.RepositoryItem
import com.machiav3lli.fdroid.ui.components.WideSearchField
import com.machiav3lli.fdroid.ui.components.common.BottomSheet
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroupHeading
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.QrCode
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PrefsReposPage(viewModel: PrefsVM) {
    val context = LocalContext.current
    val mActivity = context as NeoActivity
    val scope = rememberCoroutineScope()
    val repos = viewModel.filteredRepositories.collectAsState()
    val query by viewModel.reposSearchQuery.collectAsState()
    val sheetData by viewModel.showSheet.collectAsState(initial = null)
    val sheetState = rememberModalBottomSheetState(true)
    val intentAdress = viewModel.address.collectAsState()
    val intentFingerprint = viewModel.fingerprint.collectAsState()

    val partedRrepos by remember {
        derivedStateOf {
            repos.value.partition { it.enabled }
        }
    }

    DisposableEffect(key1 = intentAdress.value, key2 = intentFingerprint.value) {
        if (intentAdress.value.isNotEmpty()) {
            viewModel.showRepositorySheet(
                editMode = true,
                addNew = true,
                address = intentAdress.value,
                fingerprint = intentFingerprint.value,
            )
        }
        onDispose {
            if (intentAdress.value.isNotEmpty()) viewModel.setIntent("", "")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
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
                            viewModel.showRepositorySheet(
                                editMode = true,
                                addNew = true
                            )
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
                        viewModel.showRepositorySheet(
                            editMode = true,
                            addNew = true
                        )
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
            items(items = partedRrepos.first, key = { it.id }) {
                RepositoryItem(
                    modifier = Modifier.animateItemPlacement(),
                    repository = it,
                    onClick = { repo ->
                        viewModel.viewModelScope.launch {
                            SyncWorker.enableRepo(repo, !repo.enabled)
                        }
                    },
                    onLongClick = { repo -> viewModel.showRepositorySheet(repo.id) }
                )
            }
            item {
                PreferenceGroupHeading(heading = stringResource(id = R.string.disabled))
            }
            items(items = partedRrepos.second, key = { it.id }) {
                RepositoryItem(
                    modifier = Modifier.animateItemPlacement(),
                    repository = it,
                    onClick = { repo ->
                        viewModel.viewModelScope.launch {
                            SyncWorker.enableRepo(repo, !repo.enabled)
                        }
                    },
                    onLongClick = { repo -> viewModel.showRepositorySheet(repo.id) }
                )
            }
        }

        if (sheetData != null) {
            BottomSheet(
                sheetState = sheetState,
                onDismiss = {
                    viewModel.closeRepositorySheet()
                    scope.launch { sheetState.hide() }
                }
            ) {
                sheetData?.let {
                    RepoPage(
                        repositoryId = it.repositoryId,
                        initEditMode = it.editMode,
                        onDismiss = {
                            viewModel.closeRepositorySheet()
                            scope.launch { sheetState.hide() }
                        }
                    ) { newRepo -> viewModel.updateRepo(newRepo) }
                }
            }
        }
    }
}
