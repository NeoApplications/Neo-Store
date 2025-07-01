package com.machiav3lli.fdroid.data.index.v2

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.longOrNull
import java.io.File
import java.io.InputStream

/**
 * Merger for applying JSON Merge Patch (RFC 7386) to IndexV2 instances.
 */
class IndexV2Merger(private val baseFile: File) : AutoCloseable {
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun getCurrentIndex(): IndexV2? = json.decodeFromStream(baseFile.inputStream())

    fun processDiff(
        diffStream: InputStream,
    ): Boolean {
        val baseJson = baseFile.readText()
        val diffJson = diffStream.bufferedReader().use { it.readText() }
        val mergedJson = merge(baseJson, diffJson)

        // Save the merged result
        Log.d("IndexV2Merger", "Merged a diff JSON into the base: ${mergedJson != baseJson}")
        baseFile.writeText(mergedJson)
        val timestamp = getTimestamp(json.parseToJsonElement(mergedJson))
        baseFile.setLastModified(timestamp)

        return mergedJson != baseJson
    }

    fun merge(baseJson: String, diffJson: String): String {
        val baseElement = json.parseToJsonElement(baseJson)
        val diffElement = json.parseToJsonElement(diffJson)

        // No need to apply a diff older or same as base
        val baseTimestamp = getTimestamp(baseElement)
        val diffTimestamp = getTimestamp(diffElement)
        if (diffTimestamp <= baseTimestamp) return baseJson

        // Apply the merge patch
        val mergedElement = mergePatch(baseElement, diffElement)

        // Ensure the timestamp is updated
        val mergedObj = mergedElement.jsonObject.toMutableMap()
        val repoObj = (mergedObj["repo"] as? JsonObject)?.toMutableMap() ?: return baseJson
        repoObj["timestamp"] = JsonPrimitive(diffTimestamp)

        return json.encodeToString(JsonObject(mergedObj + ("repo" to JsonObject(repoObj))))
    }

    /**
     * Applies a JSON Merge Patch (RFC 7386) to the target JSON element. RFC 7386 rules:
     * - If patch is not an object, replace target entirely
     * - If patch value is null, remove the key from target
     * - If patch value is an object, recursively merge with target
     * - Otherwise, replace target value with patch value
     */
    private fun mergePatch(target: JsonElement, patch: JsonElement): JsonElement {
        if (patch !is JsonObject || target !is JsonObject) return patch
        val result = target.jsonObject.toMutableMap()

        for ((key, value) in patch) {
            // No change when object is empty
            if (value is JsonObject && value.jsonObject.isEmpty()) continue

            when (value) {
                // Remove null objects
                is JsonNull   -> {
                    result.remove(key)
                }

                // Recursively merge objects
                is JsonObject -> {
                    val targetValue = target.jsonObject[key]
                    result[key] = if (targetValue is JsonObject) {
                        mergePatch(targetValue, value)
                    } else {
                        // If target doesn't have this key or it's not an object, use the patch value
                        value
                    }
                }

                // Replace primitive values entirely
                else          -> {
                    result[key] = value
                }
            }
        }

        return JsonObject(result)
    }

    private fun getTimestamp(element: JsonElement): Long {
        return (element.jsonObject["repo"]?.jsonObject?.get("timestamp") as? JsonPrimitive)?.longOrNull
            ?: 0L
    }

    override fun close() {
        // Cleanup when needed
    }
}
