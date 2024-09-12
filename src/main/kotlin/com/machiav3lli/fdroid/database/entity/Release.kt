package com.machiav3lli.fdroid.database.entity

import android.net.Uri
import androidx.room.Entity
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_SIGNATURE
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import com.machiav3lli.fdroid.TABLE_RELEASE
import com.machiav3lli.fdroid.TABLE_RELEASE_TEMP
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// TODO add repoID, use for queries
@Entity(
    tableName = TABLE_RELEASE,
    primaryKeys = [ROW_PACKAGE_NAME, ROW_VERSION_CODE, ROW_SIGNATURE]
)
@Serializable
open class Release(
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
        data object MinSdk : Incompatibility()

        @Serializable
        data object MaxSdk : Incompatibility()

        @Serializable
        data object Platform : Incompatibility()

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

    fun copy(
        incompatibilities: List<Incompatibility>,
        selected: Boolean,
    ) = Release(
        packageName,
        selected,
        version,
        versionCode,
        added,
        size,
        minSdkVersion,
        targetSdkVersion,
        maxSdkVersion,
        source,
        release,
        hash,
        hashType,
        signature,
        obbMain,
        obbMainHash,
        obbMainHashType,
        obbPatch,
        obbPatchHash,
        obbPatchHashType,
        permissions,
        features,
        platforms,
        incompatibilities,
    )
}

@Entity(tableName = TABLE_RELEASE_TEMP)
class ReleaseTemp(
    packageName: String,
    selected: Boolean,
    version: String,
    versionCode: Long,
    added: Long,
    size: Long,
    minSdkVersion: Int,
    targetSdkVersion: Int,
    maxSdkVersion: Int,
    source: String,
    release: String,
    hash: String,
    hashType: String,
    signature: String,
    obbMain: String,
    obbMainHash: String,
    obbMainHashType: String,
    obbPatch: String,
    obbPatchHash: String,
    obbPatchHashType: String,
    permissions: List<String>,
    features: List<String>,
    platforms: List<String>,
    incompatibilities: List<Incompatibility>,
) : Release(
    packageName,
    selected,
    version,
    versionCode,
    added,
    size,
    minSdkVersion,
    targetSdkVersion,
    maxSdkVersion,
    source,
    release,
    hash,
    hashType,
    signature,
    obbMain,
    obbMainHash,
    obbMainHashType,
    obbPatch,
    obbPatchHash,
    obbPatchHashType,
    permissions,
    features,
    platforms,
    incompatibilities,
)

fun Release.asReleaseTemp() = ReleaseTemp(
    packageName = packageName,
    selected = selected,
    version = version,
    versionCode = versionCode,
    added = added,
    size = size,
    minSdkVersion = minSdkVersion,
    targetSdkVersion = targetSdkVersion,
    maxSdkVersion = maxSdkVersion,
    source = source,
    release = release,
    hash = hash,
    hashType = hashType,
    signature = signature,
    obbMain = obbMain,
    obbMainHash = obbMainHash,
    obbMainHashType = obbMainHashType,
    obbPatch = obbPatch,
    obbPatchHash = obbPatchHash,
    obbPatchHashType = obbPatchHashType,
    permissions = permissions,
    features = features,
    platforms = platforms,
    incompatibilities = incompatibilities,
)
