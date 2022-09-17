package com.machiav3lli.fdroid.ui.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.machiav3lli.fdroid.ui.compose.components.prefs.PreferenceGroup
import com.machiav3lli.fdroid.ui.dialog.BaseDialog

@OptIn(ExperimentalMaterial3Api::class)
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
        Preferences.Key.UpdateUnstable,
        Preferences.Key.IncompatibleVersions,
    )
    val installPrefs = listOf(
        Preferences.Key.RootPermission,
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
        }

        if (openDialog.value) {
            BaseDialog(openDialogCustom = openDialog) {
                when (dialogPref) {
                    // TODO
                    else -> {}
                }
            }
        }
    }
}