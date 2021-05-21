/*
 *     This file is part of Lawnchair Launcher.
 *
 *     Lawnchair Launcher is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Lawnchair Launcher is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with Lawnchair Launcher.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.saggitt.omega.model

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Point
import android.util.Log
import com.android.launcher3.*
import com.android.launcher3.LauncherSettings.Favorites
import com.android.launcher3.model.GridSizeMigrationTask
import com.android.launcher3.provider.LauncherDbUtils.SQLiteTransaction
import com.android.launcher3.util.GridOccupancy
import com.android.launcher3.widget.custom.CustomWidgetManager
import com.saggitt.omega.settings.SettingsActivity
import java.util.*
import java.util.stream.Collectors

class HomeWidgetMigrationTask private constructor(
    val context: Context,
    db: SQLiteDatabase,
    validPackages: HashSet<String>,
    usePreviewTable: Boolean,
    sourceSize: Point,
    targetSize: Point
) : GridSizeMigrationTask(context, db, validPackages, usePreviewTable, sourceSize, targetSize) {
    private val mTrgX: Int = targetSize.x
    private val mTrgY: Int = targetSize.y
    private val mTableName: String =
        if (usePreviewTable) Favorites.PREVIEW_TABLE_NAME else Favorites.TABLE_NAME

    @Throws(Exception::class)
    override fun migrateWorkspace(): Boolean {
        @SuppressLint("VisibleForTests") val allScreens = getWorkspaceScreenIds(mDb, mTableName)
        if (allScreens.isEmpty) {
            throw Exception("Unable to get workspace screens")
        }
        val allowOverlap = Utilities.getPrefs(mContext)
            .getBoolean(SettingsActivity.ALLOW_OVERLAP_PREF, false)
        val occupied = GridOccupancy(mTrgX, mTrgY)
        if (!allowOverlap) {
            val firstScreenItems = ArrayList<DbEntry>()
            for (i in 0 until allScreens.size()) {
                val screenId = allScreens[i]
                val items = loadWorkspaceEntries(screenId)
                if (screenId == Workspace.FIRST_SCREEN_ID) {
                    firstScreenItems.addAll(items)
                    break
                }
            }
            for (item in firstScreenItems) {
                occupied.markCells(item, true)
            }
        }
        if (allowOverlap || occupied.isRegionVacant(0, 0, mTrgX, 1)) {
            val customWidgets: List<LauncherAppWidgetProviderInfo?>? =
                CustomWidgetManager.INSTANCE[mContext].stream()
                    .collect(Collectors.toList())
            if (customWidgets!!.isNotEmpty()) {
                val provider = customWidgets[0]
                val widgetId = CustomWidgetManager.INSTANCE[mContext]
                    .getWidgetIdForCustomProvider(provider!!.provider)
                val itemId = LauncherSettings.Settings.call(
                    mContext.contentResolver,
                    LauncherSettings.Settings.METHOD_NEW_ITEM_ID
                )
                    .getLong(LauncherSettings.Settings.EXTRA_VALUE)
                val values = ContentValues()
                values.put(Favorites._ID, itemId)
                values.put(Favorites.CONTAINER, Favorites.CONTAINER_DESKTOP)
                values.put(Favorites.SCREEN, Workspace.FIRST_SCREEN_ID)
                values.put(Favorites.CELLX, 0)
                values.put(Favorites.CELLY, 0)
                values.put(Favorites.SPANX, mTrgX)
                values.put(Favorites.SPANY, 1)
                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_CUSTOM_APPWIDGET)
                values.put(Favorites.APPWIDGET_ID, widgetId)
                values.put(Favorites.APPWIDGET_PROVIDER, provider.provider.flattenToString())
                mDb.insert(Favorites.TABLE_NAME, null, values)
            }
        }
        return true
    }

    companion object {
        private const val TAG: String = "HomeWidgetMigrationTask"
        private const val PREF_MIGRATION_STATUS: String = "pref_migratedSmartspace"

        @JvmStatic
        @SuppressLint("ApplySharedPref")
        fun migrateIfNeeded(context: Context?) {
            val prefs = Utilities.getPrefs(context)
            val needsMigration = (!prefs!!.getBoolean(PREF_MIGRATION_STATUS, false)
                    && prefs.getBoolean(SettingsActivity.SMARTSPACE_PREF, false))
            if (!needsMigration) return
            // Save the pref so we only run migration once
            prefs.edit().putBoolean(PREF_MIGRATION_STATUS, true).commit()
            val validPackages = getValidPackages(context)
            val idp = LauncherAppState.getIDP(context)
            val size = Point(idp!!.numColumns, idp.numRows)
            val migrationStartTime = System.currentTimeMillis()
            try {
                (LauncherSettings.Settings.call(
                    context!!.contentResolver, LauncherSettings.Settings.METHOD_NEW_TRANSACTION
                )
                    .getBinder(LauncherSettings.Settings.EXTRA_VALUE) as SQLiteTransaction).use { transaction ->
                    if (!HomeWidgetMigrationTask(
                            context, transaction.db,
                            validPackages, false, size, size
                        ).migrateWorkspace()
                    ) {
                        throw RuntimeException("Failed to migrate Smartspace")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during grid migration", e)
            } finally {
                Log.v(
                    TAG, "Home widget migration completed in "
                            + (System.currentTimeMillis() - migrationStartTime)
                )
            }
        }
    }
}