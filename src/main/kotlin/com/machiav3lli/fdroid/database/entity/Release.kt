package com.machiav3lli.fdroid.database.entity

import android.net.Uri
import io.realm.kotlin.ext.backlinks
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.ext.toRealmList
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.serializers.RealmListKSerializer
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
open class Release() : RealmObject {
    var packageName: String = ""
    var selected: Boolean = false
    var version: String = ""
    var versionCode: Long = -1L
    var added: Long = -1L
    var size: Long = -1L
    var minSdkVersion: Int = -1
    var targetSdkVersion: Int = -1
    var maxSdkVersion: Int = -1
    var source: String = ""
    var release: String = ""
    var hash: String = ""
    var hashType: String = ""
    var signature: String = ""
    var obbMain: String = ""
    var obbMainHash: String = ""
    var obbMainHashType: String = ""
    var obbPatch: String = ""
    var obbPatchHash: String = ""
    var obbPatchHashType: String = ""
    @Serializable(RealmListKSerializer::class)
    var permissions: RealmList<String> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var features: RealmList<String> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var platforms: RealmList<String> = realmListOf()
    @Serializable(RealmListKSerializer::class)
    var incompatibilities: RealmList<Incompatibility> = realmListOf()

    @PrimaryKey
    private var primaryKey: String = ""

    val product: RealmResults<Product> by backlinks(Product::releases)

    init {
        primaryKey = "$packageName/$versionCode/$signature"
    }

    constructor(
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
    ) : this() {
        this.packageName = packageName
        this.selected = selected
        this.version = version
        this.versionCode = versionCode
        this.added = added
        this.size = size
        this.minSdkVersion = minSdkVersion
        this.targetSdkVersion = targetSdkVersion
        this.maxSdkVersion = maxSdkVersion
        this.source = source
        this.release = release
        this.hash = hash
        this.hashType = hashType
        this.signature = signature
        this.obbMain = obbMain
        this.obbMainHash = obbMainHash
        this.obbMainHashType = obbMainHashType
        this.obbPatch = obbPatch
        this.obbPatchHash = obbPatchHash
        this.obbPatchHashType = obbPatchHashType
        this.permissions = permissions.toRealmList()
        this.features = features.toRealmList()
        this.platforms = platforms.toRealmList()
        this.incompatibilities = incompatibilities.toRealmList()
        primaryKey = "$packageName/$versionCode/$signature"
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
