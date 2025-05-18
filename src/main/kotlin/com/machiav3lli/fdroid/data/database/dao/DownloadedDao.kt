package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedDao : BaseDao<Downloaded> {
    @Query("SELECT * FROM downloaded")
    fun getAllFlow(): Flow<List<Downloaded>>

    @Query("SELECT * FROM downloaded WHERE packageName = :packageName")
    fun get(packageName: String): List<Downloaded>

    @Query("SELECT * FROM downloaded WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<Downloaded>>

    @Query("SELECT * FROM downloaded WHERE packageName = :packageName ORDER BY changed LIMIT 1")
    fun getLatest(packageName: String): Downloaded?

    @Query("SELECT * FROM downloaded WHERE packageName = :packageName ORDER BY changed DESC LIMIT 1")
    fun getLatestFlow(packageName: String): Flow<Downloaded?>

    @Query("DELETE FROM downloaded WHERE packageName = :packageName")
    suspend fun deleteAll(packageName: String)

    @Query("DELETE FROM downloaded WHERE packageName = :packageName AND repositoryId = :repositoryId AND version = :version AND cacheFileName = :cacheFileName")
    suspend fun delete(
        packageName: String,
        version: String,
        repositoryId: Long,
        cacheFileName: String
    )

    @Query("DELETE FROM downloaded")
    suspend fun emptyTable()
}
