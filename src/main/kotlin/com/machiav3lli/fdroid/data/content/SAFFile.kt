package com.machiav3lli.fdroid.data.content

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.machiav3lli.fdroid.R
import java.io.FileInputStream
import java.io.FileOutputStream

class SAFFile(context: Context, val uri: Uri) {

    private val mContext: Context = context

    fun read(): String? {
        try {
            val pfd = mContext.contentResolver.openFileDescriptor(uri, "r")
            val inStream = FileInputStream(pfd?.fileDescriptor).buffered()

            return try {
                inStream.reader().readText()
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to read $uri", t)
                null
            } finally {
                inStream.close()
                pfd?.close()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to read $uri", t)
            return null
        }
    }

    fun delete(): Boolean {
        return try {
            mContext.contentResolver.delete(uri, null, null) != 0
        } catch (e: UnsupportedOperationException) {
            DocumentFile.fromSingleUri(mContext, uri)?.delete() ?: false
        }
    }

    fun share(context: Context) {
        val shareTitle = context.getString(R.string.share)
        val shareText = context.getString(R.string.file_share_text)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = MAIN_MIME_TYPE
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        context.startActivity(Intent.createChooser(shareIntent, shareTitle))
    }

    companion object {
        const val TAG: String = "SAFFile"

        const val MAIN_MIME_TYPE = "application/octet-stream"
        const val EXTRAS_EXTENSION = "xts"
        const val REPOS_EXTENSION = "rps"
        const val APPS_EXTENSION = "aps"
        const val EXTRAS_MIME_TYPE = "application/vnd.neostore.xts"
        const val REPOS_MIME_TYPE = "application/vnd.neostore.rps"
        const val APPS_MIME_TYPE = "application/vnd.neostore.aps"
        val EXTRAS_MIME_ARRAY = arrayOf(EXTRAS_MIME_TYPE, MAIN_MIME_TYPE)
        val REPOS_MIME_ARRAY = arrayOf(REPOS_MIME_TYPE, MAIN_MIME_TYPE)
        val APPS_MIME_ARRAY = arrayOf(APPS_MIME_TYPE, MAIN_MIME_TYPE)

        @SuppressLint("MissingPermission")
        fun write(context: Context, location: Uri, content: String): Boolean {
            val pfd = context.contentResolver.openFileDescriptor(location, "w")
            val outStream = FileOutputStream(pfd?.fileDescriptor).buffered()
            val writer = outStream.bufferedWriter()

            return try {
                writer.write(content)
                Log.e(TAG, "Success to create backup")
                true
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to create backup", t)
                false
            } finally {
                writer.close()
                outStream.close()
                pfd?.close()
            }
        }
    }
}