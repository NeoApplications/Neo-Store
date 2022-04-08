package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.looker.droidify.TABLE_INSTALLED_NAME

@Entity(tableName = TABLE_INSTALLED_NAME)
data class Installed(
    @PrimaryKey
    var packageName: String = "",
    var version: String = "",
    var versionCode: Long = 0L,
    var signature: String = ""
)