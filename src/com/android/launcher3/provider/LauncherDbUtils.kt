/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.launcher3.provider

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.os.Binder
import android.os.Process
import android.util.Log
import com.android.launcher3.LauncherAppState
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.pm.UserCache
import com.android.launcher3.util.IntArray
import java.util.*

/**
 * A set of utility methods for Launcher DB used for DB updates and migration.
 */
object LauncherDbUtils {
    private const val TAG = "LauncherDbUtils"

    /**
     * Makes the first screen as screen 0 (if screen 0 already exists,
     * renames it to some other number).
     * If the first row of screen 0 is non empty, runs a 'lossy' GridMigrationTask to clear
     * the first row. The items in the first screen are moved and resized but the carry-forward
     * items are simply deleted.
     */
    @JvmStatic
    fun prepareScreenZeroToHostQsb(context: Context?, db: SQLiteDatabase): Boolean {
        try {
            SQLiteTransaction(db).use { t ->
                // Get the first screen
                val firstScreenId: Int
                db.rawQuery(
                    String.format(
                        Locale.ENGLISH,
                        "SELECT MIN(%1\$s) from %2\$s where %3\$s = %4\$d",
                        Favorites.SCREEN, Favorites.TABLE_NAME, Favorites.CONTAINER,
                        Favorites.CONTAINER_DESKTOP
                    ), null
                ).use { c ->
                    if (!c.moveToNext()) {
                        // No update needed
                        t.commit()
                        return true
                    }
                    firstScreenId = c.getInt(0)
                }
                if (firstScreenId != 0) {
                    // Rename the first screen to 0.
                    renameScreen(db, firstScreenId, 0)
                }

                // Check if the first row is empty
                if (DatabaseUtils.queryNumEntries(
                        db, Favorites.TABLE_NAME,
                        "container = -100 and screen = 0 and cellY = 0"
                    ) == 0L
                ) {
                    // First row is empty, no need to migrate.
                    t.commit()
                    return true
                }
                LossyScreenMigrationTask(context, LauncherAppState.getIDP(context), db)
                    .migrateScreen0()
                t.commit()
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update workspace size", e)
            return false
        }
    }

    private fun renameScreen(db: SQLiteDatabase, oldScreen: Int, newScreen: Int = 0) {
        val whereParams = arrayOf(oldScreen.toString())
        val values = ContentValues()
        values.put(Favorites.SCREEN, newScreen)
        db.update(Favorites.TABLE_NAME, values, "container = -100 and screen = ?", whereParams)
    }

    @JvmStatic
    fun queryIntArray(
        db: SQLiteDatabase, tableName: String?, columnName: String,
        selection: String?, groupBy: String?, orderBy: String?
    ): IntArray {
        val out = IntArray()
        db.query(
            tableName, arrayOf(columnName), selection, null,
            groupBy, null, orderBy
        ).use { c ->
            while (c.moveToNext()) {
                out.add(c.getInt(0))
            }
        }
        return out
    }

    @JvmStatic
    fun tableExists(db: SQLiteDatabase, tableName: String): Boolean {
        db.query(
            true, "sqlite_master", arrayOf("tbl_name"),
            "tbl_name = ?", arrayOf(tableName),
            null, null, null, null, null
        ).use { c -> return c.count > 0 }
    }

    @JvmStatic
    fun dropTable(db: SQLiteDatabase, tableName: String) {
        db.execSQL("DROP TABLE IF EXISTS $tableName")
    }

    /**
     * Copy fromTable in fromDb to toTable in toDb.
     */
    @JvmStatic
    fun copyTable(
        fromDb: SQLiteDatabase, fromTable: String, toDb: SQLiteDatabase,
        toTable: String, context: Context?
    ) {
        val userSerial = UserCache.INSTANCE[context].getSerialNumberForUser(
            Process.myUserHandle()
        )
        dropTable(toDb, toTable)
        Favorites.addTableToDb(toDb, userSerial, false, toTable)
        if (fromDb != toDb) {
            toDb.execSQL("ATTACH DATABASE '" + fromDb.path + "' AS from_db")
            toDb.execSQL(
                "INSERT INTO $toTable SELECT * FROM from_db.$fromTable"
            )
            toDb.execSQL("DETACH DATABASE 'from_db'")
        } else {
            toDb.execSQL("INSERT INTO $toTable SELECT * FROM $fromTable")
        }
    }

    /**
     * Utility class to simplify managing sqlite transactions
     */
    class SQLiteTransaction(val db: SQLiteDatabase) : Binder(), AutoCloseable {
        fun commit() {
            db.setTransactionSuccessful()
        }

        override fun close() {
            db.endTransaction()
        }

        init {
            db.beginTransaction()
        }
    }
}