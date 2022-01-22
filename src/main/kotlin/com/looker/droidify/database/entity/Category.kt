package com.looker.droidify.database.entity

import androidx.room.Entity

@Entity(tableName = "category", primaryKeys = ["repository_id", "package_name", "name"])
open class Category {
    var repository_id: Long = 0
    var package_name = ""
    var name = ""
}

@Entity(tableName = "temporary_category")
class CategoryTemp : Category()