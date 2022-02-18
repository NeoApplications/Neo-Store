package com.looker.droidify.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.looker.droidify.entity.ProductItem

@Entity(tableName = "product", primaryKeys = ["repository_id", "package_name"])
open class Product {
    var repository_id = 0L
    var package_name = ""

    var name = ""
    var summary = ""
    var description = ""
    var added = 0L
    var updated = 0L
    var version_code = 0L
    var signatures = ""
    var compatible = 0
    var icon = ""
    var metadataIcon = ""
    var releases: List<Release> = emptyList()
    var categories: List<String> = emptyList()

    // TODO Remove in next iteration
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var data: com.looker.droidify.entity.Product? = null

    val trueData: com.looker.droidify.entity.Product?
        get() = data?.copy(repositoryId = repository_id)

    val selectedReleases: List<Release>
        get() = releases.filter { it.selected }

    val displayRelease: Release?
        get() = selectedReleases.firstOrNull() ?: releases.firstOrNull()

    val version: String
        get() = displayRelease?.version.orEmpty()

    val versionCode: Long
        get() = selectedReleases.firstOrNull()?.versionCode ?: 0L

    val item: ProductItem
        get() = ProductItem(
            repository_id,
            package_name,
            name,
            summary,
            icon,
            metadataIcon,
            version,
            "",
            compatible != 0,
            false,
            0
        )
}

@Entity(tableName = "temporary_product")
class ProductTemp : Product()