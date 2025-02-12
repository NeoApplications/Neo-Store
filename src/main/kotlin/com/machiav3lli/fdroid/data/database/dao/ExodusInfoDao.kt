package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ExodusInfoDao : BaseDao<ExodusInfo> {
    @Query("SELECT * FROM exodus_info WHERE packageName = :packageName")
    fun get(packageName: String): List<ExodusInfo>

    @Query("SELECT * FROM exodus_info WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<ExodusInfo>>
}
