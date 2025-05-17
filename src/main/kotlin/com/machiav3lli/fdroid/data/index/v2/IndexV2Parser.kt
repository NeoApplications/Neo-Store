package com.machiav3lli.fdroid.data.index.v2

import android.R.attr.version
import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class IndexV2Parser(private val repositoryId: Long, private val callback: Callback) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

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
}