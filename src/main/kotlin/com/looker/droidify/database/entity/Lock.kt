package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.looker.droidify.TABLE_LOCK_NAME

@Entity(tableName = TABLE_LOCK_NAME)
data class Lock(
    @PrimaryKey
    var packageName: String = "",
    var versionCode: Long = 0L
)