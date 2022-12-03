package com.machiav3lli.fdroid.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.database.entity.Tracker
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao : BaseDao<Tracker> {
    @get:Query("SELECT * FROM `tracker`")
    val all: List<Tracker>

    @get:Query("SELECT * FROM `tracker`")
    val allFlow: Flow<List<Tracker>>

    @Query("SELECT * FROM `tracker` WHERE key = :key")
    fun get(key: Int): Tracker?

    @Query("SELECT * FROM `tracker` WHERE key = :key")
    fun getFlow(key: Int): Flow<Tracker?>
}
