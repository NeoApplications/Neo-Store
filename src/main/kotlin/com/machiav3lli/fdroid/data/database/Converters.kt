package com.machiav3lli.fdroid.data.database

import androidx.room.TypeConverter
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Donate
import com.machiav3lli.fdroid.data.entity.DownloadState

object Converters {
    @TypeConverter
    @JvmStatic
    fun toIntList(byteArray: ByteArray): List<Int> {
        val string = String(byteArray)
        return if (string == "") emptyList()
        else string.removeSurrounding("[", "]")
            .split(", ")
            .filter(String::isNotEmpty)
            .map { it.toInt() }
    }

    @JvmName("intListToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(list: List<Int>): ByteArray = list.toString().toByteArray()

    @TypeConverter
    @JvmStatic
    fun toStringList(byteArray: ByteArray): List<String> {
        val string = String(byteArray)
        return if (string == "") emptyList()
        else string.removeSurrounding("[", "]").split(", ").filter(String::isNotEmpty)
    }

    @JvmName("stringListToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(list: List<String>): ByteArray = list.toString().toByteArray()

    @TypeConverter
    @JvmStatic
    fun toPairStringList(byteArray: ByteArray): List<Pair<String, String>> {
        val string = String(byteArray)
        return if (string == "") emptyList()
        else string.removeSurrounding("[", "]").split(",").filter(String::isNotEmpty).mapNotNull {
            val pairs = it.split("|")
            if (pairs.size == 2) Pair(pairs[0], pairs[1])
            else null
        }
    }

    @JvmName("pairStringListToByteArray")
    @TypeConverter
    @JvmStatic
    fun toByteArray(list: List<Pair<String, String>>): ByteArray =
        list.map { it.toList().joinToString("|") }.toString().toByteArray()

    @TypeConverter
    @JvmStatic
    fun toAuthor(byteArray: ByteArray) = Author.fromJson(String(byteArray))

    @TypeConverter
    @JvmStatic
    fun toByteArray(author: Author) = author.toJSON().toByteArray()

    @TypeConverter
    @JvmStatic
    fun toDownloadState(byteArray: ByteArray) = DownloadState.fromJson(String(byteArray))

    @TypeConverter
    @JvmStatic
    fun toByteArray(state: DownloadState) = state.toJSON().toByteArray()

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