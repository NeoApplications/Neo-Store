package com.machiav3lli.fdroid.pages

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.content.SAFFile
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.entity.LinkRef
import com.machiav3lli.fdroid.ui.components.prefs.BasePreference
import com.machiav3lli.fdroid.ui.components.prefs.PreferenceGroup
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.EnumSelectionPrefDialogUI
import com.machiav3lli.fdroid.ui.dialog.IntInputPrefDialogUI
import com.machiav3lli.fdroid.ui.dialog.StringInputPrefDialogUI
import com.machiav3lli.fdroid.utility.currentTimestamp
import com.machiav3lli.fdroid.viewmodels.PrefsVM

@Composable
fun PrefsOtherPage(viewModel: PrefsVM) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    var dialogPref by remember { mutableStateOf<Preferences.Key<*>?>(null) }
    val onPrefDialog = { pref: Preferences.Key<*> ->
        dialogPref = pref
        openDialog.value = true
    }
    val proxyPrefs = listOf(
        Preferences.Key.ProxyType,
        Preferences.Key.ProxyHost,
        Preferences.Key.ProxyPort,
    )

    val startExportResult =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(SAFFile.EXTRAS_MIME_TYPE)) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                SAFFile.write(
                    context,
                    resultUri,
                    viewModel.extras.value
                        .joinToString(separator = ">") { it.toJSON() }
                )
                // TODO add notification about success or failure
            }
        }
    val startImportResult =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { resultUri ->
            if (resultUri != null) {
                context.contentResolver.takePersistableUriPermission(
                    resultUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                val content = SAFFile(context, resultUri).read()
                if (content != null) {
                    val extras = content
                        .split(">")
                        .map { Extras.fromJson(it) }
                        .toTypedArray()
                    viewModel.insertExtras(*extras)
                }
                // TODO add notification about success or failure
            }
        }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .blockBorder()
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                PreferenceGroup(
                    heading = stringResource(id = R.string.proxy),
                    keys = proxyPrefs,
                    onPrefDialog = onPrefDialog
                )
            }
            item {
                PreferenceGroup(heading = stringResource(id = R.string.tools)) {
                    BasePreference(
                        titleId = R.string.extras_export,
                        index = 0,
                        groupSize = 2,
                        onClick = {
                            startExportResult
                                .launch("NS_$currentTimestamp.${SAFFile.EXTRAS_EXTENSION}")
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    BasePreference(
                        titleId = R.string.extras_import,
                        index = 1,
                        groupSize = 2,
                        onClick = {
                            startImportResult.launch(SAFFile.EXTRAS_MIME_ARRAY)
                        }
                    )
                }
            }
            item {
                PreferenceGroup(
                    heading = "${stringResource(id = R.string.application_name)} ${BuildConfig.VERSION_NAME}",
                    links = LinkRef.values().toList(),
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        if (openDialog.value) {
            BaseDialog(openDialogCustom = openDialog) {
                when (dialogPref?.default?.value) {
                    is String -> StringInputPrefDialogUI(
                        prefKey = dialogPref as Preferences.Key<String>,
                        openDialogCustom = openDialog
                    )
                    is Int -> IntInputPrefDialogUI(
                        prefKey = dialogPref as Preferences.Key<Int>,
                        openDialogCustom = openDialog
                    )
                    is Preferences.Enumeration<*> -> EnumSelectionPrefDialogUI(
                        prefKey = dialogPref as Preferences.Key<Preferences.Enumeration<*>>,
                        openDialogCustom = openDialog
                    )
                    else -> {}
                }
            }
        }
    }
}
