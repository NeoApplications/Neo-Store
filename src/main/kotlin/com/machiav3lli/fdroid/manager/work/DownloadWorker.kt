package com.machiav3lli.fdroid.manager.work

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.anggrayudi.storage.file.children
import com.anggrayudi.storage.file.toDocumentFile
import com.machiav3lli.fdroid.ARG_AUTHENTICATION
import com.machiav3lli.fdroid.ARG_NAME
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.ARG_PROGRESS
import com.machiav3lli.fdroid.ARG_READ
import com.machiav3lli.fdroid.ARG_RELEASE
import com.machiav3lli.fdroid.ARG_REPOSITORY_ID
import com.machiav3lli.fdroid.ARG_RESULT_CODE
import com.machiav3lli.fdroid.ARG_STARTED
import com.machiav3lli.fdroid.ARG_TOTAL
import com.machiav3lli.fdroid.ARG_URL
import com.machiav3lli.fdroid.ARG_VALIDATION_ERROR
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.DownloadState
import com.machiav3lli.fdroid.data.entity.DownloadTask
import com.machiav3lli.fdroid.data.entity.ValidationError
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.manager.network.DownloadSizeException
import com.machiav3lli.fdroid.manager.network.Downloader
import com.machiav3lli.fdroid.utils.copyTo
import com.machiav3lli.fdroid.utils.downloadNotificationBuilder
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.android.signerSHA256Signatures
import com.machiav3lli.fdroid.utils.extension.android.versionCodeCompat
import com.machiav3lli.fdroid.utils.extension.text.formatSize
import com.machiav3lli.fdroid.utils.extension.text.hex
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.getDownloadFolder
import com.machiav3lli.fdroid.utils.isDownloadExternal
import com.machiav3lli.fdroid.utils.notifySensitivePermissionsChanged
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.java.KoinJavaComponent.get
import java.io.File
import java.security.MessageDigest
import kotlin.math.roundToInt

class DownloadWorker(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val packageManager: PackageManager by lazy { context.packageManager }
    private lateinit var task: DownloadTask
    private val langContext = ContextWrapperX.wrap(applicationContext)
    private val downloadedRepo: DownloadedRepository by inject()

    @Deprecated("")
    override val coroutineContext: CoroutineDispatcher
        get() = Dispatchers.IO

    override suspend fun doWork(): Result {
        try {
            task = getTask(inputData)

            if (Cache.getReleaseFile(applicationContext, task.release.cacheFileName).exists()) {
                Log.i(TAG, "Running publish success from fun enqueue")
                finalize(task)
                return Result.success(getWorkData(task, null))
            }

            return handleDownload(task)
        } catch (e: Exception) {
            Log.i(TAG, e.message ?: "download failed")
            return Result.failure() // TODO (workDataOf(ARG_ERROR_MESSAGE to e.message))
        }
    }

    private suspend fun handleDownload(task: DownloadTask): Result {
        val partialRelease =
            Cache.getPartialReleaseFile(applicationContext, task.release.cacheFileName)
        val downloaded = Downloaded(
            packageName = task.packageName,
            version = task.release.version,
            repositoryId = task.repoId,
            cacheFileName = task.release.cacheFileName,
            changed = System.currentTimeMillis(),
            state = DownloadState.Downloading(
                packageName = task.packageName,
                name = task.name,
                version = task.release.version,
                cacheFileName = task.release.cacheFileName,
                repoId = task.repoId,
                read = 0L,
                total = 100L,
            ),
        )

        var lastPerMille = -1

        val callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit =
            { read, total, downloadID ->
                val perMille = if (total != null) (1000f * read / total).roundToInt() else -1
                val percent = if (total != null) (100f * read / total).roundToInt() else -1

                if (perMille != lastPerMille || total == null) {
                    lastPerMille = perMille

                    setForegroundAsync(
                        createForegroundInfo(
                            "${read.formatSize()} / ${total?.formatSize()}",
                            percent
                        )
                    )

                    downloadedRepo.update(
                        downloaded.copy(
                            state = DownloadState.Downloading(
                                packageName = task.packageName,
                                name = task.name,
                                version = task.release.version,
                                cacheFileName = task.release.cacheFileName,
                                repoId = task.repoId,
                                read = read,
                                total = total?.takeIf { it > 0 },
                            )
                        )
                    )

                    if (isStopped && downloadID != -1L) {
                        ContextCompat.getSystemService(context, DownloadManager::class.java)
                            ?.remove(downloadID)
                    }
                }
            }

        try {
            val result = if (Preferences[Preferences.Key.DownloadManager]) {
                Downloader.dmDownload(context, task, partialRelease, callback)
            } else {
                Downloader.download(
                    url = task.url,
                    target = partialRelease,
                    lastModified = "",
                    entityTag = "",
                    authentication = task.authentication,
                    callback = callback
                )
            }

            if (!result.success) {
                Log.i(TAG, "Worker failure by error ${result.statusCode}")
                return Result.failure(
                    getWorkData(
                        task,
                        result,
                        ValidationError.CONNECTION
                    )
                )
            }

            val validationError = validatePackage(task, partialRelease)
            return if (validationError == ValidationError.NONE) {
                val releaseFile =
                    Cache.getReleaseFile(applicationContext, task.release.cacheFileName)
                partialRelease.renameTo(releaseFile)
                Log.i(TAG, "Worker success with result: $result")
                finalize(task)
                Result.success(getWorkData(task, result))
            } else {
                partialRelease.delete()
                Log.i(TAG, "Worker failure by validation error: $validationError")
                Result.failure(getWorkData(task, result, validationError))
            }
        } catch (e: DownloadSizeException) {
            Log.e(TAG, "Download size error: ${e.message}", e)
            partialRelease.delete()
            return Result.failure(
                getWorkData(
                    task,
                    Downloader.Result(HttpStatusCode.BadRequest, "", ""),
                    ValidationError.FILE_SIZE
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Download error: ${e.message}", e)
            return Result.failure(
                getWorkData(
                    task,
                    Downloader.Result(HttpStatusCode.InternalServerError, "", ""),
                    ValidationError.UNKNOWN
                )
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val title = langContext.getString(
            R.string.downloading_FORMAT,
            "${task.name} (${task.release.version})"
        )
        val pending = langContext.getString(R.string.pending)
        return ForegroundInfo(
            task.key.hashCode(),
            langContext.downloadNotificationBuilder(title, pending).build(),
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    // changes based on https://developer.android.com/develop/background-work/background-tasks/persistent/how-to/long-running
    private fun createForegroundInfo(progress: String, percent: Int = -1): ForegroundInfo {
        val title = langContext.getString(
            R.string.downloading_FORMAT,
            "${task.name} (${task.release.version})"
        )
        val cancel = langContext.getString(R.string.cancel)
        // TODO consider ActionReceiver-intent instead
        val cancelIntent = get<WorkManager>(WorkManager::class.java)
            .createCancelPendingIntent(id)

        val notification = langContext.downloadNotificationBuilder(title, progress, percent)
            .addAction(R.drawable.ic_cancel, cancel, cancelIntent)
            .build()

        return ForegroundInfo(
            task.key.hashCode(),
            notification,
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    fun setProgressData(data: Data) {
        setProgressAsync(
            Data.Builder()
                .putAll(data)
                .putLong(ARG_STARTED, task.started)
                .putString(ARG_PACKAGE_NAME, task.packageName)
                .putString(ARG_NAME, task.name)
                .putString(ARG_RELEASE, task.release.toJSON())
                .putString(ARG_URL, task.url)
                .putLong(ARG_REPOSITORY_ID, task.repoId)
                .putString(ARG_AUTHENTICATION, task.authentication)
                .build()
        )
    }

    private fun getWorkData(
        task: DownloadTask,
        result: Downloader.Result? = null,
        validationError: ValidationError = ValidationError.NONE,
    ): Data = if (result == null)
        workDataOf(
            ARG_STARTED to task.started,
            ARG_PACKAGE_NAME to task.packageName,
            ARG_NAME to task.name,
            ARG_RELEASE to task.release.toJSON(),
            ARG_URL to task.url,
            ARG_REPOSITORY_ID to task.repoId,
            ARG_AUTHENTICATION to task.authentication,
        )
    else workDataOf(
        ARG_STARTED to task.started,
        ARG_PACKAGE_NAME to task.packageName,
        ARG_NAME to task.name,
        ARG_RELEASE to task.release.toJSON(),
        ARG_URL to task.url,
        ARG_REPOSITORY_ID to task.repoId,
        ARG_AUTHENTICATION to task.authentication,
        ARG_RESULT_CODE to result.statusCode.value,
        ARG_VALIDATION_ERROR to validationError.ordinal,
    )

    private suspend fun finalize(task: DownloadTask) {
        if (isDownloadExternal) {
            context.getDownloadFolder()?.let { downloadFolder ->
                val cacheFile = Cache.getReleaseFile(applicationContext, task.release.cacheFileName)
                    .toDocumentFile(applicationContext)
                if (downloadFolder.children.none { it.name == task.release.cacheFileName }) {
                    cacheFile?.copyTo(context, downloadFolder)
                }
            }
        }
    }

    private fun validatePackage(task: DownloadTask, file: File): ValidationError {
        val hashType = task.release.hashType.nullIfEmpty() ?: "SHA-256"
        val hash = try {
            MessageDigest.getInstance(hashType).let { md ->
                file.inputStream().use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    generateSequence { input.read(buffer) }.takeWhile { it != -1 }
                        .forEach { md.update(buffer, 0, it) }
                }
                md.digest().hex()
            }
        } catch (e: Exception) {
            ""
        }
        return when {
            hash.isEmpty() || hash != task.release.hash -> ValidationError.INTEGRITY
            else                                        -> runCatching {
                packageManager.getPackageArchiveInfo(
                    file.path,
                    PackageManager.GET_ACTIVITIES or
                            PackageManager.GET_SERVICES or
                            PackageManager.GET_PROVIDERS or
                            PackageManager.GET_RECEIVERS or
                            PackageManager.GET_INSTRUMENTATION or
                            PackageManager.GET_SIGNATURES or
                            PackageManager.GET_SIGNING_CERTIFICATES or
                            PackageManager.GET_PERMISSIONS
                )
            }.getOrNull()?.run {
                if (packageName != task.packageName ||
                    versionCodeCompat != task.release.versionCode
                ) {
                    ValidationError.METADATA
                } else {
                    val signatures = signerSHA256Signatures
                    if ((signatures.isEmpty() || task.release.signature !in signatures)
                        && !Preferences[Preferences.Key.DisableSignatureCheck]
                    ) {
                        Log.e(
                            TAG,
                            "Signature check failed\nDownloaded package signatures: $signatures\nExpected signature: ${task.release.signature}"
                        )
                        ValidationError.SIGNATURE
                    } else {
                        val permissions = permissions
                            ?.asSequence()
                            .orEmpty()
                            .map { it.name }
                            .filter { it.startsWith("android.permission.") }
                            .toSet()
                        when {
                            Preferences[Preferences.Key.DisablePermissionsCheck] -> null

                            task.release.permissions.containsAll(permissions)    -> {
                                val oldPermissions = try {
                                    val installedInfo = packageManager.getPackageInfo(
                                        task.packageName,
                                        PackageManager.GET_PERMISSIONS
                                    )
                                    installedInfo.requestedPermissions
                                        ?.toSet()
                                        .orEmpty()
                                        .toSet()
                                } catch (e: PackageManager.NameNotFoundException) {
                                    return@run null
                                }

                                val addedPermissions =
                                    task.release.permissions.minus(oldPermissions)
                                val addedSensitivePermissions =
                                    addedPermissions.intersect(SENSITIVE_PERMISSIONS)

                                if (addedSensitivePermissions.isNotEmpty()) {
                                    Log.i(
                                        TAG,
                                        "New sensitive permissions detected: $addedSensitivePermissions"
                                    )
                                    langContext.notifySensitivePermissionsChanged(
                                        task.packageName,
                                        task.name,
                                        addedSensitivePermissions
                                    )
                                    // TODO consider blocking install till confirmed by user
                                    //  return ValidationError.SENSITIVE_PERMISSION
                                }

                                null
                            }

                            else                                                 -> {
                                ValidationError.PERMISSIONS
                            }
                        }
                    }
                }
            } ?: ValidationError.NONE
        }
    }

    data class Progress(
        val progress: Int = 0,
        val read: Long = -1L,
        val total: Long = -1L,
    )

    // TODO fun onSuccess(data: Data) : Result {}
    // TODO fun onFailure(data: Data) : Result {}

    companion object {
        private const val TAG = "DownloadWorker"
        val SENSITIVE_PERMISSIONS = setOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.BODY_SENSORS,
        )

        fun enqueue(
            packageName: String,
            label: String,
            repository: Repository,
            release: Release,
        ) {
            val data = workDataOf(
                ARG_STARTED to System.currentTimeMillis(),
                ARG_PACKAGE_NAME to packageName,
                ARG_NAME to label,
                ARG_RELEASE to release.toJSON(),
                ARG_URL to release.getDownloadUrl(repository),
                ARG_REPOSITORY_ID to repository.id,
                ARG_AUTHENTICATION to repository.authentication,
            )
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(data)
                .setConstraints(constraints)
                .addTag("download_$packageName")
                .build()

            NeoApp.wm.enqueueUniqueWork(
                "$packageName-${repository.id}-${release.version}",
                ExistingWorkPolicy.REPLACE,
                downloadRequest,
            )
        }

        fun getTask(data: Data) = DownloadTask(
            data.getLong(ARG_STARTED, System.currentTimeMillis()),
            data.getString(ARG_PACKAGE_NAME) ?: "",
            data.getString(ARG_NAME) ?: "",
            Release.fromJson(data.getString(ARG_RELEASE) ?: ""),
            data.getString(ARG_URL) ?: "",
            data.getLong(ARG_REPOSITORY_ID, -1L),
            data.getString(ARG_AUTHENTICATION) ?: "",
        )

        fun getProgress(data: Data) = Progress(
            data.getInt(ARG_PROGRESS, -1),
            data.getLong(ARG_READ, 0L),
            data.getLong(ARG_TOTAL, -1L),
        )
    }
}
