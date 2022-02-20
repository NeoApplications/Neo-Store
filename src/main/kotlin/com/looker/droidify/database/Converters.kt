package com.looker.droidify.database

import androidx.room.TypeConverter
import com.looker.droidify.database.entity.Release
import com.looker.droidify.database.entity.Release.Companion.deserializeIncompatibilities
import com.looker.droidify.entity.Donate
import com.looker.droidify.entity.Screenshot
import com.looker.droidify.utility.extension.json.writeDictionary
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

    @JvmName("releasesToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(releases: List<Release>) =
        jsonGenerate { releases.forEach { item -> item.serialize(it) }.toString().toByteArray() }

    @TypeConverter
    @JvmStatic
    fun toIncompatibilities(byteArray: ByteArray) =
        byteArray.jsonParse { it.deserializeIncompatibilities() }

    @JvmName("incompatibilitiesToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(list: List<Release.Incompatibility>) =
        jsonGenerate { generator ->
            list.forEach {
                generator.writeDictionary {
                    when (it) {
                        is Release.Incompatibility.MinSdk -> {
                            writeStringField("type", "minSdk")
                        }
                        is Release.Incompatibility.MaxSdk -> {
                            writeStringField("type", "maxSdk")
                        }
                        is Release.Incompatibility.Platform -> {
                            writeStringField("type", "platform")
                        }
                        is Release.Incompatibility.Feature -> {
                            writeStringField("type", "feature")
                            writeStringField("feature", it.feature)
                        }
                    }::class
                }
            }
        }

    @TypeConverter
    @JvmStatic
    fun toScreenshots(byteArray: ByteArray): List<Screenshot> {
        val string = byteArray.toString()
        return if (string == "") emptyList()
        else string.split(",").mapNotNull { byteArray.jsonParse { Screenshot.deserialize(it) } }
    }

    @JvmName("screenshotsToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(screenshots: List<Screenshot>) = jsonGenerate {
        screenshots.forEach { item -> item.serialize(it) }.toString().toByteArray()
    }

    @TypeConverter
    @JvmStatic
    fun toDonates(byteArray: ByteArray): List<Donate> {
        val string = byteArray.toString()
        return if (string == "") emptyList()
        else string.split(",").mapNotNull { byteArray.jsonParse { Donate.deserialize(it) } }
    }

    @JvmName("donatesToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(donates: List<Donate>) = jsonGenerate {
        donates.forEach { item -> item.serialize(it) }.toString().toByteArray()
    }
}