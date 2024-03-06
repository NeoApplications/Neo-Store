package com.machiav3lli.fdroid.database.entity

import com.machiav3lli.fdroid.entity.LauncherActivity
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.serializers.RealmListKSerializer
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable


class Installed() : RealmObject {
    @PrimaryKey
    var packageName: String = ""
    var version: String = ""
    var versionCode: Long = 0L
    var signature: String = ""
    var isSystem: Boolean = false
    @Serializable(RealmListKSerializer::class)
    var launcherActivities: RealmList<LauncherActivity> = realmListOf()

    constructor(
        packageName: String = "",
        version: String = "",
        versionCode: Long = 0L,
        signature: String = "",
        isSystem: Boolean = false,
        launcherActivities: List<LauncherActivity> = emptyList(),
    ) : this() {
        this.packageName = packageName
        this.version = version
        this.versionCode = versionCode
        this.signature = signature
        this.isSystem = isSystem
        this.launcherActivities = launcherActivities.toRealmList()
    }
}