/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.compose.screens.preferences

import android.annotation.SuppressLint
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.backup.BackupFile
import com.saggitt.omega.compose.components.BackupCard
import com.saggitt.omega.compose.components.BaseDialog
import com.saggitt.omega.compose.components.ElevatedActionButton
import com.saggitt.omega.compose.components.ViewWithActionBar
import com.saggitt.omega.compose.components.preferences.CreateBackupDialogUI
import com.saggitt.omega.compose.components.preferences.PreferenceGroupHeading
import com.saggitt.omega.compose.components.preferences.RestoreBackupDialogUI
import com.saggitt.omega.theme.OmegaAppTheme

@SuppressLint("WrongConstant")
@Composable
fun BackupsPrefPage() {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    val openDialog = remember { mutableStateOf(false) }
    val localBackups by remember(openDialog.value) {
        mutableStateOf(
            BackupFile.listLocalBackups(context)
                .plus(prefs.recentBackups.onGetValue().map { BackupFile(context, it) }
                    .filter { it.meta != null })
                .sortedBy { it.meta?.timestamp }
        )
    }
    var dialogPref by remember { mutableStateOf<Any?>(null) }
    val onDialog = { pref: Any ->
        dialogPref = pref
        openDialog.value = true
    }
    val openRestoreResult =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                if (!localBackups.map(BackupFile::uri).contains(resultUri)) {
                    prefs.recentBackups.add(resultUri)
                }

                onDialog(BackupFile(context, resultUri))
            }
        }

    OmegaAppTheme {
        ViewWithActionBar(
            title = stringResource(R.string.backups)
        ) { paddingValues ->
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                columns = GridCells.Fixed(2),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    ElevatedActionButton(
                        textId = R.string.title_create,
                        iconId = R.drawable.ic_backup,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            onDialog("backup")
                        }
                    )
                }
                item {
                    ElevatedActionButton(
                        textId = R.string.restore_backup,
                        iconId = R.drawable.ic_restore,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            openRestoreResult.launch(BackupFile.EXTRA_MIME_TYPES)
                        }
                    )
                }
                if (localBackups.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        PreferenceGroupHeading(stringResource(id = R.string.local_backups))
                    }
                    items(items = localBackups, span = { GridItemSpan(1) }) {
                        BackupCard(backup = it) {
                            onDialog(it)
                        }
                    }
                }
            }

            if (openDialog.value) {
                BaseDialog(openDialogCustom = openDialog) {
                    when (dialogPref) {
                        "backup" -> CreateBackupDialogUI(
                            openDialogCustom = openDialog
                        )
                        is BackupFile -> RestoreBackupDialogUI(
                            backupFile = dialogPref as BackupFile,
                            openDialogCustom = openDialog
                        )
                    }
                }
            }
        }
    }
}
