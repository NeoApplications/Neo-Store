package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.DownloadedDao
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadedRepository(
    private val downloadedDao: DownloadedDao,
) {
    fun getAllFlow() = downloadedDao.getAllFlow()

    fun getLatestFlow(packageName: Flow<String>) = packageName.flatMapLatest {
        downloadedDao.getLatestFlow(it)
    }

    suspend fun update(value: Downloaded) {
        downloadedDao.upsert(value)
    }

    suspend fun delete(downloaded: Downloaded) {
        downloadedDao.delete(
            downloaded.packageName,
            downloaded.version,
            downloaded.repositoryId,
            downloaded.cacheFileName
        )
    }
}