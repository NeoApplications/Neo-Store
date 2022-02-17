package com.looker.droidify.entity

data class ProductItem(
    var repositoryId: Long = 0,
    var packageName: String = "com.looker.droidify",
    var name: String = "Droid-ify",
    var summary: String = "A great F-Droid client",
    val icon: String = "",
    val metadataIcon: String = "",
    val version: String = "69",
    var installedVersion: String = "69",
    var compatible: Boolean = false,
    var canUpdate: Boolean = false,
    var matchRank: Int = 0,
)
