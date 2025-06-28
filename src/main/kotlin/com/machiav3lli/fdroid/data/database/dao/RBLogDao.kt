package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.data.database.entity.RBLog
import kotlinx.coroutines.flow.Flow

@Dao
interface RBLogDao : BaseDao<RBLog> {
    @Query("SELECT * FROM rb_log WHERE packageName = :packageName")
    fun get(packageName: String): List<RBLog>

    @Query("SELECT * FROM rb_log WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<RBLog>>
}
