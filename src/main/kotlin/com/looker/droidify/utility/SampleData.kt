package com.looker.droidify.utility

import com.looker.droidify.database.entity.Release

object SampleData {
    val demoRelease = Release(
        packageName = "com.looker.droidify",
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
}