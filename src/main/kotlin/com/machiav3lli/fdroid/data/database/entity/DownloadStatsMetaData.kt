package com.machiav3lli.fdroid.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.machiav3lli.fdroid.ROW_FILE_NAME
import com.machiav3lli.fdroid.ROW_LAST_MODIFIED
import com.machiav3lli.fdroid.TABLE_DOWNLOAD_STATS_FILE_METADATA

@Entity(
    tableName = TABLE_DOWNLOAD_STATS_FILE_METADATA,
    indices = [
        Index(value = [ROW_FILE_NAME], unique = true),
        Index(value = [ROW_FILE_NAME, ROW_LAST_MODIFIED]),
    ]
)
data class DownloadStatsFileMetadata(
    @PrimaryKey
    @ColumnInfo(name = ROW_FILE_NAME)
    val fileName: String,
    @ColumnInfo(name = ROW_LAST_MODIFIED)
    val lastModified: String,
    val lastFetched: Long = System.currentTimeMillis(),
    val fetchSuccess: Boolean = true,
    val fileSize: Long? = null,
    val recordsCount: Int? = null,
)