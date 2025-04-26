package com.machiav3lli.fdroid.data.index

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.database.entity.asReleaseTemp
import com.machiav3lli.fdroid.data.index.v0.IndexV0Parser
import com.machiav3lli.fdroid.data.index.v1.IndexV1Merger
import com.machiav3lli.fdroid.data.index.v1.IndexV1Parser
import com.machiav3lli.fdroid.data.index.v2.IndexV2Parser
import com.machiav3lli.fdroid.manager.network.Downloader
import com.machiav3lli.fdroid.utils.CoroutineUtils
import com.machiav3lli.fdroid.utils.ProgressInputStream
import com.machiav3lli.fdroid.utils.extension.text.unhex
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.security.cert.Certificate
import java.security.cert.CertificateEncodingException
import java.security.cert.X509Certificate
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarFile

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
        INDEX_V1("index-v1.jar", "index-v1.json", false),
        INDEX_V2("", "index-v2.json", false),
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
            listOfNotNull(
                if (Preferences[Preferences.Key.IndexV2]) IndexType.INDEX_V2 else null,
                IndexType.INDEX_V1,
                IndexType.INDEX
            ),
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
                    if (result.statusCode == HttpStatusCode.NotFound && indexTypes.isNotEmpty()) {
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
                            "Invalid response: HTTP ${result.statusCode}"
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
    ): Pair<Downloader.Result, File> = withContext(Dispatchers.IO) {
        val file = Cache.getTemporaryFile(context)
        try {
            val result = Downloader.download(
                repository.address.toUri().buildUpon()
                    .appendPath(
                        if (indexType != IndexType.INDEX_V2) indexType.jarName
                        else indexType.contentName
                    )
                    .build().toString(),
                file,
                repository.lastModified,
                repository.entityTag,
                repository.authentication
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

    private fun processFile(
        context: Context,
        repository: Repository, indexType: IndexType, unstable: Boolean,
        file: File, lastModified: String, entityTag: String, callback: (Stage, Long, Long?) -> Unit,
    ): Boolean {
        var rollback = true
        val db = DatabaseX.getInstance(context)
        return synchronized(updaterLock) {
            try {
                val (jarFile, indexEntry) = if (indexType != IndexType.INDEX_V2) JarFile(file, true)
                    .let { Pair(it, it.getEntry(indexType.contentName) as JarEntry?) }
                else Pair(null, null)
                val indexStream = if (indexType == IndexType.INDEX_V2) file.inputStream()
                else null
                val total = if (indexType != IndexType.INDEX_V2) indexEntry?.size
                else file.length()
                db.getProductTempDao().emptyTable()
                db.getReleaseTempDao().emptyTable()
                db.getCategoryTempDao().emptyTable()

                val (changedRepository, certificateFromIndex) = when (indexType) {
                    IndexType.INDEX -> processIndexV0(
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

                    IndexType.INDEX_V2 -> processIndexV2(
                        context,
                        repository,
                        indexStream!!,
                        unstable,
                        lastModified,
                        entityTag,
                        total!!,
                        callback,
                    )
                }

                val workRepository = changedRepository ?: repository
                // TODO add better validation for Index-V2
                if (workRepository.timestamp < repository.timestamp) {
                    throw UpdateException(
                        ErrorType.VALIDATION, "New index is older than current index: " +
                                "${workRepository.timestamp} < ${repository.timestamp}"
                    )
                } else if (indexType != IndexType.INDEX_V2) {
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
                } else {
                    val valid = workRepository.fingerprint.isNotBlank()
                    commitChanges(workRepository, workRepository.fingerprint, callback)
                    rollback = !valid
                    valid
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
        val products = mutableListOf<Product>()
        val features = context.packageManager.systemAvailableFeatures
            .asSequence().map { it.name }.toSet() + setOf("android.hardware.touchscreen")

        val parser = IndexV0Parser(repository.id, object : IndexV0Parser.Callback {
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
                if (products.size >= 100) {
                    db.getProductTempDao().putTemporary(products)
                    db.getReleaseTempDao()
                        .insert(*(products.flatMap { it.releases }
                            .map { it.asReleaseTemp() }.toTypedArray()))
                    products.clear()
                }
            }
        })

        ProgressInputStream(inputStream) {
            callback(Stage.PROCESS, it, total)
        }.use { parser.parse(it) }

        if (Thread.interrupted()) {
            throw InterruptedException()
        }
        if (products.isNotEmpty()) {
            db.getProductTempDao().putTemporary(products)
            db.getReleaseTempDao()
                .insert(*(products.flatMap { it.releases }
                    .map { it.asReleaseTemp() }.toTypedArray()))
            products.clear()
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
            val unmergedProducts = mutableListOf<Product>()
            val unmergedReleases = mutableListOf<Pair<String, List<Release>>>()
            IndexV1Merger(mergerFile).use { indexMerger ->
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
                                    mirrors, name, description, version,
                                    lastModified, entityTag, timestamp
                                )
                            }

                            override fun onProduct(product: Product) {
                                if (Thread.interrupted()) {
                                    throw InterruptedException()
                                }
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
                                if (Thread.interrupted()) {
                                    throw InterruptedException()
                                }
                                unmergedReleases += Pair(packageName, releases)
                                if (unmergedReleases.size >= 100) {
                                    indexMerger.addReleases(unmergedReleases)
                                    unmergedReleases.clear()
                                }
                            }
                        }
                    ).parse(progressInputStream)

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
                    indexMerger.forEach(
                        repository.id,
                        100
                    ) { products, totalCount ->
                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        progress += products.size
                        callback(
                            Stage.MERGE,
                            progress.toLong(),
                            totalCount.toLong()
                        )
                        products.map {
                            it.apply {
                                refreshReleases(features, unstable)
                                refreshVariables()
                            }
                        }.let { updatedProducts ->
                            db.getProductTempDao().putTemporary(updatedProducts)
                            db.getReleaseTempDao()
                                .insert(*(updatedProducts.flatMap { it.releases }
                                    .map { it.asReleaseTemp() }.toTypedArray()))
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
            val unmergedProducts = mutableListOf<Product>()
            val unmergedReleases = mutableListOf<Pair<String, List<Release>>>()
            IndexV1Merger(mergerFile).use { indexMerger ->
                ProgressInputStream(inputStream) {
                    callback(
                        Stage.PROCESS,
                        it,
                        total
                    )
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
                                if (unmergedProducts.size >= 100) {
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
                                if (unmergedReleases.size >= 100) {
                                    indexMerger.addReleases(unmergedReleases)
                                    unmergedReleases.clear()
                                }
                            }
                        }
                    ).parse(progressInputStream)

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
                    indexMerger.forEach(
                        repository.id,
                        100
                    ) { products, totalCount ->
                        if (Thread.interrupted()) {
                            throw InterruptedException()
                        }
                        progress += products.size
                        callback(
                            Stage.MERGE,
                            progress.toLong(),
                            totalCount.toLong()
                        )
                        products.map {
                            it.apply {
                                refreshReleases(features, unstable)
                                refreshVariables()
                            }
                        }.let { updatedProducts ->
                            db.getProductTempDao().putTemporary(updatedProducts)
                            db.getReleaseTempDao()
                                .insert(*(updatedProducts.flatMap { it.releases }
                                    .map { it.asReleaseTemp() }.toTypedArray()))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(this::class.java.simpleName, e.message, e)
        } finally {
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
