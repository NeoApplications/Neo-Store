package com.machiav3lli.fdroid.service.worker

class SyncTask(val repositoryId: Long, val request: SyncRequest)

enum class SyncState { CONNECTING, SYNCING, FAILED, FINISHING }

enum class SyncRequest { AUTO, MANUAL, FORCE }
