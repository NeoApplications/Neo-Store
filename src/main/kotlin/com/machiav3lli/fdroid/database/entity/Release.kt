package com.machiav3lli.fdroid.database.entity

import android.net.Uri
import androidx.room.Entity
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_SIGNATURE
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import com.machiav3lli.fdroid.TABLE_RELEASE
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO make a Room entity
@Entity(
    tableName = TABLE_RELEASE,
    primaryKeys = [ROW_PACKAGE_NAME, ROW_VERSION_CODE, ROW_SIGNATURE]
)
@Serializable
data class Release(
    val packageName: String,
    val selected: Boolean,
    val version: String,
    val versionCode: Long,
    val added: Long,
    val size: Long,
    val minSdkVersion: Int,
    val targetSdkVersion: Int,
    val maxSdkVersion: Int,
    val source: String,
    val release: String,
    val hash: String,
    val hashType: String,
    val signature: String,
    val obbMain: String,
    val obbMainHash: String,
    val obbMainHashType: String,
    val obbPatch: String,
    val obbPatchHash: String,
    val obbPatchHashType: String,
    val permissions: List<String>,
    val features: List<String>,
    val platforms: List<String>,
    val incompatibilities: List<Incompatibility>,
) {
    @Serializable
    sealed class Incompatibility {
        @Serializable
        object MinSdk : Incompatibility()

        @Serializable
        object MaxSdk : Incompatibility()

        @Serializable
        object Platform : Incompatibility()

        @Serializable
        data class Feature(val feature: String) : Incompatibility()

        fun toJSON() = Json.encodeToString(this)

        companion object {
            fun fromJson(json: String) = Json.decodeFromString<Incompatibility>(json)
        }
    }

    val identifier: String
        get() = "$versionCode.$hash"

    fun getDownloadUrl(repository: Repository): String {
        return Uri.parse(repository.address).buildUpon().appendPath(release).build().toString()
    }

    val cacheFileName: String
        get() = "${packageName}_${hash.replace('/', '-')}.apk"

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Release>(json)
    }
}
