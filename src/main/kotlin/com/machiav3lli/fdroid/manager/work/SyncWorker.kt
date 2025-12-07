package com.machiav3lli.fdroid.manager.work

import com.machiav3lli.fdroid.DOWNLOAD_STATS_SYNC
import com.machiav3lli.fdroid.EXODUS_TRACKERS_SYNC
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.RB_LOGS_SYNC
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import org.koin.java.KoinJavaComponent.get

object SyncWorker {
    fun enqueueManual(vararg repos: Pair<Long, String>) {
        repos.forEach { (repoId, _) ->

            when (repoId) {
                EXODUS_TRACKERS_SYNC -> ExodusWorker.fetchTrackers()
                RB_LOGS_SYNC         -> RBWorker.fetchRBLogs()
                DOWNLOAD_STATS_SYNC  -> DownloadStatsWorker.enqueuePeriodic()
                else                 -> {
                    BatchSyncWorker.enqueue(
                        request = SyncRequest.MANUAL,
                        repositoryIds = setOf(repoId),
                    )
                }
            }
        }
    }

    suspend fun enableRepo(repository: Repository, enabled: Boolean): Boolean {
        val reposRepo = get<RepositoriesRepository>(RepositoriesRepository::class.java)
        reposRepo.upsert(repository.enable(enabled))
        val isEnabled = !repository.enabled && repository.lastModified.isEmpty()
        val cooldownedSync = System.currentTimeMillis() -
                NeoApp.latestSyncs.getOrDefault(repository.id, 0L) >=
                10_000L
        if (enabled && isEnabled && cooldownedSync) {
            NeoApp.latestSyncs[repository.id] = System.currentTimeMillis()
            BatchSyncWorker.enqueue(SyncRequest.MANUAL, setOf(repository.id))
        } else {
            NeoApp.wm.cancelSync(repository.id)
            NeoApp.db.cleanUp(Pair(repository.id, false))
        }
        return true
    }

    suspend fun deleteRepo(repoId: Long): Boolean {
        val reposRepo = get<RepositoriesRepository>(RepositoriesRepository::class.java)
        val repository = reposRepo.load(repoId)
        return repository != null && run {
            enableRepo(repository, false)
            reposRepo.deleteById(repoId)
            true
        }
    }
}
