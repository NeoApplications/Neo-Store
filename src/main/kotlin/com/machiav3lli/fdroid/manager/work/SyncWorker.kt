package com.machiav3lli.fdroid.manager.work

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.machiav3lli.fdroid.ARG_CHANGED
import com.machiav3lli.fdroid.ARG_EXCEPTION
import com.machiav3lli.fdroid.ARG_READ
import com.machiav3lli.fdroid.ARG_REPOSITORY_ID
import com.machiav3lli.fdroid.ARG_REPOSITORY_NAME
import com.machiav3lli.fdroid.ARG_STAGE
import com.machiav3lli.fdroid.ARG_STATE
import com.machiav3lli.fdroid.ARG_SUCCESS
import com.machiav3lli.fdroid.ARG_SYNC_REQUEST
import com.machiav3lli.fdroid.ARG_TOTAL
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.EXODUS_TRACKERS_SYNC
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.TAG_SYNC_ONETIME
import com.machiav3lli.fdroid.TAG_SYNC_PERIODIC
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.data.entity.SyncState
import com.machiav3lli.fdroid.data.entity.SyncTask
import com.machiav3lli.fdroid.data.index.RepositoryUpdater
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.syncNotificationBuilder
import com.machiav3lli.fdroid.utils.updateSyncProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.get
import kotlin.math.roundToInt

// TODO Add support for local repositories e.g. Calyx local repository
class SyncWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams), KoinComponent {
    private var repoId = inputData.getLong(ARG_REPOSITORY_ID, -1L)
    private var request = SyncRequest.entries[
        inputData.getInt(ARG_SYNC_REQUEST, 0)
    ]
    private var repoName = inputData.getString(ARG_REPOSITORY_NAME) ?: ""
    private lateinit var task: SyncTask
    private val langContext = ContextWrapperX.wrap(applicationContext)
    private val reposRepo: RepositoriesRepository by inject()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        task = SyncTask(repoId, request, repoName)

        return@withContext if (repoId != -1L) {
            handleSync(task)
        } else Result.success(
            getWorkData(succeeded = true)
        )
    }

    private suspend fun CoroutineScope.handleSync(task: SyncTask): Result {
        val repository = reposRepo.load(task.repoId)

        Log.i(this::class.java.simpleName, "sync repository: ${task.repoId}")
        if (repository != null && repository.enabled && task.repoId != EXODUS_TRACKERS_SYNC) {
            launch {
                setForegroundAsync(
                    createForegroundInfo(
                        state = SyncState.Enum.CONNECTING,
                    )
                )
            }
            val unstable = Preferences[Preferences.Key.UpdateUnstable]
            var lastPerCent = -1
            var lastStage: RepositoryUpdater.Stage? = null

            try {
                val changed = future {
                    RepositoryUpdater.update(
                        context,
                        repository,
                        unstable
                    ) { stage, progress, total ->
                        val perCent = if (total != null) (100f * progress / total).roundToInt()
                        else (progress / 100_000).toInt()

                        if (stage != lastStage || perCent != lastPerCent) this@handleSync.runCatching {
                            lastPerCent = perCent
                            lastStage = stage
                            setForegroundAsync(
                                createForegroundInfo(
                                    state = SyncState.Enum.SYNCING,
                                    progress = Progress(
                                        stage,
                                        progress,
                                        total ?: -1L,
                                    )
                                )
                            )
                        }
                    }
                }.join()
                return Result.success(
                    getWorkData(
                        state = SyncState.Enum.FINISHING,
                        succeeded = true,
                        message = "Repository updated",
                        changed = changed,
                    )
                )
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                return Result.failure(
                    getWorkData(
                        state = SyncState.Enum.FAILED,
                        succeeded = false,
                        message = throwable.message ?: "",
                    )
                )
            }
        } else {
            return Result.success(
                getWorkData(
                    state = SyncState.Enum.FINISHING,
                    succeeded = true,
                    changed = false,
                )
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val title = langContext.getString(
            R.string.syncing_FORMAT,
            repoName
        )
        return ForegroundInfo(
            NOTIFICATION_ID_SYNCING,
            langContext.syncNotificationBuilder(title).build(),
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    // changes based on https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/long-running
    private fun createForegroundInfo(
        state: SyncState.Enum,
        progress: Progress? = null,
    ): ForegroundInfo {
        val title = langContext.getString(
            R.string.syncing_FORMAT,
            repoName
        )
        val cancel = langContext.getString(R.string.cancel)
        // TODO consider if it's needed (ActionReceiver-intent already built-in!)
        val cancelIntent = get<WorkManager>(WorkManager::class.java)
            .createCancelPendingIntent(id)

        val notification = langContext.syncNotificationBuilder(title)
            .apply {
                if (state == SyncState.Enum.CONNECTING)
                    setContentText(langContext.getString(R.string.connecting))
                        .setProgress(0, 0, true)
                else if (progress != null)
                    updateSyncProgress(langContext, progress)
            }
            .addAction(R.drawable.ic_cancel, cancel, cancelIntent) // TODO reconsider
            .build()

        return ForegroundInfo(
            task.key.hashCode(),
            notification,
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    private fun setProgressData(data: Data) {
        setProgressAsync(
            Data.Builder()
                .putAll(data)
                .putLong(ARG_REPOSITORY_ID, repoId)
                .putInt(ARG_SYNC_REQUEST, request.ordinal)
                .putString(ARG_REPOSITORY_NAME, repoName)
                .build()
        )
    }

    private fun getWorkData(
        state: SyncState.Enum? = null,
        succeeded: Boolean = false,
        progress: Progress? = null,
        message: String = "",
        changed: Boolean = false,
    ): Data = if (state == null) workDataOf(
        ARG_REPOSITORY_ID to repoId,
        ARG_SYNC_REQUEST to request.ordinal,
        ARG_REPOSITORY_NAME to repoName,
        ARG_SUCCESS to succeeded,
    )
    else if (progress != null) workDataOf(
        ARG_REPOSITORY_ID to repoId,
        ARG_SYNC_REQUEST to request.ordinal,
        ARG_REPOSITORY_NAME to repoName,
        ARG_STATE to state.ordinal,
        ARG_STAGE to progress.stage.ordinal,
        ARG_READ to progress.read,
        ARG_TOTAL to progress.total,
        ARG_SUCCESS to succeeded,
        ARG_EXCEPTION to message,
        ARG_CHANGED to changed,
    )
    else workDataOf(
        ARG_REPOSITORY_ID to repoId,
        ARG_SYNC_REQUEST to request.ordinal,
        ARG_REPOSITORY_NAME to repoName,
        ARG_STATE to state.ordinal,
        ARG_SUCCESS to succeeded,
        ARG_EXCEPTION to message,
        ARG_CHANGED to changed,
    )

    data class Progress(
        val stage: RepositoryUpdater.Stage,
        val read: Long,
        val total: Long,
    ) {
        val percentage: Int
            get() = (100f * read / total).roundToInt()
    }

    companion object {
        fun Request(
            repoId: Long,
            repoName: String,
            request: SyncRequest,
        ): OneTimeWorkRequest {
            val builder = OneTimeWorkRequest.Builder(SyncWorker::class.java)

            builder
                .addTag(if (request == SyncRequest.AUTO) TAG_SYNC_PERIODIC else TAG_SYNC_ONETIME)
                .setInputData(
                    workDataOf(
                        ARG_REPOSITORY_ID to repoId,
                        ARG_SYNC_REQUEST to request.ordinal,
                        ARG_REPOSITORY_NAME to repoName,
                    )
                )

            if (request == SyncRequest.FORCE)
                builder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)

            return builder.build()
        }

        private fun enqueueManual(vararg repos: Pair<Long, String>) {
            repos.map { (repoId, repoName) ->

                if (repoId != EXODUS_TRACKERS_SYNC) {
                    val data = workDataOf(
                        ARG_REPOSITORY_ID to repoId,
                        ARG_SYNC_REQUEST to SyncRequest.MANUAL.ordinal,
                        ARG_REPOSITORY_NAME to repoName,
                    )

                    NeoApp.wm.enqueueUniqueWork(
                        "sync_$repoId",
                        ExistingWorkPolicy.KEEP,
                        OneTimeWorkRequestBuilder<SyncWorker>()
                            .setInputData(data)
                            .addTag(TAG_SYNC_ONETIME)
                            .build()
                    )
                } else ExodusWorker.fetchTrackers()
            }
        }

        suspend fun enableRepo(repository: Repository, enabled: Boolean): Boolean =
            withContext(Dispatchers.IO) {
                val reposRepo = get<RepositoriesRepository>(RepositoriesRepository::class.java)
                reposRepo.upsert(repository.enable(enabled))
                val isEnabled = !repository.enabled && repository.lastModified.isEmpty()
                val cooldownedSync = System.currentTimeMillis() -
                        NeoApp.latestSyncs.getOrDefault(repository.id, 0L) >=
                        10_000L
                if (enabled && isEnabled && cooldownedSync) {
                    NeoApp.latestSyncs[repository.id] = System.currentTimeMillis()
                    enqueueManual(Pair(repository.id, repository.name))
                } else {
                    NeoApp.wm.cancelSync(repository.id)
                    NeoApp.db.cleanUp(Pair(repository.id, false))
                }
                true
            }

        suspend fun deleteRepo(repoId: Long): Boolean = withContext(Dispatchers.IO) {
            val reposRepo = get<RepositoriesRepository>(RepositoriesRepository::class.java)
            val repository = reposRepo.load(repoId)
            repository != null && run {
                enableRepo(repository, false)
                reposRepo.deleteById(repoId)
                true
            }
        }

        fun getTask(data: Data) = SyncTask(
            data.getLong(ARG_REPOSITORY_ID, -1L),
            SyncRequest.entries[
                data.getInt(ARG_SYNC_REQUEST, 0)
            ],
            data.getString(ARG_REPOSITORY_NAME) ?: "",
        )

        fun getState(data: Data): SyncState = when (data.getInt(ARG_STATE, 0)) {
            SyncState.Enum.FAILED.ordinal    -> SyncState.Failed(
                data.getLong(ARG_REPOSITORY_ID, -1L),
                SyncRequest.entries[
                    data.getInt(ARG_SYNC_REQUEST, 0)
                ],
                data.getString(ARG_REPOSITORY_NAME) ?: "",
            )

            SyncState.Enum.FINISHING.ordinal -> SyncState.Finishing(
                data.getLong(ARG_REPOSITORY_ID, -1L),
                SyncRequest.entries[
                    data.getInt(ARG_SYNC_REQUEST, 0)
                ],
                data.getString(ARG_REPOSITORY_NAME) ?: "",
            )

            SyncState.Enum.SYNCING.ordinal   -> SyncState.Syncing(
                data.getLong(ARG_REPOSITORY_ID, -1L),
                SyncRequest.entries[
                    data.getInt(ARG_SYNC_REQUEST, 0)
                ],
                data.getString(ARG_REPOSITORY_NAME) ?: "",
                getProgress(data),
            )

            // SyncState.Enum.CONNECTING.ordinal
            else                             -> SyncState.Connecting(
                data.getLong(ARG_REPOSITORY_ID, -1L),
                SyncRequest.entries[
                    data.getInt(ARG_SYNC_REQUEST, 0)
                ],
                data.getString(ARG_REPOSITORY_NAME) ?: "",
            )
        }

        private fun getProgress(data: Data) = Progress(
            RepositoryUpdater.Stage.entries[
                data.getInt(ARG_STAGE, 0)
            ],
            data.getLong(ARG_READ, 0L),
            data.getLong(ARG_TOTAL, -1L),
        )
    }
}