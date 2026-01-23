package com.machiav3lli.fdroid.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.fdroid.FIELD_CACHEFILENAME
import com.machiav3lli.fdroid.FIELD_VERSION
import com.machiav3lli.fdroid.ROW_CHANGED
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.TABLE_DOWNLOADED
import com.machiav3lli.fdroid.data.entity.DownloadState

@Entity(
    tableName = TABLE_DOWNLOADED,
    primaryKeys = [ROW_PACKAGE_NAME, FIELD_VERSION, FIELD_CACHEFILENAME],
    indices = [
        Index(value = [ROW_PACKAGE_NAME, FIELD_VERSION, FIELD_CACHEFILENAME], unique = true),
        Index(value = [ROW_PACKAGE_NAME]),
        Index(value = [ROW_CHANGED]),
    ]
)
data class Downloaded(
    val packageName: String = "",
    @ColumnInfo(defaultValue = "")
    val label: String = "",
    val version: String = "",
    @ColumnInfo(defaultValue = "0")
    val repositoryId: Long = 0L,
    val cacheFileName: String = "",
    val changed: Long = 0L,
    val state: DownloadState,
) {
    val itemKey: String
        get() = "$packageName-$repositoryId-$version-$cacheFileName"
}