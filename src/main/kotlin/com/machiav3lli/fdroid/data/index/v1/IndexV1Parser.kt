package com.machiav3lli.fdroid.data.index.v1

import android.util.SparseArray
import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.utils.LocaleChooser.LocaleConfig
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

class IndexV1Parser(private val repositoryId: Long, private val callback: Callback, private val localesCallback: LocalesCallback) {
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
                localesCallback.setLocalized(product.localized.keys)
                try {
                    callback.onProduct(product.toProduct(repositoryId, localesCallback))
                } finally {
                    localesCallback.resetLocalized()
                }
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

    interface LocalesCallback {
        fun setLocalized(keys: Iterable<String>)
        fun <T> findLocalized(callback: (String) -> T?): T?
        fun resetLocalized()
    }

    open internal class PrefLocalized(private val locales: LocaleConfig) : LocalesCallback {
        val matched_locales = SparseArray<MutableList<String>>()

        override fun setLocalized(keys: Iterable<String>) {
            for (locale in keys) {
                val weight = locales.getWeight(locale)
                if (weight == -1) continue
                val list = matched_locales.get(weight)
                if (list == null) {
                    matched_locales.put(weight, mutableListOf(locale))
                } else {
                    list.add(locale)
                }
            }
        }

        override fun <T> findLocalized(callback: (String) -> T?): T? {
            for (i in 0..<matched_locales.size()) {
                for (locale in matched_locales.valueAt(i)) {
                    val value = callback(locale)
                    if (value != null) return value
                }
            }
            return null
        }

        override fun resetLocalized() {
            for (i in 0..<matched_locales.size()) {
                matched_locales.valueAt(i).clear()
            }
        }
    }

    class ParsingException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
