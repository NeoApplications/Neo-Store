package com.machiav3lli.fdroid.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.database.entity.Tracker
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao : BaseDao<Tracker> {
    @Query("SELECT * FROM `tracker`")
    fun getAll(): List<Tracker>

    @Query("SELECT * FROM `tracker`")
    fun getAllFlow(): Flow<List<Tracker>>

    @Query("SELECT * FROM `tracker` WHERE key = :key")
    fun get(key: Int): Tracker?

    @Query("SELECT * FROM `tracker` WHERE key = :key")
    fun getFlow(key: Int): Flow<Tracker?>
}
