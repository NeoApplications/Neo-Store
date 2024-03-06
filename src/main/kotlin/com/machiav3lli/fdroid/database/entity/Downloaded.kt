package com.machiav3lli.fdroid.database.entity

import com.machiav3lli.fdroid.service.worker.DownloadState
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Downloaded() : RealmObject {
    var packageName: String = ""
    var version: String = ""
    var cacheFileName: String = ""

    @PrimaryKey
    private var primaryKey: String = ""

    var changed: Long = 0L
    var state: DownloadState? = null

    init {
        primaryKey = "$packageName/$version/$cacheFileName"
    }

    constructor(
        packageName: String = "",
        version: String = "",
        cacheFileName: String = "",
    ) : this() {
        this.packageName = packageName
        this.version = version
        this.cacheFileName = cacheFileName
        primaryKey = "$packageName/$version/$cacheFileName"
    }
}