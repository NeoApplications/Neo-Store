package com.machiav3lli.fdroid.database.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class Extras() : RealmObject {
    @PrimaryKey
    var packageName: String = ""
    var favorite: Boolean = false
    var ignoreUpdates: Boolean = false
    var ignoredVersion: Long = 0L
    var ignoreVulns: Boolean = false
    var allowUnstable: Boolean = false

    constructor(packageName: String = "") : this() {
        this.packageName = packageName
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Extras>(json)
    }
}