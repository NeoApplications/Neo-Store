package com.machiav3lli.fdroid.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.machiav3lli.fdroid.ROW_KEY
import com.machiav3lli.fdroid.TABLE_TRACKER
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(
    tableName = TABLE_TRACKER,
    indices = [
        Index(value = [ROW_KEY], unique = true)
    ]
)
data class Tracker(
    @PrimaryKey
    val key: Int = 0,
    override val name: String = String(),
    override val network_signature: String = String(),
    override val code_signature: String = String(),
    override val creation_date: String = String(),
    override val website: String = String(),
    override val description: String = String(),
    override val categories: List<String> = emptyList(),
    override val documentation: List<String> = emptyList(),
) : TrackerData(
    name,
    network_signature,
    code_signature,
    creation_date,
    website,
    description,
    categories,
    documentation,
)

@Serializable
open class TrackerData(
    open val name: String = String(),
    open val network_signature: String = String(),
    open val code_signature: String = String(),
    open val creation_date: String = String(),
    open val website: String = String(),
    open val description: String = String(),
    open val categories: List<String> = emptyList(),
    open val documentation: List<String> = emptyList(),
)

@Serializable
data class Trackers(
    val trackers: Map<String, TrackerData> = emptyMap(),
) {
    fun toJSON() = Json.encodeToString(this)

    companion object {
        private val jsonConfig = Json { ignoreUnknownKeys = true }
        fun fromJson(json: String) = jsonConfig.decodeFromString<Trackers>(json)
    }
}