package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.ProductDao
import com.machiav3lli.fdroid.data.database.dao.RepositoryDao
import com.machiav3lli.fdroid.data.database.entity.LatestSyncs
import com.machiav3lli.fdroid.data.database.entity.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RepositoriesRepository(
    private val productsDao: ProductDao,
    private val reposDao: RepositoryDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun getById(repoId: Long): Flow<Repository?> = reposDao.getFlow(repoId)
        .flowOn(cc)

    fun getAll(): Flow<List<Repository>> = reposDao.getAllFlow()
        .flowOn(cc)

    fun getLatestUpdates(): Flow<LatestSyncs> = reposDao.latestUpdatesFlow()
        .flowOn(cc)

    fun productsCount(repoId: Long): Flow<Long> = productsDao.countForRepositoryFlow(repoId)
        .flowOn(cc)

    suspend fun load(repoId: Long): Repository? = withContext(jcc) {
        reposDao.get(repoId)
    }

    suspend fun loadAll(): List<Repository> = withContext(jcc) {
        reposDao.getAll()
    }

    suspend fun upsert(repo: Repository) = withContext(jcc) {
        reposDao.put(repo)
    }

    suspend fun insertReturn(repository: Repository) = withContext(jcc) {
        reposDao.insertReturn(repository)
    }

    suspend fun insertOrUpdate(vararg repository: Repository) = withContext(jcc) {
        reposDao.insertOrUpdate(*repository)
    }

    suspend fun deleteById(repoId: Long) = withContext(jcc) {
        reposDao.deleteById(repoId)
    }
}