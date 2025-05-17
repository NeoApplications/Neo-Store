package com.machiav3lli.fdroid.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_REPOCATEGORY
import com.machiav3lli.fdroid.TABLE_REPOCATEGORY_TEMP

@Entity(
    tableName = TABLE_REPOCATEGORY,
    primaryKeys = [ROW_REPOSITORY_ID, ROW_NAME],
    indices = [
        Index(value = [ROW_NAME, ROW_LABEL]),
    ]
)
open class RepoCategory(
    var repositoryId: Long = 0,
    var name: String = "", // map key in index-v2
    var label: String = "", // name in index-v2
    var icon: String = "",
)

@Entity(tableName = TABLE_REPOCATEGORY_TEMP)
class RepoCategoryTemp(repositoryId: Long, name: String, label: String, icon: String) :
    RepoCategory(repositoryId, name, label, icon)

data class CategoryDetails(
    var name: String,
    var label: String,
    var icon: String = "",
)