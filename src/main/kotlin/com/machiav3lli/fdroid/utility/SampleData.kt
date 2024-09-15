package com.machiav3lli.fdroid.utility

import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository

object SampleData {
    val demoRelease = Release(
        packageName = "com.machiav3lli.fdroid",
        repositoryId = 1,
        selected = false,
        version = "v0.2.3",
        versionCode = 1234,
        added = 12345,
        size = 12345,
        maxSdkVersion = 32,
        minSdkVersion = 29,
        targetSdkVersion = 32,
        source = "",
        release = "",
        hash = "",
        hashType = "",
        signature = "",
        obbPatchHashType = "",
        obbPatchHash = "",
        obbPatch = "",
        obbMainHashType = "",
        obbMainHash = "",
        obbMain = "",
        permissions = listOf(),
        features = listOf(),
        platforms = listOf(),
        incompatibilities = listOf()
    )
    val demoRepository = Repository(
        0,
        "https://f-droid.org/repo",
        emptyList(),
        "F-Droid",
        "The official F-Droid Free Software repository. " +
                "Everything in this repository is always built from the source code.",
        21,
        true,
        "43238D512C1E5EB2D6569F4A3AFBF5523418B82E0A3ED1552770ABB9A9C9CCAB",
        "",
        "",
        0L,
        0L,
        ""
    )
}