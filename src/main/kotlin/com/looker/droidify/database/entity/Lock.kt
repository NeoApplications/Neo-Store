package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_lock")
data class Lock(
    @PrimaryKey
    var packageName: String = "",
    var versionCode: Long = 0L
)