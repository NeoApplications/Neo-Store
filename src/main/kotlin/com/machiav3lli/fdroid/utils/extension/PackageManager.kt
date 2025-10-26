package com.machiav3lli.fdroid.utils.extension

import android.content.pm.PackageManager
import android.os.Build
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.utils.extension.android.Android

fun PackageManager.isNSPackageUpdateOwner(packageName: String): Boolean = when {
    packageName == BuildConfig.APPLICATION_ID
         -> BuildConfig.APPLICATION_ID

    Android.sdk(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
         -> getInstallSourceInfo(packageName).let {
        it.updateOwnerPackageName ?: it.installingPackageName
    }

    Android.sdk(Build.VERSION_CODES.R)
         -> getInstallSourceInfo(packageName).installingPackageName

    else -> getInstallerPackageName(
        packageName
    )
} == BuildConfig.APPLICATION_ID

fun PackageManager.isInstalled(packageName: String): Boolean = runCatching {
    getApplicationInfo(packageName, 0) != null
}.getOrElse { false }