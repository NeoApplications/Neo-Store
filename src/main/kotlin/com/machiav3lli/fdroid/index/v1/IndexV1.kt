package com.machiav3lli.fdroid.index.v1

import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable
data class IndexV1(
    val repo: Repo,
    //val requests: Requests,
    val apps: List<App>,
    val packages: Map<String, List<Package>>
) {
    @Serializable
    data class Repo(
        val address: String = "",
        @Serializable(with = MirrorsSerializer::class)
        val mirrors: List<String> = emptyList(),
        val name: String = "",
        val description: String = "",
        val version: Int = 0,
        val timestamp: Long = 0,
        val icon: String,
    )

    @Serializable
    data class App(
        val packageName: String,
        val name: String = "",
        val summary: String = "",
        val description: String = "",
        val icon: String = "",
        val authorName: String = "",
        val authorEmail: String = "",
        val authorWebSite: String = "",
        val sourceCode: String = "",
        val changelog: String = "",
        val webSite: String = "",
        val issueTracker: String = "",
        val added: Long = 0,
        val lastUpdated: Long = 0,
        val suggestedVersionCode: String = "",
        val categories: List<String> = emptyList(),
        val antiFeatures: List<String> = emptyList(),
        val license: String = "",
        val donate: String? = null,
        val bitcoin: String? = null,
        val litecoin: String? = null,
        val flattr: String? = null,
        val liberapay: String? = null,
        val openCollective: String? = null,
        val localized: Map<String, Localized> = emptyMap(),
    )

    @Serializable
    data class Localized(
        val name: String = "",
        val summary: String = "",
        val description: String = "",
        val whatsNew: String = "",
        val icon: String = "",
        val phoneScreenshots: List<String> = emptyList(),
        val sevenInchScreenshots: List<String> = emptyList(),
        val tenInchScreenshots: List<String> = emptyList(),
        val tvScreenshots: List<String> = emptyList(),
        val wearScreenshots: List<String> = emptyList(),
    ) {
        fun localeIcon(locale: String): String = icon.nullIfEmpty()?.let { "$locale/$it" }.orEmpty()
    }

    @Serializable
    data class Package(
        val versionName: String,
        val versionCode: Long = 0L,
        val added: Long,
        val size: Long,
        val minSdkVersion: Int = 0,
        val targetSdkVersion: Int = 0,
        val maxSdkVersion: Int = 0,
        val srcname: String = "",
        val apkName: String = "",
        val hash: String = "",
        val hashType: String = "",
        val sig: String = "",
        val signer: String = "", //
        val obbMainFile: String = "",
        val obbMainFileSha256: String = "",
        val obbPatchFile: String = "",
        val obbPatchFileSha256: String = "",
        @SerialName("uses-permission")
        @Serializable(PermissionListSerializer::class)
        val usesPermission: List<Permission> = emptyList(),
        @SerialName("uses-permission-sdk-23")
        @Serializable(PermissionListSerializer::class)
        val usesPermissionSdk23: List<Permission> = emptyList(),
        val features: List<String> = emptyList(),
        val nativecode: List<String> = emptyList(),
    )

    @Serializable
    data class Permission(
        val name: String,
        val maxSdk: Int = 0,
    )

    companion object {
        object PermissionListSerializer :
            JsonTransformingSerializer<List<Permission>>(ListSerializer(Permission.serializer())) {
            override fun transformDeserialize(element: JsonElement): JsonElement {
                return JsonArray(element.jsonArray.map {
                    JsonObject(
                        mapOf(
                            "name" to it.jsonArray[0],
                            "maxSdk" to (it.jsonArray.getOrNull(1).takeUnless { it is JsonNull }
                                ?: JsonPrimitive(0)),
                        )
                    )
                })
            }

            override fun transformSerialize(element: JsonElement): JsonElement {
                require(element is JsonArray) { "Expected JsonArray, but got ${element::class}" }
                return JsonArray(element.map { item ->
                    JsonArray(
                        listOfNotNull(
                            item.jsonObject["name"],
                            item.jsonObject["maxSdk"].takeUnless { it is JsonNull },
                        )
                    )
                })
            }
        }

        object MirrorsSerializer :
            JsonTransformingSerializer<List<String>>(ListSerializer(String.serializer())) {
            override fun transformDeserialize(element: JsonElement): JsonElement {
                return when (element) {
                    is JsonArray -> {
                        JsonArray(element.map {
                            when (it) {
                                is JsonPrimitive -> it
                                is JsonObject    -> JsonPrimitive(
                                    it.jsonObject["url"]?.jsonPrimitive?.content ?: ""
                                )

                                else             -> JsonPrimitive("")
                            }
                        })
                    }

                    else         -> element
                }
            }
        }
    }
}