package com.machiav3lli.fdroid.data.database.entity

import androidx.core.net.toUri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.machiav3lli.fdroid.ROW_HASH
import com.machiav3lli.fdroid.ROW_MINSDK_VERSION
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_PLATFORMS
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.ROW_SIGNATURE
import com.machiav3lli.fdroid.ROW_TARGETSDK_VERSION
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import com.machiav3lli.fdroid.TABLE_RELEASE
import com.machiav3lli.fdroid.TABLE_RELEASE_TEMP
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Entity(
    tableName = TABLE_RELEASE,
    primaryKeys = [ROW_PACKAGE_NAME, ROW_REPOSITORY_ID, ROW_VERSION_CODE, ROW_SIGNATURE, ROW_PLATFORMS, ROW_HASH],
    indices = [
        Index(
            value = [ROW_PACKAGE_NAME, ROW_REPOSITORY_ID, ROW_VERSION_CODE, ROW_SIGNATURE, ROW_PLATFORMS, ROW_HASH],
            unique = true
        ),
        Index(value = [ROW_PACKAGE_NAME, ROW_MINSDK_VERSION, ROW_TARGETSDK_VERSION]),
        Index(value = [ROW_PACKAGE_NAME]),
        Index(value = [ROW_MINSDK_VERSION]),
        Index(value = [ROW_TARGETSDK_VERSION]),
        Index(value = [ROW_PACKAGE_NAME, ROW_SIGNATURE]),
        Index(value = [ROW_REPOSITORY_ID]),
    ]
)
@Serializable
open class Release(
    val packageName: String,
    @ColumnInfo(defaultValue = "0")
    val repositoryId: Long = 0L,
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
    @ColumnInfo(defaultValue = "0")
    val isCompatible: Boolean,
    @ColumnInfo(defaultValue = "[]")
    val releaseChannels: List<String> = emptyList(),
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
        get() = "$packageName&$repositoryId#$versionCode%$signature§$platforms.$hash"

    fun getDownloadUrl(repository: Repository): String {
        return repository.downloadAddress.toUri().buildUpon().appendPath(release).build().toString()
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
        packageName = packageName,
        repositoryId = repositoryId,
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
        isCompatible = incompatibilities.isEmpty(),
        releaseChannels = releaseChannels,
    )
}

@Entity(tableName = TABLE_RELEASE_TEMP)
class ReleaseTemp(
    packageName: String,
    repositoryId: Long,
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
    isCompatible: Boolean,
    releaseChannels: List<String>,
) : Release(
    packageName = packageName,
    repositoryId = repositoryId,
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
    isCompatible = isCompatible,
    releaseChannels = releaseChannels,
)

fun Release.asReleaseTemp() = ReleaseTemp(
    packageName = packageName,
    repositoryId = repositoryId,
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
    isCompatible = isCompatible,
    releaseChannels = releaseChannels,
)
