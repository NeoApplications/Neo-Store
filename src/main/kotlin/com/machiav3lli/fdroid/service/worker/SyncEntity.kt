package com.machiav3lli.fdroid.service.worker

class SyncTask(val repositoryId: Long, val request: SyncRequest)

sealed class SyncState {
    data object Connecting : SyncState()
    data object Failed : SyncState()
    data object Finishing : SyncState()
    class Syncing(val progress: SyncWorker.Progress) : SyncState()

    val isRunning: Boolean
        get() = this is Connecting || this is Syncing

    enum class Enum { CONNECTING, SYNCING, FAILED, FINISHING }
}

enum class SyncRequest { AUTO, MANUAL, FORCE }
