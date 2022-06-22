package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.looker.droidify.TABLE_EXTRAS_NAME

@Entity(tableName = TABLE_EXTRAS_NAME)
data class Extras(
    @PrimaryKey
    var packageName: String = "",
    var favorite: Boolean = false,
    var ignoreUpdates: Boolean = false,
    var ignoredVersion: Long = 0L,
)