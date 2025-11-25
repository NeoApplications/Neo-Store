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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsRepository(
    private val productsDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val repoCategoryDao: RepoCategoryDao,
    private val downloadStatsDao: DownloadStatsDao,
) {
    suspend fun upsertProduct(vararg product: Product) = productsDao.upsert(*product)

    fun getProducts(req: Request): Flow<List<EmbeddedProduct>> = productsDao.queryFlowList(req)

    fun getProduct(packageName: String): Flow<List<EmbeddedProduct>> =
        productsDao.getFlow(packageName)

    fun getProductsOfRepo(repoId: Long): Flow<List<EmbeddedProduct>> =
        productsDao.productsForRepositoryFlow(repoId)

    fun getSpecificProducts(pkgs: Set<String>): Flow<List<EmbeddedProduct>> =
        productsDao.queryFlowOfPackages(pkgs)

    fun getRecentTopApps(client: String, numMonths: Int = 3): Flow<List<PackageSum>> =
        downloadStatsDao.getFlowRecentTopApps(
            getIsoDateOfMonthsAgo(numMonths).isoDateToInt(),
            50,
            client
        )

    fun getAllTimeTopApps(): Flow<List<PackageSum>> =
        downloadStatsDao.getFlowRecentTopApps(getIsoDateOfMonthsAgo(100).isoDateToInt(), 50)

    fun getAuthorList(author: String): Flow<List<EmbeddedProduct>> =
        productsDao.getAuthorPackagesFlow(author)

    fun getAllLicenses(): Flow<List<Licenses>> = productsDao.getAllLicensesFlow()

    fun getAllLicensesDistinct(): Flow<List<String>> = productsDao.getAllLicensesFlow()
        .mapLatest {
            it.map(Licenses::licenses).flatten().distinct()
        }

    fun getAllCategories(): Flow<List<String>> = categoryDao.getAllNamesFlow()

    fun getAllCategoryDetails(): Flow<List<CategoryDetails>> =
        repoCategoryDao.getAllCategoryDetailsFlow()

    fun getIconDetails(): Flow<List<IconDetails>> = productsDao.getIconDetailsFlow()

    fun getIconDetailsMap(): Flow<Map<String, IconDetails>> = productsDao.getIconDetailsFlow()
        .mapLatest { it.associateBy(IconDetails::packageName) }

    suspend fun loadList(
        installed: Boolean,
        updates: Boolean,
        section: Section,
        order: Order,
        ascending: Boolean
    ): List<EmbeddedProduct> = productsDao.queryObject(
        installed = installed,
        updates = updates,
        section = section,
        order = order,
        ascending = ascending,
    )

    suspend fun loadProduct(packageName: String): List<EmbeddedProduct> =
        productsDao.get(packageName)

    suspend fun productExists(packageName: String): Boolean = productsDao.exists(packageName)
}