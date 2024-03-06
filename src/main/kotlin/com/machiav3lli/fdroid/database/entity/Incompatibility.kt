package com.machiav3lli.fdroid.database.entity

import io.realm.kotlin.types.EmbeddedRealmObject
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
sealed class Incompatibility() : EmbeddedRealmObject {
    @Serializable
    data object MinSdk : Incompatibility()

    @Serializable
    data object MaxSdk : Incompatibility()

    @Serializable
    data object Platform : Incompatibility()

    @Serializable
    data class Feature(val feature: String) : Incompatibility()

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Incompatibility>(json)
    }
}