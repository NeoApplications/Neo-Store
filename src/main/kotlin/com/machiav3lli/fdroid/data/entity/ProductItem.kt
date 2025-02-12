package com.machiav3lli.fdroid.data.entity

data class ProductItem(
    var repositoryId: Long = 0,
    var packageName: String = "com.machaiv3lli.fdroid",
    var name: String = "Droid-ify",
    val developer: String = "",
    var summary: String = "A great F-Droid client",
    val icon: String = "",
    val metadataIcon: String = "",
    val version: String = "69",
    var installedVersion: String = "",
    var compatible: Boolean = false,
    var canUpdate: Boolean = false,
    var launchable: Boolean = false,
    var matchRank: Int = 0,
)
