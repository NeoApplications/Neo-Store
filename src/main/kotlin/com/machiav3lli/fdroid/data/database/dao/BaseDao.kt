package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Upsert

interface BaseDao<T> {
    @Insert
    suspend fun insert(vararg product: T)

    @Upsert
    suspend fun upsert(vararg product: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(vararg obj: T): Int

    @Delete
    suspend fun delete(obj: T)
}