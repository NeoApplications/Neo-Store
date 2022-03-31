package com.looker.droidify.entity

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class ProductPreference(val ignoreUpdates: Boolean, val ignoreVersionCode: Long) {
    fun shouldIgnoreUpdate(versionCode: Long): Boolean {
        return ignoreUpdates || ignoreVersionCode == versionCode
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<ProductPreference>(json)
    }
}
