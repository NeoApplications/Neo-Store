package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.InstalledDao
import com.machiav3lli.fdroid.data.database.dao.ProductDao
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Section
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class InstalledRepository(
    private val productsDao: ProductDao,
    private val installedDao: InstalledDao,
) {
    fun get(packageName: String): Flow<Installed?> = installedDao.getFlow(packageName)

    fun getAll(): Flow<List<Installed>> = installedDao.getAllFlow()

    fun getMap(): Flow<Map<String, Installed>> = installedDao.getAllFlow()
        .map { it.associateBy(Installed::packageName) }

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