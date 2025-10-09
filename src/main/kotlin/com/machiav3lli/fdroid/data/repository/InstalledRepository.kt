package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.InstalledDao
import com.machiav3lli.fdroid.data.database.dao.ProductDao
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Section
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class InstalledRepository(
    private val productsDao: ProductDao,
    private val installedDao: InstalledDao,
) {
    private val cc = Dispatchers.IO

    fun get(packageName: String): Flow<Installed?> = installedDao.getFlow(packageName)
        .flowOn(cc)

    fun getAll(): Flow<List<Installed>> = installedDao.getAllFlow()
        .flowOn(cc)

    suspend fun load(packageName: String): Installed? = installedDao.get(packageName)

    suspend fun loadUpdatedProducts() = productsDao.queryObject(
        installed = true,
        updates = true,
        section = Section.All,
        order = Order.NAME,
        ascending = true,
    )

    suspend fun loadListWithVulns(repoId: Long): List<EmbeddedProduct> =
        productsDao.getInstalledProductsWithVulnerabilities(repoId)

    suspend fun upsert(vararg installed: Installed) = installedDao.upsert(*installed)

    suspend fun emptyTable() = installedDao.emptyTable()

    suspend fun delete(packageName: String) = installedDao.delete(packageName)
}