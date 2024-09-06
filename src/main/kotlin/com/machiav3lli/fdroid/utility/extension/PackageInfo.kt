package com.machiav3lli.fdroid.utility.extension

import android.content.pm.PackageInfo

val PackageInfo.grantedPermissions: Map<String, Boolean>
    get() = requestedPermissions?.mapIndexed { index, perm ->
        Pair(
            perm,
            (requestedPermissionsFlags?.get(index)
                ?: 0) and PackageInfo.REQUESTED_PERMISSION_GRANTED
                    == PackageInfo.REQUESTED_PERMISSION_GRANTED
        )
    }?.toMap().orEmpty()