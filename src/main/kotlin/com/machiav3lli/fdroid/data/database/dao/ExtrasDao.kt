package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.machiav3lli.fdroid.ROW_ALLOW_UNSTABLE
import com.machiav3lli.fdroid.ROW_FAVORITE
import com.machiav3lli.fdroid.ROW_IGNORED_VERSION
import com.machiav3lli.fdroid.ROW_IGNORE_UPDATES
import com.machiav3lli.fdroid.ROW_IGNORE_VULNS
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.data.database.entity.Extras
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtrasDao : BaseDao<Extras> {
    @Query("DELETE FROM extras WHERE $ROW_PACKAGE_NAME = :packageName")
    suspend fun delete(packageName: String)

    @Query("SELECT * FROM extras WHERE $ROW_PACKAGE_NAME = :packageName")
    suspend operator fun get(packageName: String): Extras?

    @Query("SELECT * FROM extras WHERE $ROW_PACKAGE_NAME = :packageName")
    fun getFlow(packageName: String): Flow<Extras?>

    @Query("SELECT * FROM extras")
    suspend fun getAll(): List<Extras>

    @Query("SELECT * FROM extras")
    fun getAllFlow(): Flow<List<Extras>>

    @Query("SELECT $ROW_PACKAGE_NAME FROM extras WHERE $ROW_FAVORITE != 0")
    suspend fun getFavorites(): Array<String>

    @Query("SELECT $ROW_PACKAGE_NAME FROM extras WHERE $ROW_FAVORITE != 0")
    fun getFavoritesFlow(): Flow<Array<String>>

    @Query("UPDATE extras SET $ROW_FAVORITE = :isFavorite WHERE $ROW_PACKAGE_NAME = :packageName")
    suspend fun updateFavorite(packageName: String, isFavorite: Boolean)

    @Query("UPDATE extras SET $ROW_IGNORE_VULNS = :ignoreVulns WHERE $ROW_PACKAGE_NAME = :packageName")
    suspend fun updateIgnoreVulns(packageName: String, ignoreVulns: Boolean)

    @Query("UPDATE extras SET $ROW_IGNORE_UPDATES = :ignoreUpdates WHERE $ROW_PACKAGE_NAME = :packageName")
    suspend fun updateIgnoreUpdates(packageName: String, ignoreUpdates: Boolean)

    @Query("UPDATE extras SET $ROW_ALLOW_UNSTABLE = :allowUnstable WHERE $ROW_PACKAGE_NAME = :packageName")
    suspend fun updateAllowUnstable(packageName: String, allowUnstable: Boolean)

    @Query("UPDATE extras SET $ROW_IGNORED_VERSION = :ignoredVersion WHERE $ROW_PACKAGE_NAME = :packageName")
    suspend fun updateIgnoredVersion(packageName: String, ignoredVersion: Long)

    @Transaction
    suspend fun upsertExtra(packageName: String, updateFunc: suspend ExtrasDao.(Extras?) -> Unit) {
        updateFunc(get(packageName))
    }
}