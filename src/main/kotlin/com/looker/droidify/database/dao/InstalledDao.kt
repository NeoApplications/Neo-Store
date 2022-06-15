package com.looker.droidify.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.looker.droidify.database.entity.Installed

// TODO make sure that apps that not uninstalled by Droid-ify still get removed
@Dao
interface InstalledDao : BaseDao<Installed> {
    fun put(vararg installed: Installed) {
        installed.forEach { insertReplace(it) }
    }

    @get:Query("SELECT * FROM memory_installed")
    val allLive: LiveData<List<Installed>>

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    fun get(packageName: String): Installed?

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    fun getLive(packageName: String): LiveData<Installed?>

    @Query("DELETE FROM memory_installed WHERE packageName = :packageName")
    fun delete(packageName: String)

    @Query("DELETE FROM memory_installed")
    fun emptyTable()
}