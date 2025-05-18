package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.ExtrasDao
import com.machiav3lli.fdroid.data.database.entity.Extras
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class ExtrasRepository(
    private val extrasDao: ExtrasDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun get(packageName: String): Flow<Extras?> = extrasDao.getFlow(packageName)
        .flowOn(cc)

    fun getAll(): Flow<List<Extras>> = extrasDao.getAllFlow()
        .flowOn(cc)

    fun getAllFavorites(): Flow<Array<String>> = extrasDao.getFavoritesFlow()
        .flowOn(cc)

    suspend fun load(packageName: String): Extras? = withContext(jcc) {
        extrasDao[packageName]
    }

    suspend fun upsert(vararg extras: Extras) = withContext(jcc) {
        extrasDao.upsert(*extras)
    }

    suspend fun upsertExtra(packageName: String, updateFunc: suspend ExtrasDao.(Extras?) -> Unit) =
        withContext(jcc) {
            extrasDao.upsertExtra(packageName, updateFunc)
        }

    suspend fun setIgnoredVersion(packageName: String, versionCode: Long) =
        upsertExtra(packageName) {
            if (it != null) updateIgnoredVersion(packageName, versionCode)
            else insert(Extras(packageName, ignoredVersion = versionCode))
        }

    suspend fun setIgnoreUpdates(packageName: String, setBoolean: Boolean) =
        upsertExtra(packageName) {
            if (it != null) updateIgnoreUpdates(packageName, setBoolean)
            else insert(Extras(packageName, ignoreUpdates = setBoolean))
        }

    suspend fun setIgnoreVulns(packageName: String, setBoolean: Boolean) =
        upsertExtra(packageName) {
            if (it != null) updateIgnoreVulns(packageName, setBoolean)
            else insert(Extras(packageName, ignoreVulns = setBoolean))

        }

    suspend fun setFavorite(packageName: String, setBoolean: Boolean) =
        upsertExtra(packageName) {
            if (it != null) updateFavorite(packageName, setBoolean)
            else insert(Extras(packageName, favorite = setBoolean))
        }
}