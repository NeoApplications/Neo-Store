package com.machiav3lli.fdroid.data.index.v1

import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class IndexV1Parser(private val repositoryId: Long, private val callback: Callback) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @OptIn(ExperimentalSerializationApi::class)
    fun parse(inputStream: InputStream) {
        try {
            val indexV1 = json.decodeFromStream<IndexV1>(inputStream)

            with(indexV1.repo) {
                callback.onRepository(
                    mirrors = listOf(address).plus(mirrors).distinct(),
                    name = name,
                    description = description,
                    version = version,
                    timestamp = timestamp
                )
            }

            indexV1.apps.forEach { product ->
                callback.onProduct(product.toProduct(repositoryId))
            }

            indexV1.packages.forEach { (packageName, releases) ->
                callback.onReleases(
                    packageName,
                    releases.map { it.toRelease(repositoryId, packageName) },
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
        )

        fun onProduct(product: IndexProduct)
        fun onReleases(packageName: String, releases: List<Release>)
    }

    class ParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
