package com.machiav3lli.fdroid.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DOWNLOADING
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.android.notificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO maybe replace by using WorkManager instead?
class DownloadService : ConnectionService<DownloadService.Binder>() {
    private val scope = CoroutineScope(Dispatchers.Default)

    @Serializable
    sealed class State {
        abstract val packageName: String
        abstract val name: String
        abstract val version: String
        abstract val cacheFileName: String
        abstract val repoId: Long

        @Serializable
        class Pending(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        @Serializable
        class Connecting(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        @Serializable
        class Downloading(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
            val read: Long,
            val total: Long?,
        ) : State()

        @Serializable
        class Success(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
            val release: Release,
        ) : State()

        @Serializable
        class Error(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        @Serializable
        class Cancel(
            override val packageName: String,
            override val name: String,
            override val version: String,
            override val cacheFileName: String,
            override val repoId: Long,
        ) : State()

        fun toJSON() = Json.encodeToString(this)

        companion object {
            fun fromJson(json: String) = Json.decodeFromString<State>(json)
        }
    }

    private val mutableStateSubject = MutableSharedFlow<State>()

    class Task(
        val packageName: String, val name: String, val release: Release,
        val url: String, val repoId: Long, val authentication: String,
    )

    private data class CurrentTask(
        val task: Task, val job: kotlinx.coroutines.Job,
        val lastState: State,
    )

    private val tasks = mutableListOf<Task>()
    private var currentTask: CurrentTask? = null

    inner class Binder : android.os.Binder() {
        val stateSubject = mutableStateSubject.asSharedFlow()
            .distinctUntilChanged { old, new ->
                new.packageName == old.packageName &&
                        old::class.java == new::class.java && (
                        if (old is State.Downloading && new is State.Downloading && new.total != null)
                            new.read - old.read <= new.total / 100
                        else true
                        )
            }
    }

    override fun onBind(intent: Intent): Binder = Binder()

    override fun onCreate() {
        super.onCreate()

        if (Android.sdk(26)) {
            NotificationChannel(
                NOTIFICATION_CHANNEL_DOWNLOADING,
                getString(R.string.downloading), NotificationManager.IMPORTANCE_LOW
            )
                .apply { setShowBadge(false) }
                .let(notificationManager::createNotificationChannel)
        }

        mutableStateSubject.onEach { (application as MainApplication).db.downloadedDao.insertReplace() }
            .launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()

        scope.cancel()
        cancelTasks(null)
        cancelCurrentTask(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    private fun cancelTasks(packageName: String?) {
        tasks.removeAll { task ->
            (packageName == null || task.packageName == packageName) && run {
                scope.launch {
                    mutableStateSubject.emit(
                        State.Cancel(
                            task.packageName,
                            task.name,
                            task.release.version,
                            task.release.cacheFileName,
                            task.repoId,
                        )
                    )
                }
                true
            }
        }
    }

    private fun cancelCurrentTask(packageName: String?) {
        currentTask?.let { current ->
            if (packageName == null || current.task.packageName == packageName) {
                currentTask = null
                scope.launch {
                    mutableStateSubject.emit(
                        State.Cancel(
                            current.task.packageName,
                            current.task.name,
                            current.task.release.version,
                            current.task.release.cacheFileName,
                            current.task.repoId,
                        )
                    )
                }
                current.job.cancel()
            }
        }
    }
}
