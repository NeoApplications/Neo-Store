package com.looker.droidify.database.dao

import androidx.room.*

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