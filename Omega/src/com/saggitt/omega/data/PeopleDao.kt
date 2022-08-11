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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PeopleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(people: PeopleInfo)

    @Query("SELECT * FROM PeopleInfo WHERE contactName LIKE '%' || :query || '%' LIMIT 5")
    fun findPeople(query: String): List<PeopleInfo>

    @Query("DELETE FROM peopleinfo")
    suspend fun deleteAll()
}