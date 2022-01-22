package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_lock")
class Lock {
    @PrimaryKey
    var package_name = ""
    var version_code = 0L
}