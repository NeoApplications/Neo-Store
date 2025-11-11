package com.machiav3lli.fdroid.data.index

import android.content.Context
import android.util.Log
import androidx.compose.ui.util.fastMap
import androidx.core.net.toUri
import com.machiav3lli.fdroid.BUFFER_SIZE
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureTemp
import com.machiav3lli.fdroid.data.database.entity.CategoryTemp
import com.machiav3lli.fdroid.data.database.entity.IndexProduct
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.database.entity.RepoCategoryTemp
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.database.entity.asProductTemp
import com.machiav3lli.fdroid.data.database.entity.asReleaseTemp
import com.machiav3lli.fdroid.data.index.v0.IndexV0Parser
import com.machiav3lli.fdroid.data.index.v1.IndexV1Parser
import com.machiav3lli.fdroid.data.index.v2.IdMap
import com.machiav3lli.fdroid.data.index.v2.IndexV2
import com.machiav3lli.fdroid.data.index.v2.IndexV2Merger
import com.machiav3lli.fdroid.data.index.v2.IndexV2Parser
import com.machiav3lli.fdroid.data.index.v2.IndexV2Parser.Companion.hasCachedIndex
import com.machiav3lli.fdroid.data.index.v2.findLocalized
import com.machiav3lli.fdroid.manager.network.Downloader
import com.machiav3lli.fdroid.manager.work.SyncWorker
import com.machiav3lli.fdroid.utils.CoroutineUtils
import com.machiav3lli.fdroid.utils.ProgressInputStream
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.extension.text.unhex
import com.machiav3lli.fdroid.utils.notifyDebugStatus
import io.ktor.http.HttpStatusCode
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarFile

object RepositoryUpdater : KoinComponent {
    enum class Stage {
        DOWNLOAD, PROCESS, MERGE, COMMIT
    }

    private enum class IndexType(
        val jarName: String,
        val contentName: String,
        val certificateFromIndex: Boolean,
    ) {
        INDEX("index.jar", "index.xml", true),
        INDEX_V1("index-v1.jar", "index-v1.json", false),
        INDEX_V2("", "index-v2.json", false),
        INDEX_V2_ENTRY("entry.jar", "entry.json", false),
        INDEX_V2_DIFF("", "TIMESTAMP.json", false),
    }

    enum class ErrorType {
        NETWORK, HTTP, VALIDATION, PARSING
    }

    class UpdateException : Exception {
        val errorType: ErrorType

        constructor(errorType: ErrorType, message: String) : super(message) {
            this.errorType = errorType
        }

        constructor(errorType: ErrorType, message: String, cause: Throwable) : super(
            message,
            cause
        ) {
            this.errorType = errorType
        }
    }

    private val updaterMutex = Mutex()
    private val cleanupLock = Any()
    private val db: DatabaseX by inject()

    fun init() {
        var lastDisabled = setOf<Long>()

        runBlocking(Dispatchers.IO) {
            launch {
                val newDisabled = CoroutineUtils.querySingle {
                    db.getRepositoryDao().getAllDisabledIds()
                }.toSet()

                val disabled = newDisabled - lastDisabled
                lastDisabled = newDisabled

                if (disabled.isNotEmpty()) {
                    val pairs = disabled.asSequence().map { Pair(it, false) }.toSet()

                    synchronized(cleanupLock) {
                        db.cleanUp(pairs)
                    }
                }
            }
        }
    }

    suspend fun await() {
        updaterMutex.withLock { }
    }

    suspend fun update(
        context: Context,
        repository: Repository, unstable: Boolean,
        callback: (Stage, Long, Long?) -> Unit,
    ): Boolean {
        return update(
            context = context,
            repository = repository,
            indexTypes = listOfNotNull(
                if (Preferences[Preferences.Key.IndexV2]) IndexType.INDEX_V2_ENTRY else null,
                if (Preferences[Preferences.Key.IndexV2]) IndexType.INDEX_V2 else null,
                IndexType.INDEX_V1,
                IndexType.INDEX
            ),
            unstable = unstable,
            callback = callback
        )
    }

    private suspend fun update(
        context: Context,
        repository: Repository, indexTypes: List<IndexType>, unstable: Boolean,
        entryLastModified: String? = null,
        entryEntityTag: String? = null,
        callback: (Stage, Long, Long?) -> Unit,
    ): Boolean {
        val indexType = indexTypes[0]
        return withContext(Dispatchers.IO) {
            val (result, file) = downloadIndex(
                context = context,
                repository = repository,
                indexType = indexType,
                lastModified = if (indexType == IndexType.INDEX_V2_ENTRY) repository.entryLastModified
                else repository.lastModified,
                entityTag = if (indexType == IndexType.INDEX_V2_ENTRY) repository.entryEntityTag
                else repository.entityTag,
                callback = callback
            )

            when {
                result.isNotModified -> {
                    file.delete()
                    false
                }

                !result.success     -> {
                    file.delete()
                    if (result.statusCode == HttpStatusCode.NotFound && indexTypes.size > 1) {
                        update(
                            context = context,
                            repository = repository,
                            indexTypes = indexTypes.drop(1),
                            unstable = unstable,
                            callback = callback
                        )
                    } else {
                        throw UpdateException(
                            ErrorType.HTTP,
                            "Invalid response: HTTP ${result.statusCode}"
                        )
                    }
                }

                else                -> {
                    launch {
                        CoroutineUtils.managedSingle {
                            val hasCachedIndex = hasCachedIndex(context, repository.id)
                            Log.d(
                                "RepositoryUpdater",
                                "downloaded repository file, repoID = ${repository.id}, indexType = $indexType, hasCachedIndex = $hasCachedIndex"
                            )
                            when (indexType) {
                                IndexType.INDEX_V2_ENTRY -> {
                                    if (hasCachedIndex) {
                                        downloadIndexV2Diff(
                                            context, repository, unstable,
                                            file, result.lastModified.nullIfEmpty(),
                                            result.entityTag.nullIfEmpty(), callback
                                        )
                                    } else {
                                        update(
                                            context = context,
                                            repository = repository,
                                            indexTypes = indexTypes.drop(1),
                                            unstable = unstable,
                                            entryLastModified = result.lastModified.nullIfEmpty(),
                                            entryEntityTag = result.entityTag.nullIfEmpty(),
                                            callback = callback
                                        )
                                    }
                                }

                                else                     -> processFile(
                                    context, repository, indexType,
                                    unstable, file, result.lastModified, entryLastModified,
                                    result.entityTag, entryEntityTag, callback
                                )
                            }
                        }
                    }
                    true
                }
            }
        }
    }

    private suspend fun downloadIndex(
        context: Context,
        repository: Repository, indexType: IndexType,
        lastModified: String, entityTag: String,
        callback: (Stage, Long, Long?) -> Unit,
    ): Pair<Downloader.Result, File> = withContext(Dispatchers.IO) {
        val file = Cache.getTemporaryFile(context)
        try {
            val result = Downloader.download(
                url = repository.downloadAddress.toUri().buildUpon()
                    .appendPath(
                        if (indexType != IndexType.INDEX_V2) indexType.jarName
                        else indexType.contentName
                    )
                    .build().toString(),
                target = file,
                lastModified = lastModified,
                entityTag = entityTag,
                authentication = repository.authentication
            ) { read, total, _ -> callback(Stage.DOWNLOAD, read, total) }
            Pair(result, file)
        } catch (e: Exception) {
            // onErrorResumeNext replacement?
            file.delete()
            throw UpdateException(
                ErrorType.NETWORK,
                "Network error",
                e
            )
        }
    }

    private suspend fun downloadIndexV2Diff(
        context: Context,
        repository: Repository,
        unstable: Boolean,
        file: File,
        entryLastModified: String?,
        entryEntityTag: String?,
        callback: (Stage, Long, Long?) -> Unit
    ): Boolean {
        val fallbackUpdate = suspend {
            update(
                context = context,
                repository = repository,
                indexTypes = persistentListOf(
                    IndexType.INDEX_V2,
                    IndexType.INDEX_V1,
                    IndexType.INDEX,
                ),
                unstable = unstable,
                entryLastModified = entryLastModified,
                entryEntityTag = entryEntityTag,
                callback = callback
            )
        }

        return withContext(Dispatchers.IO) {
            val (jarFile, indexEntry) = JarFile(file, true)
                .let { Pair(it, it.getEntry(IndexType.INDEX_V2_ENTRY.contentName) as JarEntry?) }
            jarFile.getInputStream(indexEntry)?.let { entryStream ->
                val entry = IndexV2.Entry.fromJsonStream(entryStream)

                if (entry.timestamp == repository.timestamp) {
                    file.delete()
                    return@withContext false
                }

                val diffAddress = entry.getDiff(repository.timestamp)?.name
                    ?: return@withContext fallbackUpdate()

                val (result, diffFile) = downloadDiffFile(
                    context, repository,
                    entry.timestamp,
                    diffAddress,
                    callback
                )
                val hasCachedIndex = hasCachedIndex(context, repository.id)
                Log.d(
                    "RepositoryUpdater",
                    "downloaded diff file, repoID = ${repository.id}, result.statusCode = ${result.statusCode}, hasCachedIndex = $hasCachedIndex"
                )
                when {
                    result.isNotModified -> {
                        diffFile.delete()
                        false
                    }

                    !result.success     -> {
                        diffFile.delete()
                        if (result.statusCode == HttpStatusCode.NotFound) fallbackUpdate()
                        else {
                            throw UpdateException(
                                ErrorType.HTTP,
                                "Invalid response: HTTP ${result.statusCode}"
                            )
                        }
                    }

                    else                -> {
                        launch {
                            CoroutineUtils.managedSingle {
                                if (hasCachedIndex) processFile(
                                    context, repository, IndexType.INDEX_V2_DIFF, unstable,
                                    diffFile, result.lastModified, entryLastModified,
                                    result.entityTag, entryEntityTag, callback,
                                ) else fallbackUpdate()
                            }
                        }
                        true
                    }
                }
            } ?: fallbackUpdate()
        }
    }

    private suspend fun downloadDiffFile(
        context: Context, repository: Repository,
        entryTimestamp: Long, diffAddress: String,
        callback: (Stage, Long, Long?) -> Unit,
    ): Pair<Downloader.Result, File> = withContext(Dispatchers.IO) {
        val file = Cache.getTemporaryFile(context)
        val diffUrl = (repository.downloadAddress + diffAddress)
        try {
            val result = Downloader.download(
                url = diffUrl,
                target = file,
                lastModified = "",
                entityTag = "",
                authentication = repository.authentication
            ) { read, total, _ -> callback(Stage.DOWNLOAD, read, total) }
            Log.d(
                "RepositoryUpdater",
                "downloading diff file from entry, repoID = ${repository.id}, timestamp = ${repository.timestamp}, entry.timestamp = $entryTimestamp, downloadUrl = $diffUrl"
            )
            Pair(result, file)
        } catch (e: Exception) {
            // onErrorResumeNext replacement?
            file.delete()
            throw UpdateException(
                ErrorType.NETWORK,
                "Network error",
                e
            )
        }
    }

    private suspend fun processFile(
        context: Context,
        repository: Repository, indexType: IndexType, unstable: Boolean,
        file: File, lastModified: String, entryLastModified: String?,
        entityTag: String, entryEntityTag: String?, callback: (Stage, Long, Long?) -> Unit,
    ): Boolean {
        var rollback = true
        Log.d(
            "RepositoryUpdater",
            "processing file, repoID = ${repository.id}, indexType = $indexType, lastModified = $lastModified, entryLastModified = $entryLastModified"
        )
        return updaterMutex.withLock {
            try {
                val (jarFile, indexEntry) = when (indexType) {
                    IndexType.INDEX_V2_DIFF,
                    IndexType.INDEX_V2 -> Pair(null, null)

                    else               -> JarFile(file, true)
                        .let { Pair(it, it.getEntry(indexType.contentName) as JarEntry?) }
                }
                val index = when (indexType) {
                    IndexType.INDEX_V2      -> file
                    IndexType.INDEX_V2_DIFF -> {
                        Cache.getIndexV2File(context, repository.id)
                            .takeIf { it.exists() && it.length() > 0 }
                            ?.let { indexFile ->
                                IndexV2Merger(indexFile).use { merger ->
                                    merger.processDiff(
                                        file.inputStream()
                                    ).let {
                                        notifyDebugStatus(
                                            context,
                                            "RepositoryUpdater",
                                            "merged diff file, repoID = ${repository.id}, succcess = $it, indexFile = $indexFile."
                                        )
                                    }
                                }
                                indexFile
                            } ?: file
                    }

                    else                    -> null
                }
                val total = when (indexType) {
                    IndexType.INDEX_V2_DIFF,
                    IndexType.INDEX_V2 -> file.length()

                    else               -> indexEntry?.size
                }
                db.getProductTempDao().emptyTable()
                db.getReleaseTempDao().emptyTable()
                db.getCategoryTempDao().emptyTable()

                Log.d(
                    "RepositoryUpdater",
                    "parsing index file, repoID = ${repository.id}, indexType = $indexType, total = $total, lastModified = $lastModified, entryLastModified = $entryLastModified"
                )
                val (changedRepository, certificateFromIndex) = when (indexType) {
                    IndexType.INDEX    -> processIndexV0(
                        context,
                        repository,
                        jarFile?.getInputStream(indexEntry)!!,
                        unstable,
                        lastModified,
                        entityTag,
                        total!!,
                        callback,
                    )

                    IndexType.INDEX_V1 -> processIndexV1(
                        context,
                        repository,
                        jarFile?.getInputStream(indexEntry)!!,
                        unstable,
                        lastModified,
                        entityTag,
                        total!!,
                        callback,
                    )

                    IndexType.INDEX_V2_DIFF,
                    IndexType.INDEX_V2 -> processIndexV2(
                        context,
                        repository,
                        index!!,
                        unstable,
                        lastModified,
                        entryLastModified.orEmpty(),
                        entityTag,
                        entryEntityTag.orEmpty(),
                        total!!,
                        callback,
                    )

                    else               -> throw IllegalArgumentException("Illegal index type was sent to processing: $indexType")
                }

                val workRepository = changedRepository ?: repository
                // TODO add better validation for Index-V2
                when {
                    workRepository.timestamp < repository.timestamp -> {
                        throw UpdateException(
                            ErrorType.VALIDATION, "New index is older than current index: " +
                                    "${workRepository.timestamp} < ${repository.timestamp}"
                        )
                    }

                    indexType != IndexType.INDEX_V2
                            && indexType != IndexType.INDEX_V2_DIFF -> {
                        val fingerprint = run {
                            val certificateFromJar = validateCertificate(indexEntry)
                            val fingerprintFromJar = calculateFingerprint(certificateFromJar)
                            if (indexType.certificateFromIndex) {
                                val fingerprintFromIndex =
                                    certificateFromIndex?.unhex()?.let(::calculateFingerprint)
                                if (fingerprintFromIndex == null || fingerprintFromJar != fingerprintFromIndex) {
                                    throw UpdateException(
                                        ErrorType.VALIDATION,
                                        "index.xml contains invalid public key"
                                    )
                                }
                                fingerprintFromIndex
                            } else {
                                fingerprintFromJar
                            }
                        }

                        commitChanges(workRepository, fingerprint, callback)
                        rollback = false
                        true
                    }

                    else                                            -> {
                        val valid = workRepository.fingerprint.isNotBlank()
                        commitChanges(workRepository, workRepository.fingerprint, callback)
                        rollback = !valid
                        valid
                    }
                }
            } catch (e: Exception) {
                when (e) {
                    is UpdateException,
                    is InterruptedException,
                         -> throw e

                    is IndexV2Parser.ParsingException,
                         -> when (e.cause) {
                        is IndexV2Parser.BaseParsingException -> {
                            Cache.getIndexV2File(context, repository.id).delete()
                            SyncWorker.enqueueManual(Pair(repository.id, repository.name))
                            false
                        }

                        is IndexV2Parser.DiffParsingException -> {
                            throw UpdateException(
                                ErrorType.PARSING,
                                e.message.orEmpty(),
                                e.cause ?: e
                            )
                        }

                        else                                  -> throw UpdateException(
                            ErrorType.PARSING,
                            e.message.orEmpty(),
                            e.cause ?: e
                        )
                    }

                    else -> throw UpdateException(
                        ErrorType.PARSING,
                        "Error parsing index",
                        e
                    )
                }
            } finally {
                file.delete()
                if (rollback) {
                    db.finishTemporary(repository, false)
                }
            }
        }
    }

    private fun processIndexV0(
        context: Context,
        repository: Repository,
        inputStream: InputStream,
        unstable: Boolean,
        lastModified: String,
        entityTag: String,
        total: Long,
        callback: (Stage, Long, Long?) -> Unit
    ): Pair<Repository?, String?> {
        var changedRepository: Repository? = null
        var certificateFromIndex: String? = null
        val products = mutableListOf<IndexProduct>()
        val features = context.packageManager.systemAvailableFeatures
            .asSequence().map { it.name }.toSet() + setOf("android.hardware.touchscreen")

        val parser = IndexV0Parser(repository.id, object : IndexV0Parser.Callback {
            override fun onRepository(
                mirrors: List<String>, name: String, description: String,
                certificate: String, version: Int, timestamp: Long,
            ) {
                changedRepository = repository.update(
                    mirrors = mirrors,
                    name = name,
                    description = description,
                    version = version,
                    lastModified = lastModified,
                    entityTag = entityTag,
                    timestamp = timestamp,
                    webBaseUrl = null,
                    entryLastModified = null,
                    entryEntityTag = null,
                )
                certificateFromIndex = certificate.lowercase(Locale.US)
            }

            override fun onProduct(product: IndexProduct) {
                if (Thread.interrupted()) throw InterruptedException()

                products += product.apply {
                    refreshReleases(features, unstable)
                }
                runBlocking {
                    if (products.size >= 100) {
                        db.getProductTempDao().insert(*products.map {
                            it.toV2().asProductTemp()
                        }.toTypedArray())
                        db.getCategoryTempDao().insert(*products.flatMap {
                            it.categories.distinct().map { category ->
                                CategoryTemp(
                                    repositoryId = it.repositoryId,
                                    packageName = it.packageName,
                                    name = category,
                                )
                            }
                        }.toTypedArray())
                        db.getReleaseTempDao()
                            .insert(*(products.flatMap { it.releases }
                                .map { it.asReleaseTemp() }.toTypedArray()))
                        products.clear()
                    }
                }
            }
        })

        runBlocking {
            ProgressInputStream(inputStream) {
                callback(Stage.PROCESS, it, total)
            }.use { parser.parse(it) }

            if (Thread.interrupted()) throw InterruptedException()

            if (products.isNotEmpty()) {
                db.getProductTempDao().insert(*products.map {
                    it.toV2().asProductTemp()
                }.toTypedArray())
                db.getCategoryTempDao().insert(*products.flatMap {
                    it.categories.distinct().map { category ->
                        CategoryTemp(
                            repositoryId = it.repositoryId,
                            packageName = it.packageName,
                            name = category,
                        )
                    }
                }.toTypedArray())
                db.getReleaseTempDao()
                    .insert(*(products.flatMap { it.releases }
                        .map { it.asReleaseTemp() }.toTypedArray()))
                products.clear()
            }
        }
        return Pair(changedRepository, certificateFromIndex)
    }

    private fun processIndexV1(
        context: Context,
        repository: Repository,
        inputStream: InputStream,
        unstable: Boolean,
        lastModified: String,
        entityTag: String,
        total: Long,
        callback: (Stage, Long, Long?) -> Unit
    ): Pair<Repository?, String?> {
        var changedRepository: Repository? = null
        val features = context.packageManager.systemAvailableFeatures
            .asSequence().map { it.name }.toSet() + setOf("android.hardware.touchscreen")

        val mergerFile = Cache.getTemporaryFile(context)
        try {
            val unmergedProducts = mutableListOf<IndexProduct>()
            val unmergedReleases = mutableListOf<Pair<String, List<Release>>>()
            IndexContentMerger(mergerFile).use { indexMerger ->
                ProgressInputStream(inputStream) {
                    callback(
                        Stage.PROCESS,
                        it,
                        total
                    )
                }.use { progressInputStream ->
                    IndexV1Parser(
                        repository.id,
                        object : IndexV1Parser.Callback {
                            override fun onRepository(
                                mirrors: List<String>,
                                name: String,
                                description: String,
                                version: Int,
                                timestamp: Long,
                            ) {
                                changedRepository = repository.update(
                                    mirrors = mirrors,
                                    name = name,
                                    description = description,
                                    version = version,
                                    lastModified = lastModified,
                                    entityTag = entityTag,
                                    timestamp = timestamp,
                                    webBaseUrl = null,
                                    entryLastModified = null,
                                    entryEntityTag = null,
                                )
                            }

                            override fun onProduct(product: IndexProduct) {
                                if (Thread.interrupted()) throw InterruptedException()

                                unmergedProducts += product
                                if (unmergedProducts.size >= 100) {
                                    indexMerger.addProducts(unmergedProducts)
                                    unmergedProducts.clear()
                                }
                            }

                            override fun onReleases(
                                packageName: String,
                                releases: List<Release>,
                            ) {
                                if (Thread.interrupted()) throw InterruptedException()

                                unmergedReleases += Pair(packageName, releases)
                                if (unmergedReleases.size >= 100) {
                                    indexMerger.addReleases(unmergedReleases)
                                    unmergedReleases.clear()
                                }
                            }
                        }
                    ).parse(progressInputStream)

                    if (Thread.interrupted()) throw InterruptedException()

                    if (unmergedProducts.isNotEmpty()) {
                        indexMerger.addProducts(unmergedProducts)
                        unmergedProducts.clear()
                    }
                    if (unmergedReleases.isNotEmpty()) {
                        indexMerger.addReleases(unmergedReleases)
                        unmergedReleases.clear()
                    }
                    var progress = 0
                    indexMerger.forEach(
                        repository.id,
                        100
                    ) { products, totalCount ->
                        if (Thread.interrupted()) throw InterruptedException()

                        progress += products.size
                        callback(
                            Stage.MERGE,
                            progress.toLong(),
                            totalCount.toLong()
                        )
                        runBlocking {
                            products.map {
                                it.apply {
                                    refreshReleases(features, unstable)
                                }
                            }.let { updatedProducts ->
                                db.getProductTempDao().insert(*updatedProducts.map {
                                    it.toV2().asProductTemp()
                                }.toTypedArray())
                                db.getCategoryTempDao().insert(*updatedProducts.flatMap {
                                    it.categories.distinct().map { category ->
                                        CategoryTemp(
                                            repositoryId = it.repositoryId,
                                            packageName = it.packageName,
                                            name = category,
                                        )
                                    }
                                }.toTypedArray())
                                db.getReleaseTempDao().insert(
                                    *(updatedProducts.flatMap { it.releases }
                                        .fastMap { it.asReleaseTemp() }.toTypedArray())
                                )
                            }
                        }
                    }
                }
            }
        } finally {
            mergerFile.delete()
        }
        return Pair(changedRepository, null)
    }

    private fun processIndexV2(
        context: Context,
        repository: Repository,
        indexFile: File,
        unstable: Boolean,
        lastModified: String,
        entryLastModified: String,
        entityTag: String,
        entryEntityTag: String,
        total: Long,
        callback: (Stage, Long, Long?) -> Unit
    ): Pair<Repository?, String?> {
        var changedRepository: Repository? = null
        val repoCategories: MutableSet<RepoCategoryTemp> = mutableSetOf()
        val repoAntifeatures: MutableSet<AntiFeatureTemp> = mutableSetOf()
        val features = context.packageManager.systemAvailableFeatures
            .asSequence().map { it.name }.toSet() + setOf("android.hardware.touchscreen")

        val mergerFile = Cache.getTemporaryFile(context)
        try {
            val unmergedProducts = mutableListOf<IndexProduct>()
            val unmergedReleases = mutableListOf<Pair<String, List<Release>>>()
            IndexContentMerger(mergerFile).use { indexMerger ->
                ProgressInputStream(indexFile.inputStream()) {
                    callback(Stage.PROCESS, it, total)
                }.use { progressInputStream ->
                    IndexV2Parser(
                        repository.id,
                        object : IndexV2Parser.Callback {
                            override fun onRepository(
                                mirrors: List<String>,
                                name: String,
                                description: String,
                                version: Int,
                                timestamp: Long,
                                webBaseUrl: String?,
                                categories: IdMap<IndexV2.Category>,
                                antiFeatures: IdMap<IndexV2.AntiFeature>,
                            ) {
                                repoCategories.addAll(categories.map {
                                    RepoCategoryTemp(
                                        repository.id,
                                        it.key,
                                        it.value.name.findLocalized(""),
                                        it.value.icon.findLocalized(IndexV2.File("")).name,
                                    )
                                })
                                repoAntifeatures.addAll(antiFeatures.map {
                                    AntiFeatureTemp(
                                        repository.id,
                                        it.key,
                                        it.value.name.findLocalized(""),
                                        it.value.description.findLocalized(""),
                                        it.value.icon.findLocalized(IndexV2.File("")).name,
                                    )
                                })
                                changedRepository = repository.update(
                                    mirrors = mirrors,
                                    name = name,
                                    description = description,
                                    version = version,
                                    lastModified = lastModified,
                                    entityTag = entityTag,
                                    timestamp = timestamp,
                                    webBaseUrl = webBaseUrl,
                                    entryLastModified = entryLastModified,
                                    entryEntityTag = entryEntityTag,
                                )
                            }

                            override fun onProduct(product: IndexProduct) {
                                if (Thread.interrupted()) throw InterruptedException()

                                unmergedProducts += product
                                if (unmergedProducts.size >= 100) {
                                    indexMerger.addProducts(unmergedProducts)
                                    unmergedProducts.clear()
                                }
                            }

                            override fun onReleases(
                                packageName: String,
                                releases: List<Release>,
                            ) {
                                if (Thread.interrupted()) throw InterruptedException()

                                unmergedReleases += Pair(packageName, releases)
                                if (unmergedReleases.size >= 100) {
                                    indexMerger.addReleases(unmergedReleases)
                                    unmergedReleases.clear()
                                }
                            }
                        }
                    ).parse(progressInputStream)

                    if (Thread.interrupted()) throw InterruptedException()

                    if (unmergedProducts.isNotEmpty()) {
                        indexMerger.addProducts(unmergedProducts)
                        unmergedProducts.clear()
                    }
                    if (unmergedReleases.isNotEmpty()) {
                        indexMerger.addReleases(unmergedReleases)
                        unmergedReleases.clear()
                    }
                    var progress = 0
                    indexMerger.forEach(
                        repository.id,
                        100
                    ) { products, totalCount ->
                        if (Thread.interrupted()) throw InterruptedException()

                        progress += products.size
                        callback(
                            Stage.MERGE,
                            progress.toLong(),
                            totalCount.toLong()
                        )
                        runBlocking {
                            products.map {
                                it.apply {
                                    refreshReleases(features, unstable)
                                }
                            }.let { updatedProducts ->
                                db.getProductTempDao().insert(*updatedProducts.map {
                                    it.toV2().asProductTemp()
                                }.toTypedArray())
                                db.getCategoryTempDao().insert(*updatedProducts.flatMap {
                                    it.categories.distinct().map { category ->
                                        CategoryTemp(
                                            repositoryId = it.repositoryId,
                                            packageName = it.packageName,
                                            name = category,
                                        )
                                    }
                                }.toTypedArray())
                                db.getReleaseTempDao().insert(
                                    *(updatedProducts.flatMap { it.releases }
                                        .map { it.asReleaseTemp() }.toTypedArray())
                                )
                            }
                        }
                    }
                    runBlocking {
                        db.getRepoCategoryTempDao().insert(*repoCategories.toTypedArray())
                        db.getAntiFeatureTempDao().insert(*repoAntifeatures.toTypedArray())
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("RepositoryUpdater", "Error processing index-v2", e)
            throw UpdateException(
                ErrorType.PARSING,
                "Error processing index-v2: ${e.message}",
                e
            )
        } finally {
            Cache.getIndexV2File(context, repository.id).let {
                if (it != indexFile) indexFile.copyTo(
                    it,
                    true,
                    BUFFER_SIZE,
                )
            }
            mergerFile.delete()
        }
        return Pair(changedRepository, null)
    }

    private fun validateCertificate(indexEntry: JarEntry?): X509Certificate {
        val codeSigners = indexEntry?.codeSigners
        if (codeSigners == null || codeSigners.size != 1) {
            throw UpdateException(
                ErrorType.VALIDATION,
                "Index must be signed by a single code signer"
            )
        }
        val certificates = codeSigners[0].signerCertPath?.certificates.orEmpty()
        if (certificates.size != 1) {
            throw UpdateException(
                ErrorType.VALIDATION,
                "Index code signer should have only one certificate"
            )
        }
        return certificates[0] as X509Certificate
    }

    private fun calculateFingerprint(certificate: Certificate): String {
        val encoded = try {
            certificate.encoded
        } catch (e: CertificateEncodingException) {
            null
        }
        return encoded?.let(::calculateFingerprint).orEmpty()
    }

    private fun calculateFingerprint(key: ByteArray): String {
        return if (key.size >= 256) {
            try {
                val fingerprint = MessageDigest.getInstance("SHA-256").digest(key)
                val builder = StringBuilder()
                for (byte in fingerprint) {
                    builder.append("%02X".format(Locale.US, byte.toInt() and 0xff))
                }
                builder.toString()
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        } else {
            ""
        }
    }

    private fun commitChanges(
        workRepository: Repository,
        fingerprint: String,
        callback: (Stage, Long, Long?) -> Unit,
    ): Boolean = runBlocking {
        val commitRepository = if (workRepository.fingerprint != fingerprint) {
            if (workRepository.fingerprint.isEmpty()) {
                workRepository.copy(fingerprint = fingerprint)
            } else {
                throw UpdateException(
                    ErrorType.VALIDATION,
                    "Certificate fingerprints do not match"
                )
            }
        } else {
            workRepository
        }
        if (Thread.interrupted()) {
            throw InterruptedException()
        }
        callback(Stage.COMMIT, 0, null)
        db.finishTemporary(commitRepository, true)
        true
    }
}
