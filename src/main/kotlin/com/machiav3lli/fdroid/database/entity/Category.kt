package com.machiav3lli.fdroid.database.entity

import androidx.room.Entity
import com.machiav3lli.fdroid.*

@Entity(
    tableName = TABLE_CATEGORY,
    primaryKeys = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME, ROW_LABEL]
)
open class Category {
    var repositoryId: Long = 0
    var packageName = ""
    var label = ""
}

@Entity(tableName = TABLE_CATEGORY_TEMP)
class CategoryTemp : Category()