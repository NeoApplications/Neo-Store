package com.machiav3lli.fdroid.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.fdroid.ROW_NAME
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_CATEGORY
import com.machiav3lli.fdroid.TABLE_CATEGORY_TEMP

@Entity(
    tableName = TABLE_CATEGORY,
    primaryKeys = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME, ROW_NAME],
    indices = [
        Index(value = [ROW_REPOSITORY_ID, ROW_PACKAGE_NAME, ROW_NAME], unique = true),
        Index(value = [ROW_PACKAGE_NAME, ROW_NAME])
    ]
)
open class Category(
    val repositoryId: Long = 0,
    val packageName: String = "",
    val name: String = "",
)

@Entity(tableName = TABLE_CATEGORY_TEMP)
class CategoryTemp(repositoryId: Long, packageName: String, name: String) :
    Category(repositoryId, packageName, name)