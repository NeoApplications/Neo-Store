package com.machiav3lli.fdroid.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.fdroid.ROW_HASH
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_REPRODUCIBLE
import com.machiav3lli.fdroid.ROW_TIMESTAMP
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import com.machiav3lli.fdroid.ROW_VERSION_NAME
import com.machiav3lli.fdroid.TABLE_RB_LOG
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

@Entity(
    tableName = TABLE_RB_LOG,
    primaryKeys = [ROW_HASH, ROW_PACKAGE_NAME, ROW_TIMESTAMP],
    indices = [
        Index(value = [ROW_HASH, ROW_PACKAGE_NAME, ROW_TIMESTAMP], unique = true),
        Index(value = [ROW_PACKAGE_NAME, ROW_VERSION_CODE, ROW_REPRODUCIBLE]),
        Index(value = [ROW_PACKAGE_NAME, ROW_HASH, ROW_REPRODUCIBLE]),
        Index(value = [ROW_PACKAGE_NAME])
    ]
)
data class RBLog(
    val hash: String,
    override val repository: String,
    override val apk_url: String,
    @ColumnInfo(name = ROW_PACKAGE_NAME)
    override val appid: String,
    @ColumnInfo(name = ROW_VERSION_CODE)
    override val version_code: Int,
    @ColumnInfo(name = ROW_VERSION_NAME)
    override val version_name: String,
    override val tag: String,
    override val commit: String,
    override val timestamp: Long,
    override val reproducible: Boolean?,
    override val error: String?,
) : RBData(
    repository = repository,
    apk_url = apk_url,
    appid = appid,
    version_code = version_code,
    version_name = version_name,
    tag = tag,
    commit = commit,
    timestamp = timestamp,
    reproducible = reproducible,
    error = error
)

@Serializable
open class RBData(
    open val repository: String,
    open val apk_url: String,
    open val appid: String,
    open val version_code: Int,
    open val version_name: String,
    open val tag: String,
    open val commit: String,
    open val timestamp: Long,
    open val reproducible: Boolean?,
    open val error: String?
)

@Serializable
class RBLogs {
    companion object {
        private val jsonConfig = Json { ignoreUnknownKeys = true }
        fun fromJson(json: String) = jsonConfig.decodeFromString<Map<String, List<RBData>>>(json)

        @OptIn(ExperimentalSerializationApi::class)
        fun fromStream(inst: InputStream) =
            jsonConfig.decodeFromStream<Map<String, List<RBData>>>(inst)
    }
}
