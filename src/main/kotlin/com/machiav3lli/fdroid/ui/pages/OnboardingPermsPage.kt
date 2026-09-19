package com.machiav3lli.fdroid.ui.pages

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.ColoringState
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.data.entity.Permission
import com.machiav3lli.fdroid.ui.components.ActionButton
import com.machiav3lli.fdroid.ui.components.PermissionItem
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowCircleRight
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.needsBatteryOptimizationWhitelist
import com.machiav3lli.fdroid.utils.needsInstallPackagesPermission
import com.machiav3lli.fdroid.utils.needsNotificationPermission

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun OnboardingPermsPage(onComplete: () -> Unit) {
    val context = LocalContext.current
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey> = remember {
        mutableStateOf(DialogKey.None)
    }

    val permissionStatePostNotifications = if (Android.sdk(Build.VERSION_CODES.TIRAMISU)) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    val ignored by remember { mutableIntStateOf(0) }
    val permissionsList = remember {
        mutableStateMapOf<Permission, () -> Unit>()
    }

    fun SnapshotStateMap<Permission, () -> Unit>.refresh() {
        apply {
            if (context.needsBatteryOptimizationWhitelist()) {
                this.putIfAbsent(Permission.BatteryOptimization) {
                    dialogKey.value = DialogKey.PermissionBatteryOptimization
                    openDialog.value = true
                }
            } else remove(Permission.BatteryOptimization)
            if (context.needsNotificationPermission()) {
                putIfAbsent(Permission.PostNotifications) {
                    permissionStatePostNotifications?.launchPermissionRequest()
                }
            } else remove(Permission.PostNotifications)
            if (context.needsInstallPackagesPermission()) {
                putIfAbsent(Permission.InstallPackages) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        "package:${context.packageName}".toUri()
                    )
                    startActivityForResult(context as Activity, intent, 71662, null)
                }
            } else remove(Permission.InstallPackages)
        }

        if (permissionsList.isEmpty()) onComplete()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, key2 = ignored, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsList.refresh()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(permissionsList.toList(), key = { it.first.nameId }) { pair ->
                PermissionItem(
                    modifier = Modifier.animateItem(),
                    item = pair.first,
                    onClick = pair.second
                ) {
                    permissionsList.refresh()
                }
            }
            if (permissionsList.isEmpty()) {
                item {
                    Text(
                        text = stringResource(id = R.string.no_permissions_identified),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (permissionsList.isEmpty()) {
            ActionButton(
                onClick = onComplete,
                text = stringResource(id = R.string.next),
                icon = Phosphor.ArrowCircleRight,
                coloring = ColoringState.Positive,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
            )
        }
    }

    if (openDialog.value) {
        BaseDialog(openDialogCustom = openDialog) {
            when (dialogKey.value) {
                is DialogKey.PermissionBatteryOptimization -> KeyDialogUI(
                    key = dialogKey.value,
                    openDialog = openDialog,
                    primaryAction = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = ("package:" + context.packageName).toUri()
                        try {
                            context.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                R.string.ignore_battery_optimization_not_supported,
                                Toast.LENGTH_LONG
                            ).show()
                            Preferences[Preferences.Key.IgnoreDisableBatteryOptimization] = true
                        }
                    },
                    onDismiss = {
                        dialogKey.value = DialogKey.None
                        openDialog.value = false
                    }
                )

                else                                       -> {}
            }

        }
    }
}