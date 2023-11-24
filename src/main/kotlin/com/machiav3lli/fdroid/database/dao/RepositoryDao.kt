package com.machiav3lli.fdroid.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.machiav3lli.fdroid.database.entity.Repository
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao : BaseDao<Repository> {
    @Query("SELECT COUNT(_id) FROM repository")
    fun getCount(): Int

    fun put(repository: Repository): Repository {
        repository.let { item ->
            val newId = if (item.id > 0L) update(item).toLong() else returnInsert(item)
            return if (newId != repository.id) repository.copy(id = newId) else repository
        }
    }

    suspend fun insertOrUpdate(vararg repos: Repository) {
        repos.forEach { repository ->
            val old = getAll().find { it.address == repository.address }
            if (old != null) upsert(repository.copy(id = old.id))
            else insert(repository.copy(id = 0L))
        }
    }

    @Insert
    fun returnInsert(product: Repository): Long

    @Query("SELECT * FROM repository WHERE _id = :id")
    fun get(id: Long): Repository?

    @Query("SELECT * FROM repository WHERE _id = :id")
    fun getLive(id: Long): LiveData<Repository?>

    @Query("SELECT * FROM repository WHERE _id = :id")
    fun getFlow(id: Long): Flow<Repository?>

    @Query("SELECT * FROM repository ORDER BY _id ASC")
    fun getAllRepositories(): Flow<List<Repository>>

    @Query("SELECT * FROM repository ORDER BY _id ASC")
    fun getAll(): List<Repository>

    @Query("SELECT * FROM repository ORDER BY _id ASC")
    fun getAllFlow(): Flow<List<Repository>>

    @Query("SELECT * FROM repository ORDER BY _id ASC")
    fun getAllLive(): LiveData<List<Repository>>

    @Query("SELECT _id FROM repository WHERE enabled != 0 ORDER BY _id ASC")
    fun getAllEnabledIds(): List<Long>

    @Query("SELECT _id FROM repository WHERE enabled == 0 ORDER BY _id ASC")
    fun getAllDisabledIds(): List<Long>

    // TODO clean up products and other tables afterwards
    @Query("DELETE FROM repository WHERE _id = :id")
    fun deleteById(id: Long)

    @Query("SELECT MAX(_id) FROM repository")
    fun latestAddedId(): Long
}