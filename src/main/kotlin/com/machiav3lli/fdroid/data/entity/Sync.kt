package com.machiav3lli.fdroid.data.entity

import com.machiav3lli.fdroid.manager.work.SyncWorker

class SyncTask(
    val repoId: Long,
    val request: SyncRequest,
    val repoName: String,
)

sealed class SyncState(
    val repoId: Long,
    val request: SyncRequest,
    val repoName: String,
) {
    class Connecting(
        repoId: Long,
        request: SyncRequest,
        repoName: String,
    ) : SyncState(repoId, request, repoName)

    class Failed(
        repoId: Long,
        request: SyncRequest,
        repoName: String,
    ) : SyncState(repoId, request, repoName)

    class Finishing(
        repoId: Long,
        request: SyncRequest,
        repoName: String,
    ) : SyncState(repoId, request, repoName)

    class Syncing(
        repoId: Long,
        request: SyncRequest,
        repoName: String,
        val progress: SyncWorker.Progress
    ) : SyncState(repoId, request, repoName)

    val isRunning: Boolean
        get() = this is Connecting || this is Syncing

    enum class Enum { CONNECTING, SYNCING, FAILED, FINISHING }
}

enum class SyncRequest { AUTO, MANUAL, FORCE }
