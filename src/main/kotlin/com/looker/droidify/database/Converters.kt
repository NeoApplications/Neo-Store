package com.looker.droidify.database

import androidx.room.TypeConverter
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Release
import com.looker.droidify.entity.Author
import com.looker.droidify.entity.Donate
import com.looker.droidify.entity.Screenshot

object Converters {
    @TypeConverter
    @JvmStatic
    fun toStringList(byteArray: ByteArray): List<String> {
        val string = String(byteArray)
        return if (string == "") emptyList()
        else string.removeSurrounding("[", "]").split(",").filter(String::isNotEmpty)
    }

    @JvmName("stringListToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(list: List<String>): ByteArray = list.toString().toByteArray()

    @TypeConverter
    @JvmStatic
    fun toAuthor(byteArray: ByteArray) = Author.fromJson(String(byteArray))

    @TypeConverter
    @JvmStatic
    fun toByteArray(author: Author) = author.toJSON().toByteArray()

    @TypeConverter
    @JvmStatic
    fun toReleases(byteArray: ByteArray): List<Release> =
        if (String(byteArray) == "") emptyList()
        else String(byteArray).split("|").map { Release.fromJson(it) }

    @JvmName("releasesToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(releases: List<Release>) =
        if (releases.isNotEmpty()) releases.joinToString("|") { it.toJSON() }.toByteArray()
        else "".toByteArray()

    @TypeConverter
    @JvmStatic
    fun toIncompatibilities(byteArray: ByteArray): List<Release.Incompatibility> =
        if (String(byteArray) == "") emptyList()
        else String(byteArray).split("|").map { Release.Incompatibility.fromJson(it) }

    @JvmName("incompatibilitiesToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(incompatibilities: List<Release.Incompatibility>) =
        if (incompatibilities.isNotEmpty())
            incompatibilities.joinToString("|") { it.toJSON() }.toByteArray()
        else "".toByteArray()

    @TypeConverter
    @JvmStatic
    fun toScreenshots(byteArray: ByteArray): List<Screenshot> =
        if (String(byteArray) == "") emptyList()
        else String(byteArray).split("|").map { Screenshot.fromJson(it) }

    @JvmName("screenshotsToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(screenshots: List<Screenshot>) =
        if (screenshots.isNotEmpty()) screenshots.joinToString("|") { it.toJSON() }.toByteArray()
        else "".toByteArray()

    @TypeConverter
    @JvmStatic
    fun toDonates(byteArray: ByteArray): List<Donate> =
        if (String(byteArray) == "") emptyList()
        else String(byteArray).split("|").map { Donate.fromJson(it) }

    @JvmName("donatesToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(donates: List<Donate>) =
        if (donates.isNotEmpty()) donates.joinToString("|") { it.toJSON() }.toByteArray()
        else "".toByteArray()
}