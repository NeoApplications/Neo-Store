package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.LatestSyncs
import com.machiav3lli.fdroid.data.database.entity.Repository
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao : BaseDao<Repository> {
    @Query("SELECT COUNT(id) FROM repository")
    fun getCount(): Int

    @Insert
    fun insertReturn(repo: Repository): Long

    suspend fun put(vararg repository: Repository) {
        val (toUpdate, toInsert) = repository.partition { it.id > 0L }
        insert(*toInsert.toTypedArray())
        update(*toUpdate.toTypedArray())
    }

    suspend fun insertOrUpdate(vararg repos: Repository) {
        val toUpsert = mutableSetOf<Repository>()
        val toInsert = mutableSetOf<Repository>()
        repos.forEach { repository ->
            val old = getByAddress(repository.address)
            if (old != null) toUpsert.add(repository.copy(id = old.id))
            else toInsert.add(repository.copy(id = 0L))
        }
        insert(*toInsert.toTypedArray())
        upsert(*toUpsert.toTypedArray())
    }

    @Query("SELECT * FROM repository WHERE id = :id")
    fun get(id: Long): Repository?

    @Query("SELECT * FROM repository WHERE id = :id")
    fun getFlow(id: Long): Flow<Repository?>

    @Query("SELECT * FROM repository ORDER BY id ASC")
    fun getAll(): List<Repository>

    @Query("SELECT * FROM repository ORDER BY id ASC")
    fun getAllFlow(): Flow<List<Repository>>

    @Query("SELECT * FROM repository WHERE address = :address")
    suspend fun getByAddress(address: String): Repository?

    @Query("SELECT name FROM repository WHERE id = :id")
    fun getRepoName(id: Long): String

    @Query("SELECT id FROM repository WHERE enabled != 0 ORDER BY id ASC")
    fun getAllEnabledIds(): List<Long>

    @Query("SELECT id FROM repository WHERE enabled == 0 ORDER BY id ASC")
    fun getAllDisabledIds(): List<Long>

    @Query("UPDATE repository SET lastModified = '', entityTag = ''")
    suspend fun forgetLastModifications()

    // TODO clean up products and other tables afterwards
    @Query("DELETE FROM repository WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM repository WHERE address = :address")
    suspend fun deleteByAddress(address: String)

    @Query("SELECT MAX(id) FROM repository")
    fun latestAddedId(): Long

    @Query("SELECT MAX(updated) AS latest, MIN(updated) AS latestAll FROM repository WHERE enabled != 0")
    fun latestUpdatesFlow(): Flow<LatestSyncs>

    @Query("DELETE FROM repository")
    suspend fun emptyTable()
}