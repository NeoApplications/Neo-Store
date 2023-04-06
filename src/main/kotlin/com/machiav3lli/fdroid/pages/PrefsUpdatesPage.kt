package com.machiav3lli.fdroid.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroup
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.EnumSelectionPrefDialogUI
import com.machiav3lli.fdroid.ui.dialog.IntInputPrefDialogUI
import com.machiav3lli.fdroid.ui.dialog.StringInputPrefDialogUI

@Composable
fun PrefsUpdatesPage() {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Preferences.Key<*>?>(null) }
    val onPrefDialog = { pref: Preferences.Key<*> ->
        dialogPref = pref
        openDialog.value = true
    }
    val syncPrefs = listOf(
        Preferences.Key.AutoSync,
        Preferences.Key.AutoSyncInterval,
    )
    val updatesPrefs = listOf(
        Preferences.Key.InstallAfterSync,
        Preferences.Key.UpdateNotify,
        Preferences.Key.KeepInstallNotification,
        Preferences.Key.UpdateUnstable,
        Preferences.Key.IncompatibleVersions,
        Preferences.Key.DisableDownloadVersionCheck,
        Preferences.Key.DisableSignatureCheck,
    )
    val installPrefs = listOf(
        Preferences.Key.Installer,
        Preferences.Key.RootSessionInstaller,
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.prefs_sync),
                    keys = syncPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.updates),
                    keys = updatesPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.install_types),
                    keys = installPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (openDialog.value) {
            BaseDialog(openDialogCustom = openDialog) {
                when (dialogPref?.default?.value) {
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
}