package com.machiav3lli.fdroid.database.entity

import androidx.room.Entity
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
@Entity(
    primaryKeys = [ROW_PACKAGE_NAME, ROW_REPOSITORY_ID, ROW_VERSION_CODE],
)
data class InstallTask(
    val packageName: String,
    val repositoryId: Long,
    val versionCode: Long,
    val versionName: String,
    val label: String,
    val cacheFileName: String,
    val added: Long,
    val requireUser: Boolean,
) {
    val key: String
        get() = "$packageName-$repositoryId-$versionName"

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<InstallTask>(json)
    }
}