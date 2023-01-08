package com.machiav3lli.fdroid.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.machiav3lli.fdroid.database.entity.Repository
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao : BaseDao<Repository> {
    @get:Query("SELECT COUNT(_id) FROM repository")
    val count: Int

    fun put(repository: Repository): Repository {
        repository.let { item ->
            val newId = if (item.id > 0L) update(item).toLong() else returnInsert(item)
            return if (newId != repository.id) repository.copy(id = newId) else repository
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

    @get:Query("SELECT * FROM repository ORDER BY _id ASC")
    val all: List<Repository>

    @get:Query("SELECT * FROM repository ORDER BY _id ASC")
    val allFlow: Flow<List<Repository>>

    @get:Query("SELECT * FROM repository ORDER BY _id ASC")
    val allLive: LiveData<List<Repository>>

    @get:Query("SELECT _id FROM repository WHERE enabled == 0 ORDER BY _id ASC")
    val allDisabled: List<Long>

    // TODO clean up products and other tables afterwards
    @Query("DELETE FROM repository WHERE _id = :id")
    fun deleteById(id: Long)

    @Query("SELECT MAX(_id) FROM repository")
    fun latestAddedId(): Long
}