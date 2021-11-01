/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Omega Launcher Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.util

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.android.launcher3.LauncherFiles
import com.saggitt.omega.allapps.AppCountInfo

class DbHelper(c: Context?) :
    SQLiteOpenHelper(c, DATABASE_HOME, null, 1) {
    private val db: SQLiteDatabase = writableDatabase
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_COUNT)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // discard the data and start over
        db.execSQL(SQL_DELETE + TABLE_APP_COUNT)
        onCreate(db)
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    val appsCount: List<AppCountInfo>
        get() {
            val apps: MutableList<AppCountInfo> = ArrayList()
            val sqlQuery = "SELECT package_name, package_count FROM app_count;"
            val cursor: Cursor = db.rawQuery(sqlQuery, null)
            if (!cursor.moveToFirst()) {
                return apps
            }
            do {
                val name: String = cursor.getString(0)
                val count: Int = cursor.getInt(1)
                apps.add(AppCountInfo(name, count))
            } while (cursor.moveToNext())
            cursor.close()
            return apps
        }

    fun updateAppCount(packageName: String) {
        val sqlQuery =
            "SELECT package_count FROM app_count WHERE package_name='$packageName';"
        val cursor: Cursor = db.rawQuery(sqlQuery, null)
        var appCount = 0
        if (cursor.moveToFirst()) {
            appCount = cursor.getInt(0)
        } else {
            saveAppCount(packageName)
        }
        cursor.close()
        appCount++
        val cv = ContentValues()
        cv.put(COLUMN_PACKAGE_COUNT, appCount)
        db.update(TABLE_APP_COUNT, cv, "package_name='$packageName'", null)
    }

    private fun saveAppCount(packageName: String) {
        val itemValues = ContentValues()
        itemValues.put(COLUMN_PACKAGE_NAME, packageName)
        itemValues.put(COLUMN_PACKAGE_COUNT, 0)
        db.insert(TABLE_APP_COUNT, null, itemValues)
    }

    fun deleteApp(packageName: String) {
        db.delete(TABLE_APP_COUNT, "package_name='$packageName'", null)
    }

    companion object {
        private const val DATABASE_HOME: String = LauncherFiles.LAUNCHER_DB2
        private const val TABLE_APP_COUNT = "app_count"

        /*CREAR TABLA PARA CONTAR APPS*/
        private const val COLUMN_PACKAGE_NAME = "package_name"
        private const val COLUMN_PACKAGE_COUNT = "package_count"
        private const val COLUMN_PACKAGE_ID = "count_id"
        private const val SQL_CREATE_COUNT = ("CREATE TABLE " + TABLE_APP_COUNT + " ("
                + COLUMN_PACKAGE_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_PACKAGE_NAME + " VARCHAR, "
                + COLUMN_PACKAGE_COUNT + " INTEGER)")
        private const val SQL_DELETE = "DROP TABLE IF EXISTS "
    }

}
