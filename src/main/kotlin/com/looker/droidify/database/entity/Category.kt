package com.looker.droidify.database.entity

import androidx.room.Entity

@Entity(tableName = "category", primaryKeys = ["repositoryId", "packageName", "label"])
open class Category {
    var repositoryId: Long = 0
    var packageName = ""
    var label = ""
}

@Entity(tableName = "temporary_category")
class CategoryTemp : Category()