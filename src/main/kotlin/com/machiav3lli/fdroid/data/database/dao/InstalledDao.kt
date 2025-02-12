package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.Installed
import kotlinx.coroutines.flow.Flow

// TODO make sure that apps that not uninstalled by Droid-ify still get removed
@Dao
interface InstalledDao : BaseDao<Installed> {
    fun put(vararg installed: Installed) {
        installed.forEach { upsert(it) }
    }

    @Query("SELECT * FROM memory_installed")
    fun getAllFlow(): Flow<List<Installed>>

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    fun get(packageName: String): Installed?

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<Installed?>

    @Query("DELETE FROM memory_installed WHERE packageName = :packageName")
    fun delete(packageName: String)

    @Query("DELETE FROM memory_installed")
    fun emptyTable()
}