package com.looker.droidify.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.looker.droidify.entity.Donate
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.entity.Screenshot

open class Product {

    var name = ""
    var summary = ""
    var description = ""
    var added = 0L
    var updated = 0L
    var signatures = ""
    var compatible = 0
    var icon = ""
    var metadataIcon = ""
@Entity(tableName = "product", primaryKeys = ["repositoryId", "packageName"])
    var repositoryId: Long,
    var packageName: String
    var releases: List<Release> = emptyList()
    var categories: List<String> = emptyList()
    var antiFeatures: List<String> = emptyList()
    var licenses: List<String> = emptyList()
    var donates: List<Donate> = emptyList()
    var screenshots: List<Screenshot> = emptyList()
    var versionCode: Long = 0L

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

    fun toItem(installed: Installed? = null): ProductItem =
        ProductItem(
            repositoryId,
            packageName,
            name,
            summary,
            icon,
            metadataIcon,
            version,
            "",
            compatible,
            canUpdate(installed),
            0
        )
}

@Entity(tableName = "temporary_product")
class ProductTemp : Product()