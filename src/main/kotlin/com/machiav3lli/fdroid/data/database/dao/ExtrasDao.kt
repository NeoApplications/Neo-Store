package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.machiav3lli.fdroid.data.database.entity.Extras
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtrasDao : BaseDao<Extras> {
    @Query("DELETE FROM extras WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("SELECT * FROM extras WHERE packageName = :packageName")
    operator fun get(packageName: String): Extras?

    @Query("SELECT * FROM extras WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<Extras?>

    @Query("SELECT * FROM extras")
    fun getAll(): List<Extras>

    @Query("SELECT * FROM extras")
    fun getAllFlow(): Flow<List<Extras>>

    @Query("SELECT packageName FROM extras WHERE favorite != 0")
    fun getFavorites(): Array<String>

    @Query("SELECT packageName FROM extras WHERE favorite != 0")
    fun getFavoritesFlow(): Flow<Array<String>>

    @Query("UPDATE extras SET favorite = :isFavorite WHERE packageName = :packageName")
    suspend fun updateFavorite(packageName: String, isFavorite: Boolean)

    @Query("UPDATE extras SET ignoreVulns = :ignoreVulns WHERE packageName = :packageName")
    suspend fun updateIgnoreVulns(packageName: String, ignoreVulns: Boolean)

    @Query("UPDATE extras SET ignoreUpdates = :ignoreUpdates WHERE packageName = :packageName")
    suspend fun updateIgnoreUpdates(packageName: String, ignoreUpdates: Boolean)

    @Query("UPDATE extras SET allowUnstable = :allowUnstable WHERE packageName = :packageName")
    suspend fun updateAllowUnstable(packageName: String, allowUnstable: Boolean)

    @Query("UPDATE extras SET ignoredVersion = :ignoredVersion WHERE packageName = :packageName")
    suspend fun updateIgnoredVersion(packageName: String, ignoredVersion: Long)

    @Transaction
    suspend fun upsertExtra(packageName: String, updateFunc: suspend ExtrasDao.(Extras?) -> Unit) {
        updateFunc(get(packageName))
    }
}