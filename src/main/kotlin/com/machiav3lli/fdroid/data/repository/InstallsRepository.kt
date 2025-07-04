package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.InstallTaskDao
import com.machiav3lli.fdroid.data.database.entity.InstallTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class InstallsRepository(
    private val installTaskDao: InstallTaskDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun get(packageName: String): Flow<InstallTask?> = installTaskDao.getFlow(packageName)
        .flowOn(cc)

    fun getAll(): Flow<List<InstallTask>> = installTaskDao.getAllFlow()
        .flowOn(cc)

    suspend fun load(packageName: String): InstallTask? = withContext(jcc) {
        installTaskDao.get(packageName)
    }

    suspend fun loadAll(): List<InstallTask> = withContext(jcc) {
        installTaskDao.getAll()
    }

    suspend fun upsert(vararg installed: InstallTask) = withContext(jcc) {
        installTaskDao.upsert(*installed)
    }

    suspend fun emptyTable() = withContext(jcc) {
        installTaskDao.emptyTable()
    }

    suspend fun delete(packageName: String) = withContext(jcc) {
        installTaskDao.delete(packageName)
    }
}