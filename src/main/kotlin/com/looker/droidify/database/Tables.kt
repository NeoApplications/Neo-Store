package com.looker.droidify.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.entity.Repository
import com.looker.droidify.utility.jsonGenerate
import com.looker.droidify.utility.jsonParse

@Entity
class Repository {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0

    var enabled = 0
    var deleted = false

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var data: Repository? = null

    class IdAndDeleted {
        @ColumnInfo(name = "_id")
        var id = 0L

        var deleted = false
    }
}

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

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var data_item: ProductItem? = null
}

@Entity(tableName = "product.temporary")
class ProductTemp : Product()

@Entity(tableName = "category", primaryKeys = ["repository_id", "package_name", "name"])
open class Category {
    var repository_id: Long = 0
    var package_name = ""
    var name = ""
}

@Entity(tableName = "category.temporary")
class CategoryTemp : Category()

@Entity(tableName = "memory.installed")
class Installed(pName: String = "") {
    @PrimaryKey
    var package_name = pName

    var version = ""
    var version_code = 0L
    var signature = ""
}

@Entity(tableName = "memory.lock")
class Lock {
    @PrimaryKey
    var package_name = ""

    var version_code = 0L
}

object Converters {
    @TypeConverter
    @JvmStatic
    fun toRepository(byteArray: ByteArray) = byteArray.jsonParse { Repository.deserialize(it) }

    @TypeConverter
    @JvmStatic
    fun toByteArray(repository: Repository) = jsonGenerate(repository::serialize)

    @TypeConverter
    @JvmStatic
    fun toProduct(byteArray: ByteArray) =
        byteArray.jsonParse { com.looker.droidify.entity.Product.deserialize(it) }

    @TypeConverter
    @JvmStatic
    fun toByteArray(product: com.looker.droidify.entity.Product) = jsonGenerate(product::serialize)

    @TypeConverter
    @JvmStatic
    fun toProductItem(byteArray: ByteArray) = byteArray.jsonParse { ProductItem.deserialize(it) }

    @TypeConverter
    @JvmStatic
    fun toByteArray(productItem: ProductItem) = jsonGenerate(productItem::serialize)
}