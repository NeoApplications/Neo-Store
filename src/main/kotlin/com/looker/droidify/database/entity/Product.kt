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

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var data: com.looker.droidify.entity.Product? = null

    fun item(): ProductItem? = data?.item()
}

@Entity(tableName = "temporary_product")
class ProductTemp : Product()