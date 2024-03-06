package com.machiav3lli.fdroid.pages

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.machiav3lli.fdroid.ui.compose.RepositoriesRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.QrCode
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrefsReposPage(viewModel: PrefsVM) {
    val context = LocalContext.current
    val mActivity = context as NeoActivity
    val scope = rememberCoroutineScope()
    val repos by viewModel.repositories.collectAsState()
    val sheetData by viewModel.showSheet.collectAsState(initial = null)
    val sheetState = rememberModalBottomSheetState(true)
    val intentAdress = viewModel.address.collectAsState()
    val intentFingerprint = viewModel.fingerprint.collectAsState()

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
        RepositoriesRecycler(
            repositoriesList = repos,
            onClick = {
                viewModel.viewModelScope.launch {
                    SyncWorker.enableRepo(it, !it.enabled)
                }
            },
            onLongClick = { viewModel.showRepositorySheet(it.id) }
        )

        if (sheetData != null) {
            ModalBottomSheet(
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                scrimColor = Color.Transparent,
                dragHandle = null,
                onDismissRequest = {
                    viewModel.closeRepositorySheet()
                    scope.launch { sheetState.hide() }
                }
            ) {
                sheetData?.let {
                    RepoPage(
                        repoId = it.repoId,
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
