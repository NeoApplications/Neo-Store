package com.machiav3lli.fdroid.index

import android.content.Context
import android.net.Uri
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.network.Downloader
import com.machiav3lli.fdroid.utility.CoroutineUtils
import com.machiav3lli.fdroid.utility.ProgressInputStream
import com.machiav3lli.fdroid.utility.Utils
import com.machiav3lli.fdroid.utility.extension.text.unhex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.xml.sax.InputSource
import java.io.File
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarFile
import javax.xml.parsers.SAXParserFactory

object RepositoryUpdater {
    enum class Stage {
        DOWNLOAD, PROCESS, MERGE, COMMIT
    }

    private enum class IndexType(
        val jarName: String,
        val contentName: String,
        val certificateFromIndex: Boolean,
    ) {
        INDEX("index.jar", "index.xml", true),
        INDEX_V1("index-v1.jar", "index-v1.json", false)
    }

    enum class ErrorType {
        NETWORK, HTTP, VALIDATION, PARSING
    }

    class UpdateException : Exception {
        val errorType: ErrorType

        constructor(errorType: ErrorType, message: String) : super(message) {
            this.errorType = errorType
        }

        constructor(errorType: ErrorType, message: String, cause: Exception) : super(
            message,
            cause
        ) {
            this.errorType = errorType
        }
    }

    private val updaterLock = Any()
    private val cleanupLock = Any()
    lateinit var db: DatabaseX

    fun init(context: Context) {
        db = DatabaseX.getInstance(context)
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

    fun await() {
        synchronized(updaterLock) { }
    }

    suspend fun update(
        context: Context,
        repository: Repository, unstable: Boolean,
        callback: (Stage, Long, Long?) -> Unit,
    ): Boolean {
        return update(
            context,
            repository,
            listOf(IndexType.INDEX_V1, IndexType.INDEX),
            unstable,
            callback
        )
    }

    private suspend fun update(
        context: Context,
        repository: Repository, indexTypes: List<IndexType>, unstable: Boolean,
        callback: (Stage, Long, Long?) -> Unit,
    ): Boolean {
        val indexType = indexTypes[0]
        return withContext(Dispatchers.IO) {
            val (result, file) = downloadIndex(context, repository, indexType, callback)

            when {
                result.isNotChanged -> {
                    file.delete()
                    false
                }

                !result.success     -> {
                    file.delete()
                    if (result.code == 404 && indexTypes.isNotEmpty()) {
                        update(
                            context,
                            repository,
                            indexTypes.subList(1, indexTypes.size),
                            unstable,
                            callback
                        )
                    } else {
                        throw UpdateException(
                            ErrorType.HTTP,
                            "Invalid response: HTTP ${result.code}"
                        )
                    }
                }

                else                -> {
                    launch {
                        CoroutineUtils.managedSingle {
                            processFile(
                                context,
                                repository, indexType, unstable,
                                file, result.lastModified, result.entityTag, callback
                            )
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
        callback: (Stage, Long, Long?) -> Unit,
    ): Pair<Downloader.Result, File> {
        val file = Cache.getTemporaryFile(context)
        return withContext(Dispatchers.IO) {
            try {
                val result = Downloader.download(
                    Uri.parse(repository.address).buildUpon()
                        .appendPath(indexType.jarName).build().toString(),
                    file,
                    repository.lastModified,
                    repository.entityTag,
                    repository.authentication
                ) { read, total -> callback(Stage.DOWNLOAD, read, total) }
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
    }

    private fun processFile(
        context: Context,
        repository: Repository, indexType: IndexType, unstable: Boolean,
        file: File, lastModified: String, entityTag: String, callback: (Stage, Long, Long?) -> Unit,
    ): Boolean {
        var rollback = true
        val db = DatabaseX.getInstance(context)
        return synchronized(updaterLock) {
            try {
                val jarFile = JarFile(file, true)
                val indexEntry = jarFile.getEntry(indexType.contentName) as JarEntry
                val total = indexEntry.size
                db.getProductTempDao().emptyTable()
                db.getCategoryTempDao().emptyTable()
                val features = context.packageManager.systemAvailableFeatures
                    .asSequence().map { it.name }.toSet() + setOf("android.hardware.touchscreen")

                val (changedRepository, certificateFromIndex) = when (indexType) {
                    IndexType.INDEX -> {
                        val factory = SAXParserFactory.newInstance()
                        factory.isNamespaceAware = true
                        val parser = factory.newSAXParser()
                        val reader = parser.xmlReader
                        var changedRepository: Repository? = null
                        var certificateFromIndex: String? = null
                        val products = mutableListOf<Product>()

                        reader.contentHandler =
                            IndexHandler(repository.id, object : IndexHandler.Callback {
                                override fun onRepository(
                                    mirrors: List<String>, name: String, description: String,
                                    certificate: String, version: Int, timestamp: Long,
                                ) {
                                    changedRepository = repository.update(
                                        mirrors, name, description, version,
                                        lastModified, entityTag, timestamp
                                    )
                                    certificateFromIndex = certificate.lowercase(Locale.US)
                                }

                                override fun onProduct(product: Product) {
                                    if (Thread.interrupted()) {
                                        throw InterruptedException()
                                    }
                                    products += product.apply {
                                        refreshReleases(features, unstable)
                                        refreshVariables()
                                    }
                                    if (products.size >= 50) {
                                        db.getProductTempDao().putTemporary(products)
                                        products.clear()
                                    }
                                }
                            })

                        ProgressInputStream(jarFile.getInputStream(indexEntry)) {
                            callback(
                                Stage.PROCESS,
                                it,
                                total
                            )
                        }
                            .use { reader.parse(InputSource(it)) }
                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        if (products.isNotEmpty()) {
                            db.getProductTempDao().putTemporary(products)
                            products.clear()
                        }
                        Pair(changedRepository, certificateFromIndex)
                    }

                    IndexType.INDEX_V1 -> {
                        var changedRepository: Repository? = null

                        val mergerFile = Cache.getTemporaryFile(context)
                        try {
                            val unmergedProducts = mutableListOf<Product>()
                            val unmergedReleases = mutableListOf<Pair<String, List<Release>>>()
                            IndexMerger(mergerFile).use { indexMerger ->
                                ProgressInputStream(jarFile.getInputStream(indexEntry)) {
                                    callback(
                                        Stage.PROCESS,
                                        it,
                                        total
                                    )
                                }.use { it ->
                                    IndexV1Parser.parse(
                                        repository.id,
                                        it,
                                        object : IndexV1Parser.Callback {
                                            override fun onRepository(
                                                mirrors: List<String>,
                                                name: String,
                                                description: String,
                                                version: Int,
                                                timestamp: Long,
                                            ) {
                                                changedRepository = repository.update(
                                                    mirrors, name, description, version,
                                                    lastModified, entityTag, timestamp
                                                )
                                            }

                                            override fun onProduct(product: Product) {
                                                if (Thread.interrupted()) {
                                                    throw InterruptedException()
                                                }
                                                unmergedProducts += product
                                                if (unmergedProducts.size >= 50) {
                                                    indexMerger.addProducts(unmergedProducts)
                                                    unmergedProducts.clear()
                                                }
                                            }

                                            override fun onReleases(
                                                packageName: String,
                                                releases: List<Release>,
                                            ) {
                                                if (Thread.interrupted()) {
                                                    throw InterruptedException()
                                                }
                                                unmergedReleases += Pair(packageName, releases)
                                                if (unmergedReleases.size >= 50) {
                                                    indexMerger.addReleases(unmergedReleases)
                                                    unmergedReleases.clear()
                                                }
                                            }
                                        })

                                    if (Thread.interrupted()) {
                                        throw InterruptedException()
                                    }
                                    if (unmergedProducts.isNotEmpty()) {
                                        indexMerger.addProducts(unmergedProducts)
                                        unmergedProducts.clear()
                                    }
                                    if (unmergedReleases.isNotEmpty()) {
                                        indexMerger.addReleases(unmergedReleases)
                                        unmergedReleases.clear()
                                    }
                                    var progress = 0
                                    indexMerger.forEach(repository.id, 50) { products, totalCount ->
                                        if (Thread.interrupted()) {
                                            throw InterruptedException()
                                        }
                                        progress += products.size
                                        callback(
                                            Stage.MERGE,
                                            progress.toLong(),
                                            totalCount.toLong()
                                        )
                                        db.getProductTempDao().putTemporary(products
                                            .map {
                                                it.apply {
                                                    refreshReleases(features, unstable)
                                                    refreshVariables()
                                                }
                                            })
                                    }
                                }
                            }
                        } finally {
                            mergerFile.delete()
                        }
                        Pair(changedRepository, null)
                    }
                }

                val workRepository = changedRepository ?: repository
                if (workRepository.timestamp < repository.timestamp) {
                    throw UpdateException(
                        ErrorType.VALIDATION, "New index is older than current index: " +
                                "${workRepository.timestamp} < ${repository.timestamp}"
                    )
                } else {
                    val fingerprint = run {
                        val certificateFromJar = run {
                            val codeSigners = indexEntry.codeSigners
                            if (codeSigners == null || codeSigners.size != 1) {
                                throw UpdateException(
                                    ErrorType.VALIDATION,
                                    "index.jar must be signed by a single code signer"
                                )
                            } else {
                                val certificates =
                                    codeSigners[0].signerCertPath?.certificates.orEmpty()
                                if (certificates.size != 1) {
                                    throw UpdateException(
                                        ErrorType.VALIDATION,
                                        "index.jar code signer should have only one certificate"
                                    )
                                } else {
                                    certificates[0] as X509Certificate
                                }
                            }
                        }
                        val fingerprintFromJar = Utils.calculateFingerprint(certificateFromJar)
                        if (indexType.certificateFromIndex) {
                            val fingerprintFromIndex =
                                certificateFromIndex?.unhex()?.let(Utils::calculateFingerprint)
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
                    synchronized(cleanupLock) {
                        db.finishTemporary(
                            commitRepository,
                            true
                        )
                    }
                    rollback = false
                    true
                }
            } catch (e: Exception) {
                throw when (e) {
                    is UpdateException, is InterruptedException -> e
                    else                                        -> UpdateException(
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
}
