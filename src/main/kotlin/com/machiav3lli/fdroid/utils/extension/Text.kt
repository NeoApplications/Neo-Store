@file:Suppress("PackageDirectoryMismatch")

package com.machiav3lli.fdroid.utils.extension.text

import android.util.Log
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.text.DateFormat
import java.util.Date
import java.util.Locale


val RE_jumpChars = Regex("""[\t]""")
val RE_finishChars = Regex("""[\n]""")

fun <T : CharSequence> T.nullIfEmpty(): T? {
    return if (isNullOrEmpty()) null else this
}

private val sizeFormats = listOf("%.0f B", "%.0f kB", "%.1f MB", "%.2f GB")

fun Long.formatSize(): String {
    val (size, index) = generateSequence(Pair(this.toFloat(), 0)) { (size, index) ->
        if (size >= 1024f)
            Pair(size / 1024f, index + 1) else null
    }.take(sizeFormats.size).last()
    return sizeFormats[index].format(Locale.US, size)
}

fun Long.formatDateTime(): String {
    val nowDate = DateFormat.getDateInstance().format(Date(System.currentTimeMillis()))
    val mDate = DateFormat.getDateInstance().format(Date(this))
    return if (nowDate == mDate) DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(this))
    else mDate
}

fun getIsoDateOfMonthsAgo(months: Int): String = Clock.System.now()
    .toLocalDateTime(TimeZone.currentSystemDefault()).date
    .minus(DatePeriod(months = months, days = 1)).toString()

val String.pathCropped: String
    get() {
        val index = indexOfLast { it != '/' }
        return if (index >= 0 && index < length - 1) substring(0, index + 1) else this
    }

fun String?.trimAfter(char: Char, repeated: Int): String? {
    var count = 0
    this?.let {
        for (i in it.indices) {
            if (it[i] == char) count++
            if (repeated == count) return it.substring(0, i)
        }
    }
    return this
}

fun String?.trimBefore(char: Char, repeated: Int): String? {
    var count = 0
    this?.let {
        for (i in it.indices) {
            if (it[i] == char) count++
            if (repeated == count) return it.substring(i + 1)
        }
    }
    return null
}

fun String.isoDateToInt(): Int {
    val parts = split("-")
    return when (parts.size) {
        // YYYY-MM-DD
        3    -> (parts[0] + parts[1] + parts[2]).toInt()
        // YYYY-MM
        2    -> (parts[0] + parts[1] + "00").toInt()
        // YYYY
        1    -> (parts[0] + "0000").toInt()
        else -> throw IllegalArgumentException("Invalid date format")
    }
}

fun Int.intToIsoDate(): String {
    val s = toString()
    return when (s.length) {
        // YYYYMMDD -> YYYY-MM-DD
        8    -> "${s.substring(0, 4)}-${s.substring(4, 6)}-${s.substring(6, 8)}"
        // YYYYMM00 -> YYYY-MM-00
        6    -> "${s.substring(0, 4)}-${s.substring(4, 6)}-00"
        // YYYY0000 -> YYYY-00-00
        4    -> "$s-00-00"
        else -> throw IllegalArgumentException("Invalid int date format")
    }
}

fun Char.halfByte(): Int {
    return when (this) {
        in '0'..'9' -> this - '0'
        in 'a'..'f' -> this - 'a' + 10
        in 'A'..'F' -> this - 'A' + 10
        else        -> -1
    }
}

fun CharSequence.unhex(): ByteArray? {
    return if (length % 2 == 0) {
        val ints = windowed(2, 2, false).map {
            val high = it[0].halfByte()
            val low = it[1].halfByte()
            if (high >= 0 && low >= 0) {
                (high shl 4) or low
            } else {
                -1
            }
        }
        if (ints.any { it < 0 }) null else ints.map { it.toByte() }.toByteArray()
    } else {
        null
    }
}

fun ByteArray.hex(): String {
    val builder = StringBuilder()
    for (byte in this) {
        builder.append("%02x".format(Locale.US, byte.toInt() and 0xff))
    }
    return builder.toString()
}

fun Any.debug(message: String) {
    val tag = this::class.java.name.let {
        val index = it.lastIndexOf('.')
        if (index >= 0) it.substring(index + 1) else it
    }.replace('$', '.')
    Log.d(tag, message)
}
