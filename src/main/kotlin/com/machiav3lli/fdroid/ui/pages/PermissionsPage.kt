package com.machiav3lli.fdroid.ui.pages

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.entity.Permission
import com.machiav3lli.fdroid.ui.compose.components.PermissionItem
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.showBatteryOptimizationDialog

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PermissionsPage(navigateToMain: () -> Unit) {
    val context = LocalContext.current
    val powerManager =
        MainApplication.mainActivity?.getSystemService(Context.POWER_SERVICE) as PowerManager

    val permissionStatePostNotifications = if (Android.sdk(Build.VERSION_CODES.TIRAMISU)) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null
    val permissionsList = remember {
        mutableStateListOf<Pair<Permission, () -> Unit>>()
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionsList.clear()
                permissionsList.addAll(buildList {
                    if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        && (!Preferences[Preferences.Key.IgnoreIgnoreBatteryOptimization]
                                || Android.sdk(Build.VERSION_CODES.S))
                    ) add(Pair(Permission.BatteryOptimization) {
                        context.showBatteryOptimizationDialog()
                    })
                    if (permissionStatePostNotifications?.status?.isGranted == false)
                        add(Pair(Permission.PostNotifications) {
                            permissionStatePostNotifications.launchPermissionRequest()
                        })
                })
                if (permissionsList.isEmpty()) navigateToMain()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    })

    Scaffold { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(permissionsList) {
                PermissionItem(it.first, it.second)
            }
        }
    }
}