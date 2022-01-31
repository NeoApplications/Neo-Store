package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_installed")
data class Installed(
    @PrimaryKey
    var package_name: String = "",
    var version: String = "",
    var version_code: Long = 0L,
    var signature: String = ""
)