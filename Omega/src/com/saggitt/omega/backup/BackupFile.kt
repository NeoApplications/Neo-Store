/*
 * This file is part of Omega Launcher
 * Copyright (c) 2022   Omega Launcher Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.backup

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.android.launcher3.BuildConfig
import com.android.launcher3.LauncherFiles
import com.android.launcher3.R
import com.android.launcher3.Utilities
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupFile(context: Context, val uri: Uri) {

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
                    return meta
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Unable to read meta for $uri", t)
                return meta
            } finally {
                zipIs.close()
                inStream.close()
                pfd?.close()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to read meta for $uri", t)
            return null
        }
        return null
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
        return Pair(
            Utilities.getScaledDownBitmap(screenshot, 1000, false),
            Utilities.getScaledDownBitmap(wallpaper, 1000, false)
        )
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
            val dbFile2 = contextWrapper.getDatabasePath(LauncherFiles.LAUNCHER_DB2)
            val dir = contextWrapper.cacheDir.parent
            val settingsFile =
                File(dir, "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml")

            val pfd = mContext.contentResolver.openFileDescriptor(uri, "r")
            val inStream = FileInputStream(pfd?.fileDescriptor)
            val zipIs = ZipInputStream(inStream)
            val data = ByteArray(BUFFER)
            var entry: ZipEntry?
            var success: Boolean
            try {
                while (true) {
                    entry = zipIs.nextEntry
                    if (entry == null) break
                    Log.d(TAG, "Found entry ${entry.name}")
                    val file = if (entry.name.matches(Regex(LauncherFiles.LAUNCHER_DB_CUSTOM))) {
                        if (contents and INCLUDE_HOME_SCREEN == 0) continue
                        val dbFile = contextWrapper.getDatabasePath(entry.name)
                        dbFile
                    } else if (entry.name == dbFile2.name) {
                        if (contents and INCLUDE_HOME_SCREEN == 0) continue
                        dbFile2
                    } else if (entry.name == "NeoLauncher.db-shm") {
                        if (contents and INCLUDE_HOME_SCREEN == 0) continue
                        contextWrapper.getDatabasePath("NeoLauncher.db-shm")
                    } else if (entry.name == "NeoLauncher.db-wal") {
                        if (contents and INCLUDE_HOME_SCREEN == 0) continue
                        contextWrapper.getDatabasePath("NeoLauncher.db-wal")
                    } else if (entry.name.endsWith("_preferences.xml")) {
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
                return success
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to restore $uri", t)
                success = false
                return success
            } finally {
                zipIs.close()
                inStream.close()
                pfd?.close()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to restore $uri", t)
            return false
        }
    }

    fun delete(): Boolean {
        return mContext.contentResolver.delete(uri, null, null) != 0
    }

    fun share(context: Context) {
        val shareTitle = context.getString(R.string.backup_share_title)
        val shareText = context.getString(R.string.backup_share_text)
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = MIME_TYPE
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText)
        context.startActivity(Intent.createChooser(shareIntent, shareTitle))
    }

    class MetaLoader(private val backupFile: BackupFile) {
        var callback: Callback? = null
        var meta: Meta? = null
        private var withPreview = false
        var loaded = false
        private var loading = false

        private val uiScope = CoroutineScope(Dispatchers.Main)

        fun loadMeta(withPreview: Boolean = false) {
            if (loading) return
            if (!loaded) {
                loading = true
                this.withPreview = withPreview

                uiScope.launch {
                    loadMetaTask()
                }

            } else {
                callback?.onMetaLoaded()
            }
        }

        private suspend fun loadMetaTask() {
            withContext(Dispatchers.Main) {
                backupFile.meta
                if (withPreview) {
                    backupFile.meta?.preview = backupFile.readPreview()
                }
                meta = backupFile.meta
                loaded = true
                callback?.onMetaLoaded()
            }
        }

        interface Callback {
            fun onMetaLoaded()
        }
    }

    data class Meta(val name: String, val contents: Int, val timestamp: String) {

        val localizedTimestamp: String? = SimpleDateFormat
            .getDateTimeInstance()
            .format(timestampFormat.parse(timestamp)!!)
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

        const val INCLUDE_HOME_SCREEN = 1 shl 0
        const val INCLUDE_SETTINGS = 1 shl 1
        const val INCLUDE_WALLPAPER = 1 shl 2

        const val BUFFER = 2018

        const val EXTENSION = "zbk"
        const val MIME_TYPE = "application/vnd.omega.backup"
        val EXTRA_MIME_TYPES = arrayOf(MIME_TYPE, "application/x-zip", "application/octet-stream")

        const val WALLPAPER_FILE_NAME = "wallpaper.png"
        val timestampFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)

        fun getFolder(context: Context): File {
            val folder = File(
                context.getExternalFilesDir(null),
                "backup"
            )
            Log.d(TAG, "path: $folder")
            if (!folder.exists()) {
                folder.mkdirs()
            }
            return folder
        }

        fun listLocalBackups(context: Context): List<BackupFile> {
            return getFolder(context).listFiles { file -> file.extension == EXTENSION }
                ?.sortedByDescending { it.lastModified() }
                ?.map {
                    FileProvider.getUriForFile(
                        context,
                        "${BuildConfig.APPLICATION_ID}.overview.fileprovider",
                        it
                    )
                }
                ?.map { BackupFile(context, it) }
                ?: Collections.emptyList()
        }

        private fun prepareConfig(context: Context) {
            Utilities.getOmegaPrefs(context).blockingEdit {
                restoreSuccess = true
                developerOptionsEnabled.onSetValue(false)
            }
        }

        private fun cleanupConfig(context: Context, devOptionsEnabled: Boolean) {
            Utilities.getOmegaPrefs(context).blockingEdit {
                restoreSuccess = false
                developerOptionsEnabled.onSetValue(devOptionsEnabled)
            }
        }

        @SuppressLint("MissingPermission")
        fun create(context: Context, name: String, location: Uri, contents: Int): Boolean {
            val contextWrapper = ContextWrapper(context)
            val files: MutableList<File> = ArrayList()
            if (contents or INCLUDE_HOME_SCREEN != 0) {
                contextWrapper.databaseList()
                    .filter { it.matches(Regex(LauncherFiles.LAUNCHER_DB_CUSTOM)) }
                    .forEach { files.add(contextWrapper.getDatabasePath(it)) }
                files.add(contextWrapper.getDatabasePath(LauncherFiles.LAUNCHER_DB2))
                files.add(contextWrapper.getDatabasePath("NeoLauncher.db-shm"))
                files.add(contextWrapper.getDatabasePath("NeoLauncher.db-wal"))
                files.add(contextWrapper.getDatabasePath(LauncherFiles.WIDGET_PREVIEWS_DB))
                files.add(contextWrapper.getDatabasePath(LauncherFiles.APP_ICONS_DB))
            }
            if (contents or INCLUDE_SETTINGS != 0) {
                val dir = contextWrapper.cacheDir.parent
                files.add(
                    File(
                        dir,
                        "shared_prefs/" + LauncherFiles.SHARED_PREFERENCES_KEY + ".xml"
                    )
                )
                files.add(
                    File(
                        dir,
                        "shared_prefs/" + LauncherFiles.DEVICE_PREFERENCES_KEY + ".xml"
                    )
                )
            }

            val devOptionsEnabled = Utilities.getOmegaPrefs(context).developerOptionsEnabled
            prepareConfig(context)
            val pfd = context.contentResolver.openFileDescriptor(location, "w")
            val outStream = FileOutputStream(pfd?.fileDescriptor)
            val out = ZipOutputStream(BufferedOutputStream(outStream))
            val data = ByteArray(BUFFER)
            var success: Boolean
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
                Log.e(TAG, "Success to create backup")
                return success
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to create backup", t)
                success = false
                return success
            } finally {
                out.close()
                outStream.close()
                pfd?.close()
                cleanupConfig(context, devOptionsEnabled.onGetValue())
            }
        }

        private fun getMeta(name: String, contents: Int) = Meta(
            name = name,
            contents = contents,
            timestamp = getTimestamp()
        )

        private fun getTimestamp(): String {
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US)
            return simpleDateFormat.format(Date())
        }
    }
}
