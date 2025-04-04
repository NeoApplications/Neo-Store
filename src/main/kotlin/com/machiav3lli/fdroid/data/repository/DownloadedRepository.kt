package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.DatabaseX
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
    private val db: DatabaseX
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun getAllFlow() = db.getDownloadedDao().getAllFlow()
        .flowOn(cc)

    fun getLatestFlow(packageName: Flow<String>) = packageName.flatMapLatest {
        db.getDownloadedDao().getLatestFlow(it)
    }.flowOn(cc)

    suspend fun update(value: Downloaded) {
        withContext(jcc) {
            db.getDownloadedDao().upsert(value)
        }
    }

    suspend fun delete(downloaded: Downloaded) {
        withContext(jcc) {
            db.getDownloadedDao().delete(
                downloaded.packageName,
                downloaded.version,
                downloaded.repositoryId,
                downloaded.cacheFileName
            )
        }
    }
}