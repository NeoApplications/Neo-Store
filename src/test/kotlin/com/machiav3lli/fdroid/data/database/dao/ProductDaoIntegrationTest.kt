package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureTemp
import com.machiav3lli.fdroid.data.database.entity.CategoryTemp
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.ProductTemp
import com.machiav3lli.fdroid.data.database.entity.ReleaseTemp
import com.machiav3lli.fdroid.data.database.entity.RepoCategoryTemp
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.database.entity.asReleaseTemp
import com.machiav3lli.fdroid.data.entity.Author
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Section
import com.machiav3lli.fdroid.data.index.v1.IndexV1
import com.machiav3lli.fdroid.data.index.v1.toRelease
import com.machiav3lli.fdroid.data.index.v2.IndexV2
import com.machiav3lli.fdroid.data.index.v2.toRelease
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductDaoIntegrationTest {
    private val db: DatabaseX = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        DatabaseX::class.java
    ).allowMainThreadQueries()
        .build()

    private val repositoryDao get() = db.getRepositoryDao()
    private val productDao get() = db.getProductDao()

    private val jsonConfig = Json { ignoreUnknownKeys = true }

    @BeforeEach
    fun setUp() = runBlocking { clearDatabase() }

    @Test
    @DisplayName("buildProductQuery(): basic query returns every product of the enabled repository")
    fun basicQueryReturnsAllProducts() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)

        val results = query()

        assertEquals(
            listOf(PACKAGE_ORFOX, PACKAGE_PROOFMODE, PACKAGE_RIPPLE),
            results.packageNames(),
        )
    }

    @Test
    @DisplayName("buildProductQuery(): Section.NONE short-circuits to an empty query")
    fun sectionNoneReturnsNoProducts() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)

        val built = productDao.buildProductQuery(
            installed = false,
            updates = false,
            section = Section.NONE,
            order = Order.NAME,
        )

        assertTrue(built.sql.contains("LIMIT 0"), "Section.NONE must build a LIMIT 0 query")
        assertEquals(emptyList<String>(), productDao.queryObject(built).packageNames())
    }

    @Test
    @DisplayName("buildProductQuery(): Section.FAVORITE returns only favourited products")
    fun favoriteSectionReturnsOnlyFavorites() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)
        db.getExtrasDao().insert(Extras(packageName = PACKAGE_ORFOX, favorite = true))

        val results = query(section = Section.FAVORITE)

        assertEquals(listOf(PACKAGE_ORFOX), results.packageNames())
    }

    @Test
    @DisplayName("buildProductQuery(): specificPackages builds an IN clause and filters the results")
    fun specificPackagesFiltersResults() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)

        val built = productDao.buildProductQuery(
            installed = false,
            updates = false,
            section = Section.All,
            specificPackages = setOf(PACKAGE_PROOFMODE),
            order = Order.NAME,
        )

        assertTrue(
            built.sql.contains("IN ('$PACKAGE_PROOFMODE')"),
            "specificPackages must be rendered as an IN clause",
        )
        assertEquals(listOf(PACKAGE_PROOFMODE), productDao.queryObject(built).packageNames())
    }

    @Test
    @DisplayName("buildProductQuery(): category filter joins the category table")
    fun categoryFilterReturnsMatchingProducts() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)

        val results = query(category = "Internet")

        assertEquals(listOf(PACKAGE_ORFOX), results.packageNames())
    }

    @Test
    @DisplayName("buildProductQuery(): updates returns installed packages with a newer compatible release")
    fun updatesFilterReturnsUpdatablePackages() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)
        db.getInstalledDao().insert(
            Installed(
                packageName = PACKAGE_ORFOX,
                version = "16",
                versionCode = 16,
                signatures = listOf(ORFOX_SIGNER),
            ),
            Installed(
                packageName = PACKAGE_RIPPLE,
                version = "0.3.1",
                versionCode = 168,
                signatures = listOf(RIPPLE_SIGNER),
            ),
        )

        val results = query(installed = true, updates = true)

        assertEquals(listOf(PACKAGE_ORFOX), results.packageNames())
    }

    @Test
    @DisplayName("getInstalledProductsWithVulnerabilities(): returns installed products with a vulnerable release")
    fun installedVulnerableProductsAreReturned() = runBlocking {
        val repository = syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)
        db.getInstalledDao().insert(
            Installed(
                packageName = PACKAGE_ORFOX,
                version = "Orfox-Final-RIP",
                versionCode = 17,
                signatures = listOf(ORFOX_SIGNER),
            ),
        )

        val results = productDao.getInstalledProductsWithVulnerabilities(repository.id)

        assertEquals(listOf(PACKAGE_ORFOX), results.packageNames())
    }

    @Test
    @DisplayName("getInstalledProductsWithVulnerabilities(): returns nothing when nothing is installed")
    fun installedVulnerableProductsRequireInstalled() = runBlocking {
        val repository = syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)

        val results = productDao.getInstalledProductsWithVulnerabilities(repository.id)

        assertEquals(emptyList(), results.packageNames())
    }

    @Test
    @DisplayName("getInstalledProductsWithVulnerabilities(): honours the ignoreVulns extra")
    fun installedVulnerableProductsRespectIgnoreVulns() = runBlocking {
        val repository = syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)
        db.getInstalledDao().insert(
            Installed(
                packageName = PACKAGE_ORFOX,
                version = "Orfox-Final-RIP",
                versionCode = 17,
                signatures = listOf(ORFOX_SIGNER),
            ),
        )
        db.getExtrasDao().insert(Extras(packageName = PACKAGE_ORFOX, ignoreVulns = true))

        val results = productDao.getInstalledProductsWithVulnerabilities(repository.id)

        assertEquals(emptyList(), results.packageNames())
    }

    @Test
    @DisplayName("end-toend: cross-repository ranking picks the highest version across enabled repos")
    fun crossRepositoryRankingPrefersHighestVersion() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V1)
        val archive = syncRepository(guardianArchiveRepository(), GUARDIAN_ARCHIVE_INDEX_V1)

        val results = query()

        assertEquals(
            listOf(PACKAGE_ORFOX, PACKAGE_PROOFMODE, PACKAGE_RIPPLE),
            results.packageNames(),
        )
        val ripple = results.single { it.product.packageName == PACKAGE_RIPPLE }
        assertEquals(archive.id, ripple.product.repositoryId)
    }

    @Test
    @DisplayName("end-to-end: index-v2 fixture is parsed, inserted and queryable")
    fun v2IndexIsParsedAndQueryable() = runBlocking {
        syncRepository(guardianRepository(), GUARDIAN_INDEX_V2, isV2 = true)

        val results = query()

        assertEquals(
            listOf(PACKAGE_ORFOX, PACKAGE_PROOFMODE, PACKAGE_RIPPLE),
            results.packageNames(),
        )
    }

    private suspend fun syncRepository(
        repository: Repository,
        indexResource: String,
        isV2: Boolean = false,
    ): Repository {
        val repositoryId = repositoryDao.insertReturn(repository.copy(id = 0))
        val stored = repository.copy(id = repositoryId)

        val json = loadResource(indexResource)
        val parsed =
            if (isV2) parseIndexV2(json, repositoryId) else parseIndexV1(json, repositoryId)

        clearTempTables()
        insertTemp(parsed)
        db.finishTemporary(stored, success = true)

        return stored
    }

    private suspend fun insertTemp(parsed: ParsedIndex) {
        db.getProductTempDao().insert(*parsed.products.toTypedArray())
        db.getReleaseTempDao().insert(*parsed.releases.toTypedArray())
        db.getCategoryTempDao().insert(*parsed.categories.toTypedArray())
        if (parsed.repoCategories.isNotEmpty()) {
            db.getRepoCategoryTempDao().insert(*parsed.repoCategories.toTypedArray())
        }
        if (parsed.antiFeatures.isNotEmpty()) {
            db.getAntiFeatureTempDao().insert(*parsed.antiFeatures.toTypedArray())
        }
    }

    private fun parseIndexV1(json: String, repositoryId: Long): ParsedIndex {
        val index = jsonConfig.decodeFromString<IndexV1>(json)

        val products = index.apps.map { app ->
            ProductTemp(
                repositoryId = repositoryId,
                packageName = app.packageName,
                label = app.name,
                summary = app.summary,
                description = app.description,
                added = app.added,
                updated = app.lastUpdated,
                icon = app.icon,
                metadataIcon = "",
                categories = app.categories,
                antiFeatures = app.antiFeatures,
                licenses = app.license.split(',').filter(String::isNotEmpty),
                donates = emptyList(),
                screenshots = emptyList(),
                suggestedVersionCode = app.suggestedVersionCode.toLongOrNull() ?: 0L,
                author = Author(app.authorName, app.authorEmail, app.authorWebSite),
                source = app.sourceCode,
                web = app.webSite,
                video = "",
                tracker = app.issueTracker,
                translation = app.translation,
                changelog = app.changelog,
                whatsNew = "",
            )
        }
        val releases = index.packages.flatMap { (packageName, packages) ->
            packages.map { it.toReleaseTemp(repositoryId, packageName) }
        }
        val categories = index.apps.flatMap { app ->
            app.categories.distinct().map { CategoryTemp(repositoryId, app.packageName, it) }
        }

        return ParsedIndex(products, releases, categories)
    }

    private fun parseIndexV2(json: String, repositoryId: Long): ParsedIndex {
        val index = jsonConfig.decodeFromString<IndexV2>(json)

        val products = index.packages.map { (packageName, pkg) ->
            val metadata = pkg.metadata
            ProductTemp(
                repositoryId = repositoryId,
                packageName = packageName,
                label = metadata.name.localized(packageName),
                summary = metadata.summary.localized(""),
                description = metadata.description.localized(""),
                added = metadata.added,
                updated = metadata.lastUpdated,
                icon = metadata.icon.localized(IndexV2.File("")).name,
                metadataIcon = metadata.icon.localized(IndexV2.File("")).name,
                categories = metadata.categories,
                antiFeatures = pkg.versions.values.maxByOrNull { it.added }
                    ?.antiFeatures?.keys?.toList().orEmpty(),
                licenses = metadata.license.split(',').filter(String::isNotEmpty),
                donates = emptyList(),
                screenshots = emptyList(),
                suggestedVersionCode = 0L,
                author = Author(
                    metadata.authorName.orEmpty(),
                    metadata.authorEmail.orEmpty(),
                    metadata.authorWebsite.orEmpty(),
                ),
                source = metadata.sourceCode.orEmpty(),
                web = metadata.webSite.orEmpty(),
                video = metadata.video.localized(""),
                tracker = metadata.issueTracker.orEmpty(),
                translation = metadata.translation.orEmpty(),
                changelog = metadata.changelog.orEmpty(),
                whatsNew = "",
            )
        }
        val releases = index.packages.flatMap { (packageName, pkg) ->
            pkg.versions.values.map { it.toReleaseTemp(repositoryId, packageName) }
        }
        val categories = index.packages.flatMap { (packageName, pkg) ->
            pkg.metadata.categories.distinct().map { CategoryTemp(repositoryId, packageName, it) }
        }
        val repoCategories = index.repo.categories.map { (name, category) ->
            RepoCategoryTemp(
                repositoryId = repositoryId,
                name = name,
                label = category.name.localized(name),
                icon = category.icon.localized(IndexV2.File("")).name,
            )
        }
        val antiFeatures = index.repo.antiFeatures.map { (name, antiFeature) ->
            AntiFeatureTemp(
                repositoryId = repositoryId,
                name = name,
                label = antiFeature.name.localized(name),
                description = antiFeature.description.localized(""),
                icon = antiFeature.icon.localized(IndexV2.File("")).name,
            )
        }

        return ParsedIndex(products, releases, categories, repoCategories, antiFeatures)
    }

    private suspend fun query(
        installed: Boolean = false,
        updates: Boolean = false,
        section: Section = Section.All,
        category: String = FILTER_CATEGORY_ALL,
        specificPackages: Set<String> = emptySet(),
        filteredAntiFeatures: Set<String> = emptySet(),
        filteredLicenses: Set<String> = emptySet(),
        order: Order = Order.NAME,
        ascending: Boolean = true,
        numberOfItems: Int = 0,
    ): List<EmbeddedProduct> = productDao.queryObject(
        productDao.buildProductQuery(
            installed = installed,
            updates = updates,
            section = section,
            category = category,
            specificPackages = specificPackages,
            filteredAntiFeatures = filteredAntiFeatures,
            filteredLicenses = filteredLicenses,
            order = order,
            ascending = ascending,
            numberOfItems = numberOfItems,
        ),
    )

    private fun List<EmbeddedProduct>.packageNames(): List<String> =
        map { it.product.packageName }

    private fun IndexV1.Package.toReleaseTemp(repositoryId: Long, packageName: String) =
        toRelease(repositoryId, packageName).asReleaseTemp()

    private fun IndexV2.Version.toReleaseTemp(repositoryId: Long, packageName: String) =
        toRelease(repositoryId, packageName).asReleaseTemp()

    private suspend fun clearDatabase() {
        db.getProductDao().emptyTable()
        db.getReleaseDao().emptyTable()
        db.getCategoryDao().emptyTable()
        db.getRepoCategoryDao().emptyTable()
        db.getAntiFeatureDao().emptyTable()
        db.getInstalledDao().emptyTable()
        db.getExtrasDao().getAll().forEach { db.getExtrasDao().delete(it.packageName) }
        db.getRepositoryDao().emptyTable()
        clearTempTables()
    }

    private suspend fun clearTempTables() {
        db.getProductTempDao().emptyTable()
        db.getReleaseTempDao().emptyTable()
        db.getCategoryTempDao().emptyTable()
        db.getRepoCategoryTempDao().emptyTable()
        db.getAntiFeatureTempDao().emptyTable()
    }

    private fun guardianRepository() = Repository(
        address = "https://guardianproject.info/fdroid/repo",
        name = "Guardian Project",
        enabled = true,
    )

    private fun guardianArchiveRepository() = Repository(
        address = "https://guardianproject.info/fdroid/archive",
        name = "Guardian Project Archive",
        enabled = true,
    )

    private fun loadResource(path: String): String =
        checkNotNull(javaClass.classLoader?.getResourceAsStream(path)) {
            "Missing test resource: $path"
        }.bufferedReader().use { it.readText() }

    private data class ParsedIndex(
        val products: List<ProductTemp>,
        val releases: List<ReleaseTemp>,
        val categories: List<CategoryTemp>,
        val repoCategories: List<RepoCategoryTemp> = emptyList(),
        val antiFeatures: List<AntiFeatureTemp> = emptyList(),
    )

    private companion object {
        const val PACKAGE_ORFOX = "info.guardianproject.orfox"
        const val PACKAGE_PROOFMODE = "org.witness.proofmode"
        const val PACKAGE_RIPPLE = "info.guardianproject.ripple"

        const val ORFOX_SIGNER = "f006a20481c71a690de02e385ab0c9fa4ac1245240f68102682703ba0656867a"
        const val RIPPLE_SIGNER = "927f7e38b6acbecd84e02dace33efa9a7a2f0979750f28f585688ee38b3a4e28"

        const val GUARDIAN_INDEX_V1 = "test-data/guardian-index-v1.json"
        const val GUARDIAN_INDEX_V2 = "test-data/guardian-index-v2.json"
        const val GUARDIAN_ARCHIVE_INDEX_V1 = "test-data/guardian-index-v1-archive.json"
    }
}

private fun <T> Map<String, T>?.localized(fallback: T): T =
    this?.get("en") ?: this?.get("en-US") ?: this?.values?.firstOrNull() ?: fallback
