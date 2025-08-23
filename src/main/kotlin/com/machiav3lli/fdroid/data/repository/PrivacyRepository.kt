package com.machiav3lli.fdroid.data.repository

import com.machiav3lli.fdroid.data.database.dao.DownloadStatsDao
import com.machiav3lli.fdroid.data.database.dao.ExodusInfoDao
import com.machiav3lli.fdroid.data.database.dao.RBLogDao
import com.machiav3lli.fdroid.data.database.dao.TrackerDao
import com.machiav3lli.fdroid.data.database.entity.DownloadStats
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.RBLog
import com.machiav3lli.fdroid.data.database.entity.Tracker
import com.machiav3lli.fdroid.manager.network.DownloadStatsAPI
import com.machiav3lli.fdroid.manager.network.RBAPI
import com.machiav3lli.fdroid.manager.network.RExodusAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyRepository(
    private val trackerDao: TrackerDao,
    private val rbDao: RBLogDao,
    private val exodusDao: ExodusInfoDao,
    private val downloadStatsDao: DownloadStatsDao,
) {
    private val cc = Dispatchers.IO
    private val jcc = Dispatchers.IO + SupervisorJob()

    fun getAllTrackers() = trackerDao.getAllFlow()
        .flowOn(cc)

    fun getExodusInfos(packageName: String): Flow<List<ExodusInfo>> = exodusDao.getFlow(packageName)
        .flowOn(cc)

    fun getRBLogs(packageName: String): Flow<List<RBLog>> = rbDao.getFlow(packageName)
        .flowOn(cc)

    fun getDownloadStats(packageName: String): Flow<List<DownloadStats>> =
        downloadStatsDao.getFlow(packageName)
            .flowOn(cc)

    suspend fun upsertTracker(vararg trackers: Tracker) {
        withContext(jcc) {
            trackerDao.upsert(*trackers)
        }
    }

    suspend fun upsertRBLogs(vararg logs: RBLog) {
        withContext(jcc) {
            rbDao.upsert(*logs)
        }
    }

    suspend fun upsertExodusInfo(vararg infos: ExodusInfo) {
        withContext(jcc) {
            exodusDao.upsert(*infos)
        }
    }

    suspend fun upsertDownloadStats(vararg downloadStats: DownloadStats) {
        withContext(jcc) {
            downloadStatsDao.upsert(*downloadStats)
        }
    }
}

val privacyModule = module {
    singleOf(::RExodusAPI)
    singleOf(::RBAPI)
    singleOf(::DownloadStatsAPI)
}