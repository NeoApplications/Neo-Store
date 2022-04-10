package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.looker.droidify.TABLE_IGNORED_NAME

// TODO complete renaming to Ignored
@Entity(tableName = TABLE_IGNORED_NAME)
data class Ignored(
    @PrimaryKey
    var packageName: String = "",
    var versionCode: Long = 0L
)