package com.machiav3lli.fdroid.service.worker

import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.database.entity.InstallTask
import com.machiav3lli.fdroid.database.entity.Release
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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

    @Serializable
    class Pending(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val blocked: Boolean,
    ) : DownloadState()

    @Serializable
    class Connecting(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
    ) : DownloadState()

    @Serializable
    class Downloading(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val read: Long,
        val total: Long?,
    ) : DownloadState()

    @Serializable
    class Success(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val release: Release,
    ) : DownloadState()

    @Serializable
    class Error(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
        val resultCode: Int,
        val validationError: ValidationError,
    ) : DownloadState()

    @Serializable
    class Cancel(
        override val packageName: String,
        override val name: String,
        override val version: String,
        override val cacheFileName: String,
        override val repoId: Long,
    ) : DownloadState()

    val description: Int
        get() = when (this) {
            is Pending     -> R.string.pending
            is Connecting  -> R.string.connecting
            is Downloading -> R.string.downloading
            is Success     -> R.string.installing
            else           -> R.string.cancel
        }

    val isActive: Boolean
        get() = this is Connecting || this is Downloading || this is Pending

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<DownloadState>(json)
    }
}

enum class ValidationError { NONE, INTEGRITY, FORMAT, METADATA, SIGNATURE, PERMISSIONS }

sealed class ErrorType {
    data object Network : ErrorType()
    class Http(val code: Int) : ErrorType()
    class Validation(val validateError: ValidationError) : ErrorType()
}
