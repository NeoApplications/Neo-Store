package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_installed")
data class Installed(
    @PrimaryKey
    var packageName: String = "",
    var version: String = "",
    var versionCode: Long = 0L,
    var signature: String = ""
)