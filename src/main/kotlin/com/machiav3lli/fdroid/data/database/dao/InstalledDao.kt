package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.Installed
import kotlinx.coroutines.flow.Flow

@Dao
interface InstalledDao : BaseDao<Installed> {
    @Query("SELECT * FROM memory_installed")
    fun getAllFlow(): Flow<List<Installed>>

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    suspend fun get(packageName: String): Installed?

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<Installed?>

    @Query("DELETE FROM memory_installed WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM memory_installed")
    suspend fun emptyTable()
}