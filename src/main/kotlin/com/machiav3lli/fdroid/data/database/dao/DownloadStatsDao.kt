package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.DownloadStats
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadStatsDao : BaseDao<DownloadStats> {
    @Query("SELECT * FROM download_stats WHERE packageName = :packageName")
    fun get(packageName: String): List<DownloadStats>

    @Query("SELECT * FROM download_stats WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<DownloadStats>>
}
