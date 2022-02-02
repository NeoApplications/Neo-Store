package com.looker.droidify.database

import androidx.room.TypeConverter
import com.looker.droidify.entity.Release
import com.looker.droidify.utility.jsonGenerate
import com.looker.droidify.utility.jsonParse

object Converters {
    @TypeConverter
    @JvmStatic
    fun toStringList(byteArray: ByteArray): List<String> {
        val string = byteArray.toString()
        return if (string == "") emptyList()
        else string.split(",")
    }

    @TypeConverter
    @JvmStatic
    fun toString(list: List<String>): ByteArray = list.toString().toByteArray()

    @TypeConverter
    @JvmStatic
    fun toProduct(byteArray: ByteArray) =
        byteArray.jsonParse { com.looker.droidify.entity.Product.deserialize(it) }

    @TypeConverter
    @JvmStatic
    fun toByteArray(product: com.looker.droidify.entity.Product) = jsonGenerate(product::serialize)

    @TypeConverter
    @JvmStatic
    fun toReleases(byteArray: ByteArray): List<Release> {
        val string = byteArray.toString()
        return if (string == "") emptyList()
        else string.split(",").map { byteArray.jsonParse { Release.deserialize(it) } }
    }

    @TypeConverter
    @JvmStatic
    fun toByteArray(releases: List<Release>) =
        jsonGenerate { releases.forEach { item -> item.serialize(it) }.toString().toByteArray() }
}