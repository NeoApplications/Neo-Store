package com.machiav3lli.fdroid.data.database.entity

import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.fdroid.ROW_DESCRIPTION
import com.machiav3lli.fdroid.ROW_ICON
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_ANTIFEATURE
import com.machiav3lli.fdroid.TABLE_ANTIFEATURE_TEMP

@Entity(
    tableName = TABLE_ANTIFEATURE,
    primaryKeys = [ROW_REPOSITORY_ID, ROW_NAME],
    indices = [
        Index(value = [ROW_NAME, ROW_LABEL]),
        Index(value = [ROW_NAME, ROW_LABEL, ROW_DESCRIPTION, ROW_ICON]),
        Index(value = [ROW_NAME]),
        Index(value = [ROW_REPOSITORY_ID]),
    ]
)
open class AntiFeature(
    var repositoryId: Long = 0,
    var name: String = "", // map key in index-v2
    var label: String = "", // name in index-v2
    var description: String = "",
    var icon: String = "",
)

@Entity(tableName = TABLE_ANTIFEATURE_TEMP)
class AntiFeatureTemp(
    repositoryId: Long,
    name: String,
    label: String,
    description: String,
    icon: String
) : AntiFeature(repositoryId, name, label, description, icon)

data class AntiFeatureDetails(
    var name: String,
    var label: String,
    var description: String = "",
    var icon: String = "",
)