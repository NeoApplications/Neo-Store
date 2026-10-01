package com.machiav3lli.fdroid.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.anggrayudi.storage.StorageFile
import com.anggrayudi.storage.copyTo
import com.anggrayudi.storage.transfer.TransferResult
import com.machiav3lli.fdroid.data.content.Preferences

private const val TAG = "util.Storage"

fun Context.getDownloadFolder(): StorageFile? = StorageFile
    .from(this, Preferences[Preferences.Key.DownloadDirectory].toUri())

suspend fun StorageFile.copyTo(
    downloadFolder: StorageFile
) {
    copyTo(
        targetFolder = downloadFolder,
    ).let { result ->
        when (result) {
            is TransferResult.Success
                -> Log.d(TAG, "Completed result: ${result.result.name}")

            is TransferResult.Failure
                -> Log.e(TAG, result.errorCode.name, result.cause) // TODO add notification

            is TransferResult.Skipped
                -> Log.d(TAG, "Skipped as ${result.existingTarget} exists...")
        }
    }
}

val DOWNLOAD_DIRECTORY_INTENT = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)

val isDownloadExternal: Boolean
    get() = Preferences[Preferences.Key.EnableDownloadDirectory] && Preferences[Preferences.Key.DownloadDirectory] != ""
