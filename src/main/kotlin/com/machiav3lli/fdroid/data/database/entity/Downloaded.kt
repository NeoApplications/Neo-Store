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
    var packageName: String = "",
    var version: String = "",
    @ColumnInfo(defaultValue = "0")
    var repositoryId: Long = 0L,
    var cacheFileName: String = "",
    var changed: Long = 0L,
    var state: DownloadState,
) {
    val itemKey: String
        get() = "$packageName-$repositoryId-$version-$cacheFileName"
}