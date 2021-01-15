/*
 *  This file is part of Omega Launcher.
 *  Copyright (c) 2021   Saul Henriquez
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

package com.saggitt.omega.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.android.launcher3.LauncherFiles;
import com.saggitt.omega.model.AppCountInfo;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_HOME = LauncherFiles.LAUNCHER_DB2;
    private static final String TABLE_APP_COUNT = "app_count";
    //private static final String TABLE_DASH_ITEMS = "dash_items";

    /*CREAR TABLA PARA CONTAR APPS*/
    private static final String COLUMN_PACKAGE_NAME = "package_name";
    private static final String COLUMN_PACKAGE_COUNT = "package_count";
    private static final String COLUMN_PACKAGE_ID = "count_id";
    private static final String SQL_CREATE_COUNT =
            "CREATE TABLE " + TABLE_APP_COUNT + " ("
                    + COLUMN_PACKAGE_ID + " INTEGER PRIMARY KEY,"
                    + COLUMN_PACKAGE_NAME + " VARCHAR, "
                    + COLUMN_PACKAGE_COUNT + " INTEGER)";

    private static final String SQL_DELETE = "DROP TABLE IF EXISTS ";

    private SQLiteDatabase db;

    public DbHelper(Context c) {
        super(c, DATABASE_HOME, null, 1);
        db = getWritableDatabase();
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_COUNT);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // discard the data and start over
        db.execSQL(SQL_DELETE + TABLE_APP_COUNT);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public List<AppCountInfo> getAppsCount() {
        List<AppCountInfo> apps = new ArrayList<>();
        String SQL_QUERY = "SELECT package_name, package_count FROM app_count;";
        Cursor cursor = db.rawQuery(SQL_QUERY, null);
        if (!cursor.moveToFirst()) {
            return apps;
        }
        do {

            String name = cursor.getString(0);
            int count = cursor.getInt(1);
            apps.add(new AppCountInfo(name, count));
        }
        while (cursor.moveToNext());
        cursor.close();
        return apps;
    }

    public void updateAppCount(String packageName) {
        String SQL_QUERY = "SELECT package_count FROM app_count WHERE package_name='" + packageName + "';";
        Cursor cursor = db.rawQuery(SQL_QUERY, null);
        int appCount = 0;
        if (cursor.moveToFirst()) {
            appCount = cursor.getInt(0);
        } else {
            saveAppCount(packageName);
        }

        cursor.close();
        appCount++;
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_PACKAGE_COUNT, appCount);
        db.update(TABLE_APP_COUNT, cv, "package_name='" + packageName + "'", null);
    }

    private void saveAppCount(String packageName) {
        ContentValues itemValues = new ContentValues();
        itemValues.put(COLUMN_PACKAGE_NAME, packageName);
        itemValues.put(COLUMN_PACKAGE_COUNT, 0);
        db.insert(TABLE_APP_COUNT, null, itemValues);
    }

    public void deleteApp(String packageName) {
        db.delete(TABLE_APP_COUNT, "package_name='" + packageName + "'", null);
    }
}

