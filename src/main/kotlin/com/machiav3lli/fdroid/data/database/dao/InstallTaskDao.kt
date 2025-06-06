package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import kotlinx.coroutines.flow.Flow

// TODO make sure that apps that not uninstalled by Droid-ify still get removed
@Dao
interface InstallTaskDao : BaseDao<InstallTask> {
    @Query("SELECT * FROM install_task ORDER BY added DESC")
    fun getAll(): List<InstallTask>

    @Query("SELECT * FROM install_task ORDER BY added DESC")
    fun getAllFlow(): Flow<List<InstallTask>>

    @Query("SELECT * FROM install_task WHERE cacheFileName = :fileName ORDER BY added ASC")
    fun get(fileName: String): InstallTask?

    @Query("SELECT * FROM install_task WHERE cacheFileName = :fileName ORDER BY added ASC")
    fun getFlow(fileName: String): Flow<InstallTask?>

    @Query("DELETE FROM install_task WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM install_task")
    suspend fun emptyTable()
}