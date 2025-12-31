package com.machiav3lli.fdroid.data.entity

data class ProductItem(
    val repositoryId: Long = 0,
    val packageName: String = "com.machaiv3lli.fdroid",
    val name: String = "Droid-ify",
    val developer: String = "",
    val summary: String = "A great F-Droid client",
    val icon: String = "",
    val metadataIcon: String = "",
    val version: String = "69",
    val installedVersion: String = "",
    val compatible: Boolean = false,
    val canUpdate: Boolean = false,
    val launchable: Boolean = false,
    val matchRank: Int = 0,
)
