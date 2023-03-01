package com.machiav3lli.fdroid.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.database.entity.Downloaded
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedDao : BaseDao<Downloaded> {
    @get:Query("SELECT * FROM downloaded")
    val allFlow: Flow<List<Downloaded>>

    @Query("SELECT * FROM downloaded WHERE packageName = :packageName")
    fun get(packageName: String): List<Downloaded?>

    @Query("SELECT * FROM downloaded WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<Downloaded?>>

    @Query("DELETE FROM downloaded WHERE packageName = :packageName")
    fun delete(packageName: String)

    @Query("DELETE FROM downloaded")
    fun emptyTable()
}
