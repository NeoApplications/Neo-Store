package com.machiav3lli.fdroid.manager.network

import com.machiav3lli.fdroid.data.database.entity.RBData
import com.machiav3lli.fdroid.data.database.entity.RBLog

fun RBData.toLog(hash: String): RBLog = RBLog(
    hash = hash,
    repository = repository,
    apk_url = apk_url,
    appid = appid,
    version_code = version_code,
    version_name = version_name,
    tag = tag,
    commit = commit,
    timestamp = timestamp,
    reproducible = reproducible,
    error = error
)

fun Map<String, List<RBData>>.toLogs(): List<RBLog> {
    return this.flatMap { (hash, data) ->
        data.map { it.toLog(hash) }
    }
}