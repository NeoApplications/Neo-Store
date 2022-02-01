package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_lock")
data class Lock(
    @PrimaryKey
    var package_name: String = "",
    var version_code: Long = 0L
)