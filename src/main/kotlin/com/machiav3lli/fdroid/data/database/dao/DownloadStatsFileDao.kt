package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Transaction
import com.machiav3lli.fdroid.ROW_FILE_NAME
import com.machiav3lli.fdroid.ROW_LAST_MODIFIED
import com.machiav3lli.fdroid.data.database.entity.DownloadStatsFileMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadStatsFileDao : BaseDao<DownloadStatsFileMetadata> {

    @Query("SELECT * FROM ds_file_meta_data ORDER BY fileName")
    fun getAllFlow(): Flow<List<DownloadStatsFileMetadata>>

    @Query("SELECT * FROM ds_file_meta_data ORDER BY fileName")
    suspend fun getAll(): List<DownloadStatsFileMetadata>

    @Query("SELECT $ROW_FILE_NAME, $ROW_LAST_MODIFIED FROM ds_file_meta_data WHERE fetchSuccess = 1")
    suspend fun getLastModifiedDates(): Map<
            @MapColumn(columnName = ROW_FILE_NAME) String,
            @MapColumn(columnName = ROW_LAST_MODIFIED) String
            >

    @Transaction
    suspend fun multipleUpserts(updates: List<DownloadStatsFileMetadata>) {
        updates.forEach { metadata ->
            upsert(metadata)
        }
    }

    @Query("DELETE FROM ds_file_meta_data WHERE $ROW_FILE_NAME NOT IN (:activeFileNames)")
    suspend fun deleteObsoleteFiles(activeFileNames: List<String>)

    @Query("DELETE FROM ds_file_meta_data")
    suspend fun emptyTable()
}