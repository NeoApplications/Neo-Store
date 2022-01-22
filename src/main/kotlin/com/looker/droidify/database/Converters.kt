package com.looker.droidify.database

import androidx.room.TypeConverter
import com.looker.droidify.entity.ProductItem
import com.looker.droidify.entity.Repository
import com.looker.droidify.utility.jsonGenerate
import com.looker.droidify.utility.jsonParse

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