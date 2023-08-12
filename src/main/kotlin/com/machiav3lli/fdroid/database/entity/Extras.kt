package com.machiav3lli.fdroid.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.machiav3lli.fdroid.TABLE_EXTRAS
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = TABLE_EXTRAS)
@Serializable
data class Extras(
    @PrimaryKey
    var packageName: String = "",
    var favorite: Boolean = false,
    var ignoreUpdates: Boolean = false,
    var ignoredVersion: Long = 0L,
    @ColumnInfo(defaultValue = "0")
    var ignoreVulns: Boolean = false,
) {
    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Extras>(json)
    }
}