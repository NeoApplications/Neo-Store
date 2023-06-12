package com.machiav3lli.fdroid.pages

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.backup.ui.compose.icons.phosphor.Plus
import com.machiav3lli.fdroid.INTENT_ACTION_BINARY_EYE
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.ui.compose.RepositoriesRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.QrCode
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefsReposPage(viewModel: PrefsVM, address: String, fingerprint: String) {
    val context = LocalContext.current
    val prefsActivityX = context as PrefsActivityX
    val scope = rememberCoroutineScope()
    val repos by viewModel.repositories.collectAsState()
    val sheetData by viewModel.showSheet.collectAsState(initial = null)
    val sheetState = rememberModalBottomSheetState(true)

    LaunchedEffect(key1 = address) {
        if (address.isNotEmpty()) {
            viewModel.showRepositorySheet(
                editMode = true,
                addNew = true,
                address = address,
                fingerprint = fingerprint
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            val fabColors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            )
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 6.dp,
                shape = MaterialTheme.shapes.medium,
            ) {
                if (Intent(INTENT_ACTION_BINARY_EYE).resolveActivity(prefsActivityX.packageManager) != null) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        FilledTonalButton(
                            shape = MaterialTheme.shapes.medium,
                            colors = fabColors,
                            onClick = {
                                viewModel.showRepositorySheet(
                                    editMode = true,
                                    addNew = true
                                )
                            }) {
                            Icon(
                                imageVector = Phosphor.Plus,
                                contentDescription = stringResource(id = R.string.add_repository)
                            )
                        }
                        FilledTonalButton(
                            shape = MaterialTheme.shapes.medium,
                            colors = fabColors,
                            onClick = prefsActivityX::openScanner
                        ) {
                            Icon(
                                imageVector = Phosphor.QrCode,
                                contentDescription = stringResource(id = R.string.scan_qr_code)
                            )
                        }
                    }
                } else {
                    FilledTonalButton(
                        shape = MaterialTheme.shapes.medium,
                        colors = fabColors,
                        onClick = {
                            viewModel.showRepositorySheet(
                                editMode = true,
                                addNew = true
                            )
                        }) {
                        Icon(
                            imageVector = Phosphor.Plus,
                            contentDescription = stringResource(id = R.string.add_repository)
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val sortedRepoList = remember(repos) { repos.sortedBy { !it.enabled } }
        RepositoriesRecycler(
            modifier = Modifier
                .blockBorder()
                .padding(paddingValues),
            repositoriesList = sortedRepoList,
            onClick = {
                CoroutineScope(Dispatchers.Default).launch {
                    prefsActivityX.syncConnection.binder?.setEnabled(it, it.enabled)
                }
            },
            onLongClick = { viewModel.showRepositorySheet(it.id) }
        )

        if (sheetData != null) {
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.background,
                scrimColor = Color.Transparent,
                onDismissRequest = {
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
