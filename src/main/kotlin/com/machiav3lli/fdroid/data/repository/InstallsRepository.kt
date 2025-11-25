package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.InstallTaskDao
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import kotlinx.coroutines.flow.Flow

class InstallsRepository(
    private val installTaskDao: InstallTaskDao,
) {
    fun get(packageName: String): Flow<InstallTask?> = installTaskDao.getFlow(packageName)

    fun getAll(): Flow<List<InstallTask>> = installTaskDao.getAllFlow()

    suspend fun load(packageName: String): InstallTask? = installTaskDao.get(packageName)

    suspend fun loadAll(): List<InstallTask> = installTaskDao.getAll()

    suspend fun upsert(vararg installed: InstallTask) = installTaskDao.upsert(*installed)

    suspend fun emptyTable() = installTaskDao.emptyTable()

    suspend fun delete(packageName: String) = installTaskDao.delete(packageName)
}