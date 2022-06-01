/*
 * This file is part of Neo Launcher
 * Copyright (c) 2022   Neo Launcher Team
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
 *
 */

package com.saggitt.omega.data

import android.content.Context
import com.android.launcher3.util.MainThreadInitializedObject
import kotlinx.coroutines.*

class AppTrackerRepository(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("AppTrackerRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).appTrackerDao()

    fun getAppsCount(): List<AppTracker> {
        var result: List<AppTracker> = listOf()
        scope.launch {
            result = dao.getAppCount()
        }
        return result
    }

    fun updateAppCount(packageName: String) {
        scope.launch {
            //Check if the app is already in the database
            if (dao.appExist(packageName)) {
                //If it is, update the count
                val currentCount = dao.getAppCount(packageName)
                dao.update(AppTracker(packageName, currentCount + 1))
            } else {
                dao.insert(AppTracker(packageName, 1))
            }
        }
    }

    fun deleteAppCount(packageName: String) {
        scope.launch { dao.deleteAppCount(packageName) }
    }

    companion object {
        val INSTANCE = MainThreadInitializedObject(::AppTrackerRepository)
    }
}