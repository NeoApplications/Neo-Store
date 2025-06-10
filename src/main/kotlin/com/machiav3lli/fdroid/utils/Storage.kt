package com.machiav3lli.fdroid.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.SingleFileConflictCallback
import com.anggrayudi.storage.file.DocumentFileCompat
import com.anggrayudi.storage.file.copyFileTo
import com.anggrayudi.storage.result.SingleFileResult
import com.machiav3lli.fdroid.data.content.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Context.getDownloadFolder(): DocumentFile? = DocumentFileCompat
    .fromUri(this, Preferences[Preferences.Key.DownloadDirectory].toUri())

suspend fun DocumentFile.copyTo(
    context: Context,
    downloadFolder: DocumentFile
) {
    copyFileTo(
        context = context,
        targetFolder = downloadFolder,
        onConflict = object : SingleFileConflictCallback<DocumentFile>() {
            override fun onFileConflict(
                destinationFile: DocumentFile,
                action: FileConflictAction
            ) {
                Log.d(this.toString(), "onFileConflict: $action")
                super.onFileConflict(destinationFile, action)
            }
        }
    ).collect { result ->
        when (result) {
            is SingleFileResult.Validating
            -> Log.d(
                this::javaClass.name,
                "Validating..."
            )

            is SingleFileResult.Preparing
            -> Log.d(
                this::javaClass.name,
                "Preparing..."
            )

            is SingleFileResult.CountingFiles
            -> Log.d(
                this::javaClass.name,
                "Counting files..."
            )

            is SingleFileResult.DeletingConflictedFile
            -> Log.d(
                this::javaClass.name,
                "Deleting conflicted files..."
            )

            is SingleFileResult.Starting
            -> Log.d(
                this::javaClass.name,
                "Starting..."
            )

            is SingleFileResult.InProgress
            -> Log.d(
                this::javaClass.name,
                "Progress: ${result.progress.toInt()}%"
            )

            is SingleFileResult.Completed
            -> Log.d(
                this::javaClass.name,
                "Completed result: ${result.result}"
            )

            is SingleFileResult.Error
            -> withContext(Dispatchers.IO) {
                Log.e(
                    this::javaClass.name,
                    result.errorCode.name
                )
                // add notification
            }
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
