package com.machiav3lli.fdroid.database.entity

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.serializers.RealmListKSerializer
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Tracker(
) : RealmObject {
    @PrimaryKey
    var key: Int = 0
    var name: String = String()
    var network_signature: String = String()
    var code_signature: String = String()
    var creation_date: String = String()
    var website: String = String()
    var description: String = String()
    @Serializable(RealmListKSerializer::class)
    var categories: RealmList<String> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var documentation: RealmList<String> = realmListOf()

    constructor(
        key: Int,
        name: String,
        network_signature: String,
        code_signature: String,
        creation_date: String,
        website: String,
        description: String,
        categories: List<String>,
        documentation: List<String>
    ) : this() {
        this.key = key
        this.name = name
        this.network_signature = network_signature
        this.code_signature = code_signature
        this.creation_date = creation_date
        this.website = website
        this.description = description
        this.categories = categories.toRealmList()
        this.documentation = documentation.toRealmList()
    }
}

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