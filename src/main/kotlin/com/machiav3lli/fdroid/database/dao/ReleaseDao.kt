package com.machiav3lli.fdroid.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.ReleaseTemp

@Dao
interface ReleaseDao : BaseDao<Release> {
    // This one for the mode combining releases of different sources
    @Query("SELECT * FROM `release` WHERE packageName = :packageName")
    fun get(packageName: String): List<Release>

    // This one for the separating releases of different sources
    @Query("SELECT * FROM `release` WHERE packageName = :packageName AND signature = :signature")
    fun get(packageName: String, signature: String): List<Release>

    @Query("DELETE FROM `release` WHERE repositoryId = :id")
    fun deleteById(id: Long): Int
}

@Dao
interface ReleaseTempDao : BaseDao<ReleaseTemp> {
    @Query("SELECT * FROM temporary_release")
    fun getAll(): Array<ReleaseTemp>

    @Query("DELETE FROM temporary_release")
    fun emptyTable()
}
