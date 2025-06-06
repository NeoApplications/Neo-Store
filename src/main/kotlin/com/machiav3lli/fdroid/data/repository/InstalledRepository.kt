package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.InstalledDao
import com.machiav3lli.fdroid.data.database.dao.ProductDao
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Section
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class InstalledRepository(
    private val productsDao: ProductDao,
    private val installedDao: InstalledDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun get(packageName: String): Flow<Installed?> = installedDao.getFlow(packageName)
        .flowOn(cc)

    fun getAll(): Flow<List<Installed>> = installedDao.getAllFlow()
        .flowOn(cc)

    suspend fun load(packageName: String): Installed? = withContext(jcc) {
        installedDao.get(packageName)
    }

    suspend fun loadInstalledProducts() = withContext(jcc) {
        productsDao.queryObject(
            installed = true,
            updates = true,
            section = Section.All,
            order = Order.NAME,
            ascending = true,
        )
    }

    suspend fun upsert(vararg installed: Installed) = withContext(jcc) {
        installedDao.upsert(*installed)
    }

    suspend fun emptyTable() = withContext(jcc) {
        installedDao.emptyTable()
    }

    suspend fun delete(packageName: String) = withContext(jcc) {
        installedDao.delete(packageName)
    }
}