package com.machiav3lli.fdroid.data.entity

import com.machiav3lli.fdroid.data.database.entity.Downloaded
import com.machiav3lli.fdroid.data.database.entity.ProductIconDetails

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

sealed class UpdateListItem {
    abstract val key: String
    abstract val packageName: String

    data class UpdateItem(
        val product: ProductItem,
        val download: Downloaded? = null
    ) : UpdateListItem() {
        override val key: String = "${product.packageName}-${product.repositoryId}"
        override val packageName: String = product.packageName
    }

    data class DownloadOnlyItem(
        val download: Downloaded,
        val iconDetails: ProductIconDetails?,
    ) : UpdateListItem() {
        override val key: String = download.itemKey
        override val packageName: String = download.packageName
    }
}
