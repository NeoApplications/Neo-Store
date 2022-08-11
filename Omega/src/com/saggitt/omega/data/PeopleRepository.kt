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
 */
package com.saggitt.omega.data

import android.content.Context
import com.android.launcher3.util.MainThreadInitializedObject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.plus

class PeopleRepository(context: Context) {
    private val scope = CoroutineScope(Dispatchers.IO) + CoroutineName("PeopleRepository")
    private val dao = NeoLauncherDb.INSTANCE.get(context).peopleDao()

    suspend fun insert(people: PeopleInfo) {
        dao.insert(people)
    }

    fun findPeople(query: String): List<PeopleInfo?> {
        return dao.findPeople(query)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    companion object {
        val INSTANCE = MainThreadInitializedObject(::PeopleRepository)
    }
}