package com.machiav3lli.fdroid.ui.pages

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.PREFS_LANGUAGE_DEFAULT
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.ColoringState
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroup
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleRight
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.EnumSelectionPrefDialogUI
import com.machiav3lli.fdroid.ui.dialog.IntInputPrefDialogUI
import com.machiav3lli.fdroid.ui.dialog.LanguagePrefDialogUI
import com.machiav3lli.fdroid.ui.dialog.StringInputPrefDialogUI
import com.machiav3lli.fdroid.utils.DOWNLOAD_DIRECTORY_INTENT

@Composable
fun OnboardingPrefsPage(
    onNext: () -> Unit,
) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Preferences.Key<*>?>(null) }
    val onPrefDialog: (Preferences.Key<*>) -> Unit = { pref ->
        dialogPref = pref
        openDialog.value = true
    }
    val personalPrefs = listOf(
        Preferences.Key.Language,
        Preferences.Key.Theme,
        Preferences.Key.DefaultTab,
    )
    val layoutPrefs = listOf(
        Preferences.Key.BottomSearchBar,
        Preferences.Key.ShowScreenshots,
        Preferences.Key.ShowTrackers,
    )
    val cachePrefs = listOf(
        Preferences.Key.EnableDownloadDirectory,
        Preferences.Key.DownloadDirectory,
    )
    val syncPrefs = listOf(
        Preferences.Key.AutoSync,
        Preferences.Key.AutoSyncInterval,
        Preferences.Key.InstallAfterSync,
        Preferences.Key.UpdateNotify,
        Preferences.Key.UpdateUnstable,
    )

    val downloadDirectoryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let {
                val uri = it.data ?: return@let
                val oldDir = Preferences[Preferences.Key.DownloadDirectory]

                if (oldDir != uri.toString()) {
                    val flags = it.flags and (
                            Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                    Preferences[Preferences.Key.DownloadDirectory] = uri.toString()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            stickyHeader(key = "settingsTitle") {
                Text(
                    text = stringResource(id = R.string.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.prefs_personalization),
                    keys = personalPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.prefs_layout),
                    keys = layoutPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.prefs_cache),
                    keys = cachePrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.prefs_sync),
                    keys = syncPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
        }
        ActionButton(
            onClick = onNext,
            text = stringResource(id = R.string.next),
            icon = Phosphor.ArrowCircleRight,
            coloring = ColoringState.Positive,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
        )
    }

    if (openDialog.value) {
        if (dialogPref == Preferences.Key.DownloadDirectory) {
            downloadDirectoryLauncher.launch(DOWNLOAD_DIRECTORY_INTENT)
        } else BaseDialog(openDialogCustom = openDialog) {
            when (dialogPref?.default?.value) {
                PREFS_LANGUAGE_DEFAULT        -> LanguagePrefDialogUI(
                    openDialogCustom = openDialog
                )

                is String                     -> StringInputPrefDialogUI(
                    prefKey = dialogPref as Preferences.Key<String>,
                    openDialogCustom = openDialog
                )

                is Int                        -> IntInputPrefDialogUI(
                    prefKey = dialogPref as Preferences.Key<Int>,
                    openDialogCustom = openDialog
                )

                is Preferences.Enumeration<*> -> EnumSelectionPrefDialogUI(
                    prefKey = dialogPref as Preferences.Key<Preferences.Enumeration<*>>,
                    openDialogCustom = openDialog
                )

                else                          -> {}
            }
        }
    }
}
