package com.machiav3lli.fdroid.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_CATEGORY
import com.machiav3lli.fdroid.TABLE_CATEGORY_TEMP

@Entity(
    tableName = TABLE_CATEGORY,
    primaryKeys = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME, ROW_LABEL],
    indices = [
        Index(value = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME, ROW_LABEL], unique = true),
        Index(value = [ROW_PACKAGE_NAME, ROW_LABEL])
    ]
)
open class Category {
    var repositoryId: Long = 0
    var packageName = ""
    var label = ""
}

@Entity(tableName = TABLE_CATEGORY_TEMP)
class CategoryTemp : Category()