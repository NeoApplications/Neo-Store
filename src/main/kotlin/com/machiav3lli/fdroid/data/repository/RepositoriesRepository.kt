package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.AntiFeatureDao
import com.machiav3lli.fdroid.data.database.dao.ProductDao
import com.machiav3lli.fdroid.data.database.dao.RepositoryDao
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.LatestSyncs
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.AntiFeature
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

class RepositoriesRepository(
    private val productsDao: ProductDao,
    private val reposDao: RepositoryDao,
    private val antiFeatureDao: AntiFeatureDao,
) {
    fun getById(repoId: Long): Flow<Repository?> = reposDao.getFlow(repoId)

    fun getAll(): Flow<List<Repository>> = reposDao.getAllFlow()

    fun getAllEnabled(): Flow<List<Repository>> = reposDao.getAllEnabledFlow()

    fun getLatestUpdates(): Flow<LatestSyncs> = reposDao.latestUpdatesFlow()

    fun productsCount(repoId: Long): Flow<Long> = productsDao.countForRepositoryFlow(repoId)

    fun getRepoAntiFeatures(): Flow<List<AntiFeatureDetails>> =
        antiFeatureDao.getAllAntiFeatureDetailsFlow()

    fun getRepoAntiFeaturesMap(): Flow<Map<String, AntiFeatureDetails>> =
        antiFeatureDao.getAllAntiFeatureDetailsFlow()
            .mapLatest {
                it.associateBy(AntiFeatureDetails::name)
            }

    fun getRepoAntiFeaturePairs(): Flow<List<Pair<String, String>>> =
        antiFeatureDao.getAllAntiFeatureDetailsFlow()
            .mapLatest { afs ->
                val detailsMap = afs.associateBy(AntiFeatureDetails::name)
                val enumMap = AntiFeature.entries.associateBy(AntiFeature::key)
                (detailsMap.keys + enumMap.keys).map { name ->
                    detailsMap[name]?.let { Pair(it.name, it.label) } ?: Pair(name, "")
                }
            }

    suspend fun load(repoId: Long): Repository? = reposDao.get(repoId)

    suspend fun loadAll(): List<Repository> = reposDao.getAll()

    suspend fun upsert(repo: Repository) = reposDao.put(repo)

    suspend fun insertReturn(repository: Repository) = reposDao.insertReturn(repository)

    suspend fun insertOrUpdate(vararg repository: Repository) = reposDao.insertOrUpdate(*repository)

    suspend fun deleteById(repoId: Long) = reposDao.deleteById(repoId)
}