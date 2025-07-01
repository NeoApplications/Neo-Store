package com.machiav3lli.fdroid.data.index.v2

import android.R.attr.version
import android.content.Context
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class IndexV2Parser(private val repositoryId: Long, private val callback: Callback) {
    @OptIn(ExperimentalSerializationApi::class)
    fun parse(inputStream: InputStream) {
        try {
            val indexV2 = json.decodeFromStream<IndexV2>(inputStream)

            with(indexV2.repo) {
                callback.onRepository(
                    mirrors = listOf(address).plus(mirrors.map { it.url }).distinct(),
                    name = name.findLocalized(""),
                    description = description.findLocalized(""),
                    version = version,
                    timestamp = timestamp,
                    categories = categories,
                    antiFeatures = antiFeatures,
                    webBaseUrl = webBaseUrl,
                )
            }

            indexV2.packages.forEach { (packageName, pkg) ->
                callback.onProduct(pkg.toProduct(repositoryId, packageName))
                callback.onReleases(
                    packageName,
                    pkg.versions.map { it.value.toRelease(repositoryId, packageName) },
                )
            }
        } catch (e: Exception) {
            throw ParsingException("Error parsing index", e)
        }
    }

    interface Callback {
        fun onRepository(
            mirrors: List<String>,
            name: String,
            description: String,
            version: Int,
            timestamp: Long,
            webBaseUrl: String?,
            categories: IdMap<IndexV2.Category>,
            antiFeatures: IdMap<IndexV2.AntiFeature>,
        )

        fun onProduct(product: IndexProduct)
        fun onReleases(packageName: String, releases: List<Release>)
    }

    class ParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
        }

        /*
         * Checks if the index has been cached and is not empty or corrupted
         */
        fun hasCachedIndex(context: Context, repoId: Long): Boolean = runCatching {
            val cacheFile = Cache.getIndexV2File(context, repoId).apply {
                val indexBase = json.decodeFromStream<IndexV2>(inputStream())
            }
            return cacheFile.exists() && cacheFile.length() > 0
        }.fold(
            onSuccess = { it },
            onFailure = { false }
        )
    }
}