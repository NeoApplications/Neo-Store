package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.ExodusInfoDao
import com.machiav3lli.fdroid.data.database.dao.TrackerDao
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.Tracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyRepository(
    private val trackerDao: TrackerDao,
    private val exodusDao: ExodusInfoDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun getAllTrackers() = trackerDao.getAllFlow()
        .flowOn(cc)

    fun getExodusInfos(packageName: String): Flow<List<ExodusInfo>> = exodusDao.getFlow(packageName)
        .flowOn(cc)

    suspend fun upsertTracker(vararg trackers: Tracker) {
        withContext(jcc) {
            trackerDao.upsert(*trackers)
        }
    }

    suspend fun upsertExodusInfo(vararg infos: ExodusInfo) {
        withContext(jcc) {
            exodusDao.upsert(*infos)
        }
    }
}