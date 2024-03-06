package com.machiav3lli.fdroid.database.entity

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
class InstallTask() : RealmObject {
    var packageName: String = ""
    var repositoryId: Long = -1
    var versionCode: Long = -1
    var versionName: String = ""

    @PrimaryKey
    var key: String? = "$packageName-$repositoryId-$versionName"

    var label: String = ""
    var cacheFileName: String = ""
    var added: Long = 0L
    var requireUser: Boolean = false

    constructor(
        packageName: String = "",
        repositoryId: Long = -1,
        versionCode: Long = -1,
        versionName: String = "",
    ) : this() {
        this.packageName = packageName
        this.repositoryId = repositoryId
        this.versionCode = versionCode
        this.versionName = versionName
        this.key = "$packageName-$repositoryId-$versionName"
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<InstallTask>(json)
    }
}