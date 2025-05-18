package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.DownloadedDao
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadedRepository(
    private val downloadedDao: DownloadedDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun getAllFlow() = downloadedDao.getAllFlow()
        .flowOn(cc)

    fun getLatestFlow(packageName: Flow<String>) = packageName.flatMapLatest {
        downloadedDao.getLatestFlow(it)
    }.flowOn(cc)

    suspend fun update(value: Downloaded) {
        withContext(jcc) {
            downloadedDao.upsert(value)
        }
    }

    suspend fun delete(downloaded: Downloaded) {
        withContext(jcc) {
            downloadedDao.delete(
                downloaded.packageName,
                downloaded.version,
                downloaded.repositoryId,
                downloaded.cacheFileName
            )
        }
    }
}