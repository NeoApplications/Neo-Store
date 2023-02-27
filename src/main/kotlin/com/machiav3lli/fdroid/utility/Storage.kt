package com.machiav3lli.fdroid.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.file.DocumentFileCompat
import com.machiav3lli.fdroid.content.Preferences

fun Context.getDownloadFolder(): DocumentFile? = DocumentFileCompat
    .fromUri(this, Uri.parse(Preferences[Preferences.Key.DownloadDirectory]))

val DOWNLOAD_DIRECTORY_INTENT = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
    .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)

val isDownloadExternal: Boolean
    get() = Preferences[Preferences.Key.EnableDownloadDirectory] && Preferences[Preferences.Key.DownloadDirectory] != ""
