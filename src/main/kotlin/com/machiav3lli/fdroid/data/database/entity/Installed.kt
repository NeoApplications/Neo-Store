package com.machiav3lli.fdroid.data.database.entity

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
    var signature: String = "",
    var isSystem: Boolean = false,
    val launcherActivities: List<Pair<String, String>> = emptyList()
)