package com.machiav3lli.fdroid.utility.extension

import android.content.pm.PackageInfo

val PackageInfo.grantedPermissions: Map<String, Boolean>
    get() = requestedPermissions?.mapIndexed { index, perm ->
        Pair(
            perm,
            requestedPermissionsFlags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED == PackageInfo.REQUESTED_PERMISSION_GRANTED
        )
    }?.toMap().orEmpty()