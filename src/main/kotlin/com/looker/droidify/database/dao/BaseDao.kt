package com.looker.droidify.database.dao

import androidx.room.*
import com.looker.droidify.database.entity.Ignored

interface BaseDao<T> {
    @Insert
    fun insert(vararg product: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(vararg product: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg obj: T): Int

    @Delete
    fun delete(obj: T)
}

@Dao
interface LockDao : BaseDao<Ignored> {
    @Query("DELETE FROM memory_lock WHERE packageName = :packageName")
    fun delete(packageName: String)
}