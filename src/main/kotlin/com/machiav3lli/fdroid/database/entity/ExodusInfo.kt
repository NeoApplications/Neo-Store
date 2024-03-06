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

class ExodusInfo() : RealmObject {
    @PrimaryKey
    var packageName: String = ""

    var handle: String = String()
    var app_name: String = String()
    var uaid: String = String()
    var version_name: String = String()
    var version_code: String = String()
    var source: String = String()
    var icon_hash: String = String()
    var apk_hash: String = String()
    var created: String = String()
    var updated: String = String()
    var report: Int = 0
    var creator: String = String()
    var downloads: String = String()
    @Serializable(RealmListKSerializer::class)
    var trackers: RealmList<Int> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var permissions: RealmList<String> = realmListOf()

    constructor(
        packageName: String = "",
        handle: String = String(),
        appName: String = String(),
        uaid: String = String(),
        versionName: String = String(),
        versionCode: String = String(),
        source: String = String(),
        iconHash: String = String(),
        apkHash: String = String(),
        created: String = String(),
        updated: String = String(),
        report: Int = 0,
        creator: String = String(),
        downloads: String = String(),
        trackers: RealmList<Int> = realmListOf(),
        permissions: RealmList<String> = realmListOf(),
    ) : this() {
        this.packageName = packageName
        this.handle = handle
        this.app_name = appName
        this.uaid = uaid
        this.version_name = versionName
        this.version_code = versionCode
        this.source = source
        this.icon_hash = iconHash
        this.apk_hash = apkHash
        this.created = created
        this.updated = updated
        this.report = report
        this.creator = creator
        this.downloads = downloads
        this.trackers = trackers
        this.permissions = permissions
    }
}

@Serializable
open class ExodusData(
    open val handle: String = String(),
    open val app_name: String = String(),
    open val uaid: String = String(),
    open val version_name: String = String(),
    open val version_code: String = String(),
    open val source: String = String(),
    open val icon_hash: String = String(),
    open val apk_hash: String = String(),
    open val created: String = String(),
    open val updated: String = String(),
    open val report: Int = 0,
    open val creator: String = String(),
    open val downloads: String = String(),
    open val trackers: List<Int> = emptyList(),
    open val permissions: List<String> = emptyList(),
) {
    fun toExodusInfo(packageName: String) = ExodusInfo(
        packageName, handle, app_name, uaid, version_name, version_code, source,
        icon_hash, apk_hash, created, updated, report, creator, downloads,
        trackers.toRealmList(), permissions.toRealmList()
    )

    fun toJSON() = Json.encodeToString(this)

    companion object {
        private val jsonConfig = Json { ignoreUnknownKeys = true }
        fun fromJson(json: String) = jsonConfig.decodeFromString<ExodusData>(json)
        fun listFromJson(json: String) = jsonConfig.decodeFromString<List<ExodusData>>(json)
    }
}
