package com.machiav3lli.fdroid.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.database.entity.InstallTask
import kotlinx.coroutines.flow.Flow

// TODO make sure that apps that not uninstalled by Droid-ify still get removed
@Dao
interface InstallTaskDao : BaseDao<InstallTask> {
    suspend fun put(vararg tasks: InstallTask) {
        tasks.forEach { upsert(it) }
    }

    @Query("SELECT * FROM `installtask` ORDER BY added ASC")
    fun getAll(): List<InstallTask>

    @Query("SELECT * FROM `installtask` ORDER BY added ASC")
    fun getAllFlow(): Flow<List<InstallTask>>

    @Query("SELECT * FROM `installtask` WHERE packageName = :packageName AND versionCode = :versionCode")
    fun get(packageName: String, versionCode: Long): InstallTask?

    @Query("SELECT * FROM `installtask` WHERE packageName = :packageName AND versionCode = :versionCode")
    fun getFlow(packageName: String, versionCode: Long): Flow<InstallTask?>

    @Query("DELETE FROM `installtask` WHERE packageName = :packageName")
    fun delete(packageName: String)

    @Query("DELETE FROM `installtask`")
    fun emptyTable()
}