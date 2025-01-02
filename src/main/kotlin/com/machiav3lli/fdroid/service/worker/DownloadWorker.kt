package com.machiav3lli.fdroid.service.worker

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
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.anggrayudi.storage.file.children
import com.anggrayudi.storage.file.toDocumentFile
import com.google.common.util.concurrent.ListenableFuture
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
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.network.DownloadSizeException
import com.machiav3lli.fdroid.network.Downloader
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.copyTo
import com.machiav3lli.fdroid.utility.downloadNotificationBuilder
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.android.singleSignature
import com.machiav3lli.fdroid.utility.extension.android.versionCodeCompat
import com.machiav3lli.fdroid.utility.extension.text.hex
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.getDownloadFolder
import com.machiav3lli.fdroid.utility.isDownloadExternal
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import kotlin.math.roundToInt

class DownloadWorker(
    val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    private val packageManager: PackageManager by lazy { context.packageManager }
    private lateinit var task: DownloadTask

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            task = getTask(inputData)

            if (Cache.getReleaseFile(applicationContext, task.release.cacheFileName).exists()) {
                Log.i(this::javaClass.name, "Running publish success from fun enqueue")
                finalize(task)
                return@withContext Result.success(getWorkData(task, null))
            }

            val result = handleDownload(task)
            result
        } catch (e: Exception) {
            Log.i(this::javaClass.name, e.message ?: "download failed")
            val result = Result.failure() // TODO (workDataOf(ARG_ERROR_MESSAGE to e.message))
            result
        }
    }

    private suspend fun handleDownload(task: DownloadTask): Result = coroutineScope {
        val partialRelease =
            Cache.getPartialReleaseFile(applicationContext, task.release.cacheFileName)

        val callback: suspend (read: Long, total: Long?, downloadID: Long) -> Unit =
            { read, total, downloadID ->
                setProgress(
                    workDataOf(
                        ARG_PROGRESS to if (total != null) (100f * read / total).roundToInt() else -1,
                        ARG_READ to read,
                        ARG_TOTAL to (total ?: -1L),
                    )
                )

                if (isStopped && downloadID != -1L) {
                    ContextCompat.getSystemService(context, DownloadManager::class.java)
                        ?.remove(downloadID)
                }
            }

        try {
            val result = if (Preferences[Preferences.Key.DownloadManager]) {
                Downloader.dmDownload(context, task, partialRelease, callback)
            } else {
                Downloader.download(task.url, partialRelease, "", "", task.authentication, callback)
            }

            if (!result.success) {
                Log.i(this::javaClass.name, "Worker failure by error ${result.statusCode}")
                return@coroutineScope Result.failure(getWorkData(task, result))
            }

            val validationError = validatePackage(task, partialRelease)
            if (validationError == ValidationError.NONE) {
                val releaseFile =
                    Cache.getReleaseFile(applicationContext, task.release.cacheFileName)
                partialRelease.renameTo(releaseFile)
                Log.i(this::javaClass.name, "Worker success with result: $result")
                finalize(task)
                Result.success(getWorkData(task, result))
            } else {
                partialRelease.delete()
                Log.i(this::javaClass.name, "Worker failure by validation error: $validationError")
                Result.failure(getWorkData(task, result, validationError))
            }
        } catch (e: DownloadSizeException) {
            Log.e(this::javaClass.name, "Download size error: ${e.message}")
            partialRelease.delete()
            Result.failure(
                getWorkData(
                    task,
                    Downloader.Result(HttpStatusCode.BadRequest, "", ""),
                    ValidationError.FILE_SIZE
                )
            )
        } catch (e: Exception) {
            Log.e(this::javaClass.name, "Download error: ${e.message}")
            Result.failure(
                getWorkData(
                    task,
                    Downloader.Result(HttpStatusCode.InternalServerError, "", ""),
                    ValidationError.UNKNOWN
                )
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        val stateNotificationBuilder = applicationContext.downloadNotificationBuilder()
        return ForegroundInfo(
            task.key.hashCode(),
            stateNotificationBuilder.build(),
            if (Android.sdk(Build.VERSION_CODES.Q)) ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            else 0
        )
    }

    override fun setProgressAsync(data: Data): ListenableFuture<Void> {
        return super.setProgressAsync(
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
        validationError: ValidationError? = null,
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
        ARG_VALIDATION_ERROR to validationError?.ordinal,
    )

    private suspend fun finalize(task: DownloadTask) {
        if (isDownloadExternal) {
            context.getDownloadFolder()?.let { downloadFolder ->
                val cacheFile = Cache.getReleaseFile(applicationContext, task.release.cacheFileName)
                    .toDocumentFile(applicationContext)
                if (downloadFolder.children.none { it.name == task.release.cacheFileName }) {
                    withContext(Dispatchers.IO) {
                        cacheFile?.copyTo(context, downloadFolder)
                    }
                }
            }
        }
    }

    private fun validatePackage(task: DownloadTask, file: File): ValidationError {
        val hashType = task.release.hashType.nullIfEmpty() ?: "SHA256"
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
                            PackageManager.GET_SIGNING_CERTIFICATES
                )
            }.getOrNull()?.run {
                if (packageName != task.packageName ||
                    versionCodeCompat != task.release.versionCode
                ) {
                    ValidationError.METADATA
                } else {
                    val signature = singleSignature?.let(Utils::calculateHash).orEmpty()
                    if ((signature.isEmpty() || signature != task.release.signature)
                        && !Preferences[Preferences.Key.DisableSignatureCheck]
                    ) {
                        ValidationError.SIGNATURE
                    } else {
                        val permissions =
                            permissions?.asSequence().orEmpty().map { it.name }.toSet()
                        if (!task.release.permissions.containsAll(permissions)) {
                            ValidationError.PERMISSIONS
                        } else {
                            null
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

    companion object {
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

            MainApplication.wm.workManager
                .beginUniqueWork(
                    "$packageName-${repository.id}-${release.version}",
                    ExistingWorkPolicy.REPLACE,
                    downloadRequest,
                )
                .enqueue()
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
