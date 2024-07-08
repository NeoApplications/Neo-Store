package com.machiav3lli.fdroid.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.machiav3lli.fdroid.TABLE_INSTALLED

@Entity(tableName = TABLE_INSTALLED)
data class Installed(
    @PrimaryKey
    @ColumnInfo(index = true)
    var packageName: String = "",
    var version: String = "",
    var versionCode: Long = 0L,
    var signature: String = "",
    var isSystem: Boolean = false,
    val launcherActivities: List<Pair<String, String>> = emptyList()
)