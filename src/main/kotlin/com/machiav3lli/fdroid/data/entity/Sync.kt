package com.machiav3lli.fdroid.data.entity

class SyncTask(
    val repoId: Long,
    val request: SyncRequest,
    val repoName: String,
) {
    val key: String
        get() = "$repoId-$repoName-${request.name}"
}

enum class SyncState { CONNECTING, SYNCING, FAILED, FINISHING }

enum class SyncRequest { AUTO, MANUAL, FORCE }
