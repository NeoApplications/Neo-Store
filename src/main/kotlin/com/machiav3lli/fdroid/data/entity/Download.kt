package com.machiav3lli.fdroid.data.entity

import androidx.work.WorkInfo
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import com.machiav3lli.fdroid.data.database.entity.Release
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

@Serializable
class DownloadTask(
    val started: Long,
    val packageName: String,
    val name: String,
    val release: Release,
    val url: String,
    val repoId: Long,
    val authentication: String,
) {
    val key: String
        get() = "$packageName-$repoId-${release.version}"

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<DownloadTask>(json)
    }
}

@Serializable
sealed class DownloadState {
    abstract val packageName: String
    abstract val name: String
    abstract val version: String
    abstract val cacheFileName: String
    abstract val repoId: Long
    abstract val changed: Long

    @Serializable
    class Pending(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val blocked: Boolean,
    ) : DownloadState() {
        override val changed: Long = System.currentTimeMillis()
    }

    @Serializable
    class Connecting(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
    ) : DownloadState() {
        override val changed: Long = System.currentTimeMillis()
    }

    @Serializable
    class Downloading(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val read: Long,
        val total: Long?,
    ) : DownloadState() {
        override val changed: Long = System.currentTimeMillis()
        val progress: Int
            get() = if (total != null) (100f * read / total).roundToInt() else -1
    }

    @Serializable
    class Success(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val release: Release,
    ) : DownloadState() {
        override val changed: Long = System.currentTimeMillis()

        fun toInstallTask() = InstallTask(
            packageName = packageName,
            repositoryId = repoId,
            versionCode = release.versionCode,
            versionName = release.version,
            label = name,
            cacheFileName = release.cacheFileName,
            added = System.currentTimeMillis(),
            requireUser = false,
        )
    }

    @Serializable
    class Error(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val resultCode: Int,
        val validationError: ValidationError,
        val stopReason: Int = WorkInfo.STOP_REASON_NOT_STOPPED,
    ) : DownloadState() {
        override val changed: Long = System.currentTimeMillis()
    }

    @Serializable
    class Cancel(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
    ) : DownloadState() {
        override val changed: Long = System.currentTimeMillis()
    }

    val description: Int
        get() = when (this) {
            is Pending     -> R.string.pending
            is Connecting  -> R.string.connecting
            is Downloading -> R.string.downloading
            is Success     -> R.string.installing
            is Error       -> R.string.error
            else           -> R.string.cancel
        }

    val isActive: Boolean
        get() = (this is Connecting || this is Downloading || this is Pending) && System.currentTimeMillis() - this.changed < 60_000L

    fun toActionState() = when (this) {
        is Pending     -> ActionState.CancelPending
        is Connecting  -> ActionState.CancelConnecting
        is Downloading -> ActionState.CancelDownloading
        else           -> null
    }

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<DownloadState>(json)
    }
}

enum class ValidationError { NONE, INTEGRITY, FORMAT, METADATA, SIGNATURE, PERMISSIONS, FILE_SIZE, SENSITIVE_PERMISSION, UNKNOWN }

sealed class ErrorType {
    data object Network : ErrorType()
    class Http(val code: Int) : ErrorType()
    class Validation(val validateError: ValidationError) : ErrorType()
}
