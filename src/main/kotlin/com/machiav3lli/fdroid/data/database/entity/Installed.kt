package com.machiav3lli.fdroid.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.TABLE_INSTALLED

@Entity(
    tableName = TABLE_INSTALLED,
    indices = [
        Index(value = [ROW_PACKAGE_NAME], unique = true)
    ]
)
data class Installed(
    @PrimaryKey
    var packageName: String = "",
    var version: String = "",
    var versionCode: Long = 0L,
    @ColumnInfo(defaultValue = "[]")
    val signatures: List<String> = emptyList(),
    var isSystem: Boolean = false,
    val launcherActivities: List<Pair<String, String>> = emptyList()
)