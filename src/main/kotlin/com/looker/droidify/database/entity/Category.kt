package com.looker.droidify.database.entity

import androidx.room.Entity
import com.looker.droidify.*

@Entity(
    tableName = TABLE_CATEGORY_NAME,
    primaryKeys = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME, ROW_LABEL]
)
open class Category {
    var repositoryId: Long = 0
    var packageName = ""
    var label = ""
}

@Entity(tableName = TABLE_CATEGORY_TEMP_NAME)
class CategoryTemp : Category()