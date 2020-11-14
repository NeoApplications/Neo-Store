package com.saggitt.omega.backup

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.android.launcher3.BuildConfig
import com.android.launcher3.LauncherFiles
import com.android.launcher3.Utilities
import org.json.JSONArray
import java.io.*
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class OmegaBackup(val context: Context, val uri: Uri) {

    val mContext: Context = context
    val meta by lazy { readMeta() }

    private fun readMeta(): Meta? {
        try {
            val pfd = mContext.contentResolver.openFileDescriptor(uri, "r")
            val inStream = FileInputStream(pfd?.fileDescriptor)
            val zipIs = ZipInputStream(inStream)
            var entry: ZipEntry?
            var meta: Meta? = null
            try {
                while (true) {
                    entry = zipIs.nextEntry
                    if (entry == null) break
                    if (entry.name != Meta.FILE_NAME) continue
                    meta = Meta.fromString(String(zipIs.readBytes(), StandardCharsets.UTF_8))
                    break
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Unable to read meta for $uri", t)
            } finally {
                zipIs.close()
                inStream.close()
                pfd?.close()
                return meta
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to read meta for $uri", t)
            return null
        }
    }

    private fun readPreview(): Pair<Bitmap?, Bitmap?>? {
        var entry: ZipEntry?
        var screenshot: Bitmap? = null
        var wallpaper: Bitmap? = null
        readZip { zipIs ->
            while (true) {
                entry = zipIs.nextEntry
                if (entry == null) break
                if (entry!!.name == "screenshot.png") {
                    screenshot = BitmapFactory.decodeStream(zipIs)
                } else if (entry!!.name == WALLPAPER_FILE_NAME) {
                    wallpaper = BitmapFactory.decodeStream(zipIs)
                }
            }
        }
        if (screenshot == wallpaper) return null // both are null
        return Pair(Utilities.getScaledDownBitmap(screenshot, 1000, false),
                Utilities.getScaledDownBitmap(wallpaper, 1000, false))
    }

    private inline fun readZip(body: (ZipInputStream) -> Unit) {
        try {
            val pfd = mContext.contentResolver.openFileDescriptor(uri, "r")
            val inStream = FileInputStream(pfd?.fileDescriptor)
            val zipIs = ZipInputStream(inStream)
            try {
                body(zipIs)
            } catch (t: Throwable) {
                Log.e(TAG, "Unable to read zip for $uri", t)
            } finally {
                zipIs.close()
                inStream.close()
                pfd?.close()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to read zip for $uri", t)
        }
    }

    fun restore(contents: Int): Boolean {
        try {
            val contextWrapper = ContextWrapper(mContext)
            val dbFile = contextWrapper.getDatabasePath(LauncherFiles.LAUNCHER_DB)
            val dbFile2 = contextWrapper.getDatabasePath(LauncherFiles.LAUNCHER_DB2)
            val dir = contextWrapper.cacheDir.parent
            val settingsFile = File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml")

            val pfd = mContext.contentResolver.openFileDescriptor(uri, "r")
            val inStream = FileInputStream(pfd?.fileDescriptor)
            val zipIs = ZipInputStream(inStream)
            val data = ByteArray(BUFFER)
            var entry: ZipEntry?
            var success = false
            try {
                while (true) {
                    entry = zipIs.nextEntry
                    if (entry == null) break
                    Log.d(TAG, "Found entry ${entry.name}")
                    val file = if (entry.name == dbFile.name) {
                        if (contents and INCLUDE_HOMESCREEN == 0) continue
                        dbFile
                    } else if (entry.name == dbFile2.name) {
                        if (contents and INCLUDE_HOMESCREEN == 0) continue
                        dbFile2
                    } else if (entry.name == settingsFile.name) {
                        if (contents and INCLUDE_SETTINGS == 0) continue
                        settingsFile
                    } else if (entry.name == WALLPAPER_FILE_NAME) {
                        if (contents and INCLUDE_WALLPAPER == 0) continue
                        val wallpaperManager = WallpaperManager.getInstance(mContext)
                        wallpaperManager.setBitmap(BitmapFactory.decodeStream(zipIs))
                        continue
                    } else {
                        continue
                    }
                    val out = FileOutputStream(file)
                    Log.d(TAG, "Restoring ${entry.name} to ${file.absolutePath}")
                    var count: Int
                    while (true) {
                        count = zipIs.read(data, 0, BUFFER)
                        if (count == -1) break
                        out.write(data, 0, count)
                    }
                    out.close()
                }
                success = true
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to restore $uri", t)
            } finally {
                zipIs.close()
                inStream.close()
                pfd?.close()
                return success
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to restore $uri", t)
            return false
        }
    }

    fun delete(): Boolean {
        return mContext.contentResolver.delete(uri, null, null) != 0
    }

    class MetaLoader(val backup: OmegaBackup) {
        var callback: Callback? = null
        var meta: Meta? = null
        var withPreview = false
        var loaded = false
        private var loading = false

        fun loadMeta(withPreview: Boolean = false) {
            if (loading) return
            if (!loaded) {
                loading = true
                this.withPreview = withPreview
                LoadMetaTask().execute()
            } else {
                callback?.onMetaLoaded()
            }
        }

        @SuppressLint("StaticFieldLeak")
        inner class LoadMetaTask : AsyncTask<Void, Void, Meta?>() {

            override fun doInBackground(vararg params: Void?): Meta? {
                backup.meta
                if (withPreview) {
                    backup.meta?.preview = backup.readPreview()
                }
                return backup.meta
            }

            override fun onPostExecute(result: Meta?) {
                meta = result
                loaded = true
                callback?.onMetaLoaded()
            }
        }

        interface Callback {

            fun onMetaLoaded()
        }
    }

    data class Meta(val name: String, val contents: Int, val timestamp: String) {

        val localizedTimestamp = SimpleDateFormat.getDateTimeInstance().format(timestampFormat.parse(timestamp))
        var preview: Pair<Bitmap?, Bitmap?>? = null
        override fun toString(): String {
            val arr = JSONArray()
            arr.put(VERSION)
            arr.put(name)
            arr.put(contents)
            arr.put(timestamp)
            return arr.toString()
        }

        fun recycle() {
            preview?.first?.recycle()
            preview?.second?.recycle()
        }

        companion object {

            const val VERSION = 1

            const val FILE_NAME = "lcbkp"

            @Suppress("unused")
            private const val VERSION_INDEX = 0
            private const val NAME_INDEX = 1
            private const val CONTENTS_INDEX = 2
            private const val TIMESTAMP_INDEX = 3

            fun fromString(string: String): Meta {
                val arr = JSONArray(string)
                return Meta(
                        name = arr.getString(NAME_INDEX),
                        contents = arr.getInt(CONTENTS_INDEX),
                        timestamp = arr.getString(TIMESTAMP_INDEX)
                )
            }
        }
    }

    companion object {
        const val TAG = "OmegaBackup"

        const val INCLUDE_HOMESCREEN = 1 shl 0
        const val INCLUDE_SETTINGS = 1 shl 1
        const val INCLUDE_WALLPAPER = 1 shl 2

        const val BUFFER = 2018

        const val EXTENSION = "zbk"
        const val MIME_TYPE = "application/vnd.omega.backup"
        val EXTRA_MIME_TYPES = arrayOf(MIME_TYPE, "application/x-zip", "application/octet-stream")

        const val WALLPAPER_FILE_NAME = "wallpaper.png"
        val timestampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)

        fun getFolder(): File {
            val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Omega/backup")
            Log.d(TAG, "path: $folder")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            return folder
        }

        fun listLocalBackups(context: Context): List<OmegaBackup> {
            return getFolder().listFiles { file -> file.extension == EXTENSION }
                    ?.sortedByDescending { it.lastModified() }
                    ?.map { FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", it) }
                    ?.map { OmegaBackup(context, it) }
                    ?: Collections.emptyList()
        }

        private fun prepareConfig(context: Context) {
            Utilities.getOmegaPrefs(context).blockingEdit {
                restoreSuccess = true
                developerOptionsEnabled = false
            }
        }

        private fun cleanupConfig(context: Context, devOptionsEnabled: Boolean) {
            Utilities.getOmegaPrefs(context).blockingEdit {
                restoreSuccess = false
                developerOptionsEnabled = devOptionsEnabled
            }
        }

        fun create(context: Context, name: String, location: Uri, contents: Int): Boolean {
            val contextWrapper = ContextWrapper(context)
            val files: MutableList<File> = ArrayList()
            if (contents or INCLUDE_HOMESCREEN != 0) {
                files.add(contextWrapper.getDatabasePath(LauncherFiles.LAUNCHER_DB))
                files.add(contextWrapper.getDatabasePath(LauncherFiles.LAUNCHER_DB2))
            }
            if (contents or INCLUDE_SETTINGS != 0) {
                val dir = contextWrapper.cacheDir.parent
                files.add(File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml"))
            }

            val devOptionsEnabled = Utilities.getOmegaPrefs(context)
                    .developerOptionsEnabled
            prepareConfig(context)
            val pfd = context.contentResolver.openFileDescriptor(location, "w")
            val outStream = FileOutputStream(pfd?.fileDescriptor)
            val out = ZipOutputStream(BufferedOutputStream(outStream))
            val data = ByteArray(BUFFER)
            var success = false
            try {
                val metaEntry = ZipEntry(Meta.FILE_NAME)
                out.putNextEntry(metaEntry)
                out.write(getMeta(name, contents).toString().toByteArray())
                if (contents or INCLUDE_WALLPAPER != 0) {
                    val wallpaperManager = WallpaperManager.getInstance(context)
                    val wallpaperDrawable = wallpaperManager.drawable
                    val wallpaperBitmap = Utilities.drawableToBitmap(wallpaperDrawable)
                    if (wallpaperBitmap != null) {
                        val wallpaperEntry = ZipEntry(WALLPAPER_FILE_NAME)
                        out.putNextEntry(wallpaperEntry)
                        wallpaperBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                }
                files.forEach { file ->
                    val input = BufferedInputStream(FileInputStream(file), BUFFER)
                    val entry = ZipEntry(file.name)
                    out.putNextEntry(entry)
                    var count: Int
                    while (true) {
                        count = input.read(data, 0, BUFFER)
                        if (count == -1) break
                        out.write(data, 0, count)
                    }
                    input.close()
                }
                success = true
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to create backup", t)
            } finally {
                out.close()
                outStream.close()
                pfd?.close()
                cleanupConfig(context, devOptionsEnabled)
                return success
            }
        }

        private fun getMeta(name: String, contents: Int) = Meta(
                name = name,
                contents = contents,
                timestamp = getTimestamp()
        )

        private fun getTimestamp(): String {
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.US)
            return simpleDateFormat.format(Date())
        }
    }
}
