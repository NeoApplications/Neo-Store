package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.AntiFeatureDao
import com.machiav3lli.fdroid.data.database.dao.ProductDao
import com.machiav3lli.fdroid.data.database.dao.RepositoryDao
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.LatestSyncs
import com.machiav3lli.fdroid.data.database.entity.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class RepositoriesRepository(
    private val productsDao: ProductDao,
    private val reposDao: RepositoryDao,
    private val antiFeatureDao: AntiFeatureDao,
) {
    private val cc = Dispatchers.IO

    fun getById(repoId: Long): Flow<Repository?> = reposDao.getFlow(repoId)
        .flowOn(cc)

    fun getAll(): Flow<List<Repository>> = reposDao.getAllFlow()
        .flowOn(cc)

    fun getAllEnabled(): Flow<List<Repository>> = reposDao.getAllEnabledFlow()
        .flowOn(cc)

    fun getLatestUpdates(): Flow<LatestSyncs> = reposDao.latestUpdatesFlow()
        .flowOn(cc)

    fun productsCount(repoId: Long): Flow<Long> = productsDao.countForRepositoryFlow(repoId)
        .flowOn(cc)

    fun getRepoAntiFeatures(): Flow<List<AntiFeatureDetails>> =
        antiFeatureDao.getAllAntiFeatureDetailsFlow()
            .flowOn(cc)

    suspend fun load(repoId: Long): Repository? = reposDao.get(repoId)

    suspend fun loadAll(): List<Repository> = reposDao.getAll()

    suspend fun upsert(repo: Repository) = reposDao.put(repo)

    suspend fun insertReturn(repository: Repository) = reposDao.insertReturn(repository)

    suspend fun insertOrUpdate(vararg repository: Repository) = reposDao.insertOrUpdate(*repository)

    suspend fun deleteById(repoId: Long) = reposDao.deleteById(repoId)
}