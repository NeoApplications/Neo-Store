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
package com.saggitt.omega.compose.components.preferences

import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.saggitt.omega.BACKUP_CONTENT_DEFAULT
import com.saggitt.omega.BACKUP_LOCATION_CUSTOM
import com.saggitt.omega.BACKUP_LOCATION_INTERNAL
import com.saggitt.omega.backup.BackupFile
import com.saggitt.omega.backup.BackupTaskViewModel
import com.saggitt.omega.backupContentItems
import com.saggitt.omega.backupLocationItems
import com.saggitt.omega.compose.components.DialogNegativeButton
import com.saggitt.omega.compose.components.DialogPositiveButton
import com.saggitt.omega.compose.components.MultiSelectionListItem
import com.saggitt.omega.compose.components.SingleSelectionListItem
import com.saggitt.omega.util.getTimestampForFile
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBackupDialogUI(
    openDialogCustom: MutableState<Boolean>
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val textFieldFocusRequester = remember { FocusRequester() }
    val prefs = Utilities.getOmegaPrefs(context)
    var name by remember { mutableStateOf("NL_" + getTimestampForFile()) }
    val uri by remember(name) {
        mutableStateOf(
            Uri.fromFile(
                File(BackupFile.getFolder(context), name)
            )
        )
    }
    var selectedLocation by remember { mutableStateOf(BACKUP_LOCATION_INTERNAL) }
    var selectedContent by remember {
        mutableStateOf(
            if (ContextCompat.getSystemService(context, WallpaperManager::class.java)
                    ?.wallpaperInfo == null
            ) BACKUP_CONTENT_DEFAULT.or(BackupFile.INCLUDE_WALLPAPER)
            else BACKUP_CONTENT_DEFAULT
        )
    }
    val startBackupResult =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BackupFile.MIME_TYPE)) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                BackupTaskViewModel().startBackup(
                    context,
                    name,
                    resultUri,
                    selectedContent
                ) { success ->
                    if (success
                        && !BackupFile.listLocalBackups(context)
                            .map { it.meta?.name }
                            .contains(name)
                    ) prefs.recentBackups.add(resultUri)
                    else { // TODO show a response that backup failed
                    }
                    openDialogCustom.value = false
                }
            }
        }

    var radius = 16.dp
    if (prefs.themeCornerRadiusOverride.onGetValue()) {
        radius = prefs.themeCornerRadius.onGetValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.backup_create_new),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.name),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
                item {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(textFieldFocusRequester),
                        value = name,
                        shape = RoundedCornerShape(radius),
                        colors = TextFieldDefaults.textFieldColors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                        onValueChange = { name = it },
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    )
                }
                item {
                    Text(
                        text = stringResource(id = R.string.backup_contents),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
                items(items = backupContentItems.toList()) { item ->
                    val isSelected by rememberSaveable(selectedContent) {
                        mutableStateOf(selectedContent and item.first == item.first)
                    }
                    MultiSelectionListItem(
                        text = stringResource(id = item.second),
                        isChecked = isSelected
                    ) {
                        selectedContent = if (it) selectedContent.or(item.first)
                        else selectedContent.xor(item.first)
                    }
                }
                item {
                    Text(
                        text = stringResource(id = R.string.backup_location),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
                items(items = backupLocationItems.toList()) {
                    val isSelected by rememberSaveable(selectedLocation) {
                        mutableStateOf(selectedLocation == it.first)
                    }
                    SingleSelectionListItem(
                        text = stringResource(id = it.second),
                        isSelected = isSelected
                    ) {
                        selectedLocation = it.first
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        when (selectedLocation) {
                            BACKUP_LOCATION_INTERNAL ->
                                BackupTaskViewModel().startBackup(
                                    context,
                                    name,
                                    uri,
                                    selectedContent
                                ) { success ->
                                    if (success
                                        && !BackupFile.listLocalBackups(context)
                                            .map { it.meta?.name }
                                            .contains(name)
                                    ) prefs.recentBackups.add(uri)
                                    else { // TODO show a response that backup failed
                                    }
                                    openDialogCustom.value = false
                                }
                            BACKUP_LOCATION_CUSTOM ->
                                startBackupResult.launch("$name.${BackupFile.EXTENSION}")
                        }
                    }
                )
            }
        }
    }
}

private fun BackupTaskViewModel.startBackup(
    context: Context,
    name: String,
    uri: Uri,
    contents: Int,
    onPostExecute: (Boolean) -> Unit
) {
    // TODO on progress pointer
    execute(
        onPreExecute = { },
        doInBackground = {
            BackupFile.create(
                context = context,
                name = name,
                location = uri,
                contents = contents
            )
        },
        onPostExecute = onPostExecute
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestoreBackupDialogUI(
    backupFile: BackupFile,
    openDialogCustom: MutableState<Boolean>
) {
    val context = LocalContext.current
    val prefs = Utilities.getOmegaPrefs(context)
    var selectedContent by remember { mutableStateOf(backupFile.meta?.contents) }

    var radius = 16.dp
    if (prefs.themeCornerRadiusOverride.onGetValue()) {
        radius = prefs.themeCornerRadius.onGetValue().dp
    }
    val cornerRadius by remember { mutableStateOf(radius) }

    Card(
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier.padding(8.dp),
        elevation = CardDefaults.elevatedCardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.restore_backup),
                style = MaterialTheme.typography.titleLarge
            )
            LazyColumn(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .weight(1f, false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.name),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = backupFile.meta?.name ?: "",
                        onValueChange = {},
                        shape = RoundedCornerShape(radius),
                        colors = TextFieldDefaults.textFieldColors(
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                        enabled = false,
                    )
                }
                item {
                    Text(
                        text = stringResource(id = R.string.backup_timestamp),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
                item {
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = backupFile.meta?.localizedTimestamp ?: "",
                        onValueChange = {},
                        shape = RoundedCornerShape(radius),
                        colors = TextFieldDefaults.textFieldColors(
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        singleLine = true,
                        enabled = false,
                    )
                }
                item {
                    Text(
                        text = stringResource(id = R.string.restore_contents),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1
                    )
                }
                items(items = backupContentItems.toList()) { item ->
                    val isSelected by rememberSaveable(selectedContent) {
                        mutableStateOf((selectedContent?.and(item.first) ?: 0) == item.first)
                    }
                    val isEnabled by rememberSaveable {
                        mutableStateOf((selectedContent?.and(item.first) ?: 0) == item.first)
                    }
                    MultiSelectionListItem(
                        text = stringResource(id = item.second),
                        isChecked = isSelected && isEnabled,
                        isEnabled = isEnabled,
                    ) {
                        selectedContent = if (it) selectedContent?.or(item.first)
                        else selectedContent?.xor(item.first)
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth()
            ) {
                DialogNegativeButton(
                    cornerRadius = cornerRadius,
                    onClick = { openDialogCustom.value = false }
                )
                Spacer(Modifier.weight(1f))
                DialogPositiveButton(
                    modifier = Modifier.padding(start = 16.dp),
                    cornerRadius = cornerRadius,
                    onClick = {
                        // TODO restore backup (using contents and location) and add it to recentBackups if still not
                        BackupTaskViewModel().startRestore(backupFile, selectedContent ?: 0) {
                            if (it > -1) {
                                if (it and BackupFile.INCLUDE_SETTINGS == 0) {
                                    prefs.blockingEdit {
                                        restoreSuccess = true
                                    }
                                }
                                Utilities.killLauncher()
                            } else {  // TODO show a response that restore failed
                            }
                            openDialogCustom.value = false
                        }
                    }
                )
            }
        }
    }
}

private fun BackupTaskViewModel.startRestore(
    backupFile: BackupFile,
    contents: Int,
    onPostExecute: (Int) -> Unit
) {
    execute(
        onPreExecute = { },
        doInBackground = {
            if (backupFile.restore(contents)) contents else -1
        },
        onPostExecute = onPostExecute
    )
}
