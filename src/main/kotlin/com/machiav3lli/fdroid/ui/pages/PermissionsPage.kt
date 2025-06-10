package com.machiav3lli.fdroid.ui.pages

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.entity.Permission
import com.machiav3lli.fdroid.ui.components.PermissionItem
import com.machiav3lli.fdroid.ui.navigation.NavRoute
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.isRunningOnTV
import com.machiav3lli.fdroid.utils.showBatteryOptimizationDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsPage(navigator: (NavRoute) -> Unit) {
    val context = LocalContext.current
    val activity = LocalActivity.current as NeoActivity
    val mScope = CoroutineScope(Dispatchers.Main)
    val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager

    val permissionStatePostNotifications = if (Android.sdk(Build.VERSION_CODES.TIRAMISU)) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    val ignored by remember { mutableIntStateOf(0) }
    val permissionsList = remember {
        mutableStateListOf<Pair<Permission, () -> Unit>>()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, key2 = ignored, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsList.refresh(
                    context,
                    powerManager,
                    permissionStatePostNotifications,
                ) {
                    mScope.launch { navigator(NavRoute.Main()) }
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(permissionsList, key = { it.first.nameId }) { pair ->
                PermissionItem(
                    modifier = Modifier.animateItem(),
                    item = pair.first,
                    onClick = pair.second
                ) {
                    permissionsList.refresh(
                        context,
                        powerManager,
                        permissionStatePostNotifications,
                    ) {
                        mScope.launch { navigator(NavRoute.Main()) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
fun SnapshotStateList<Pair<Permission, () -> Unit>>.refresh(
    context: Context,
    powerManager: PowerManager,
    permissionStatePostNotifications: PermissionState?,
    navigateToMain: () -> Unit,
) {
    clear()
    addAll(buildList {
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)
            && !Preferences[Preferences.Key.IgnoreDisableBatteryOptimization]
            && !context.isRunningOnTV
        ) add(Pair(Permission.BatteryOptimization) {
            context.showBatteryOptimizationDialog()
        })
        if (permissionStatePostNotifications?.status?.isGranted == false
            && !Preferences[Preferences.Key.IgnoreShowNotifications]
        ) add(Pair(Permission.PostNotifications) {
            permissionStatePostNotifications.launchPermissionRequest()
        })
        if (Android.sdk(Build.VERSION_CODES.O) && !context.packageManager.canRequestPackageInstalls() && context.checkSelfPermission(
                Manifest.permission.INSTALL_PACKAGES
            ).equals(PackageManager.PERMISSION_DENIED)
        )
            add(Pair(Permission.InstallPackages) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:" + context.packageName)
                )
                startActivityForResult(context as Activity, intent, 71662, null)
            })
    })
    if (isEmpty()) navigateToMain()
}