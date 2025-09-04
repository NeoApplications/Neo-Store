package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.CategoryDao
import com.machiav3lli.fdroid.data.database.dao.DownloadStatsDao
import com.machiav3lli.fdroid.data.database.dao.ProductDao
import com.machiav3lli.fdroid.data.database.dao.RepoCategoryDao
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.IconDetails
import com.machiav3lli.fdroid.data.database.entity.Licenses
import com.machiav3lli.fdroid.data.database.entity.PackageSum
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.entity.Section
import com.machiav3lli.fdroid.utils.extension.text.getIsoDateOfMonthsAgo
import com.machiav3lli.fdroid.utils.extension.text.isoDateToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsRepository(
    private val productsDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val repoCategoryDao: RepoCategoryDao,
    private val downloadStatsDao: DownloadStatsDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    suspend fun upsertProduct(vararg product: Product) = withContext(jcc) {
        productsDao.upsert(*product)
    }

    fun getProducts(req: Request): Flow<List<EmbeddedProduct>> = productsDao.queryFlowList(req)
        .flowOn(cc)

    fun getProduct(packageName: String): Flow<List<EmbeddedProduct>> =
        productsDao.getFlow(packageName)
            .flowOn(cc)

    fun getProductsOfRepo(repoId: Long): Flow<List<EmbeddedProduct>> =
        productsDao.productsForRepositoryFlow(repoId)
            .flowOn(cc)

    fun getSpecificProducts(pkgs: Set<String>): Flow<List<EmbeddedProduct>> =
        productsDao.queryFlowOfPackages(pkgs)
            .flowOn(cc)

    fun getRecentTopApps(client : String): Flow<List<PackageSum>> =
        downloadStatsDao.getFlowRecentTopApps(getIsoDateOfMonthsAgo(3).isoDateToInt(), 50, client)
            .flowOn(cc)

    fun getAllTimeTopApps(): Flow<List<PackageSum>> =
        downloadStatsDao.getFlowRecentTopApps(getIsoDateOfMonthsAgo(100).isoDateToInt(), 50)
            .flowOn(cc)

    fun getAuthorList(author: String): Flow<List<EmbeddedProduct>> =
        productsDao.getAuthorPackagesFlow(author)
            .flowOn(cc)

    fun getAllLicenses(): Flow<List<Licenses>> = productsDao.getAllLicensesFlow()
        .flowOn(cc)

    fun getAllCategories(): Flow<List<String>> = categoryDao.getAllNamesFlow()
        .flowOn(cc)

    fun getAllCategoryDetails(): Flow<List<CategoryDetails>> =
        repoCategoryDao.getAllCategoryDetailsFlow()
            .flowOn(cc)

    fun getIconDetails(): Flow<List<IconDetails>> = productsDao.getIconDetailsFlow()
        .flowOn(cc)

    suspend fun loadList(
        installed: Boolean,
        updates: Boolean,
        section: Section,
        order: Order,
        ascending: Boolean
    ): List<EmbeddedProduct> = withContext(jcc) {
        productsDao.queryObject(
            installed = installed,
            updates = updates,
            section = section,
            order = order,
            ascending = ascending,
        )
    }

    suspend fun loadProduct(packageName: String): List<EmbeddedProduct> = withContext(jcc) {
        productsDao.get(packageName)
    }

    suspend fun productExists(packageName: String): Boolean = withContext(jcc) {
        productsDao.exists(packageName)
    }
}