package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.ROW_ADDED
import com.machiav3lli.fdroid.ROW_ANTIFEATURES
import com.machiav3lli.fdroid.ROW_AUTHOR
import com.machiav3lli.fdroid.ROW_CATEGORIES
import com.machiav3lli.fdroid.ROW_CHANGELOG
import com.machiav3lli.fdroid.ROW_COMPATIBLE
import com.machiav3lli.fdroid.ROW_DESCRIPTION
import com.machiav3lli.fdroid.ROW_DONATES
import com.machiav3lli.fdroid.ROW_ICON
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_LICENSES
import com.machiav3lli.fdroid.ROW_METADATA_ICON
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_RELEASES
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.ROW_SCREENSHOTS
import com.machiav3lli.fdroid.ROW_SIGNATURES
import com.machiav3lli.fdroid.ROW_SOURCE
import com.machiav3lli.fdroid.ROW_SUGGESTED_VERSION_CODE
import com.machiav3lli.fdroid.ROW_SUMMARY
import com.machiav3lli.fdroid.ROW_TRACKER
import com.machiav3lli.fdroid.ROW_UPDATED
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import com.machiav3lli.fdroid.ROW_WEB
import com.machiav3lli.fdroid.ROW_WHATS_NEW
import com.machiav3lli.fdroid.TABLE_PRODUCT
import com.machiav3lli.fdroid.database.entity.IconDetails
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.ProductTemp
import com.machiav3lli.fdroid.database.entity.ReleaseTemp
import com.machiav3lli.fdroid.database.entity.asProductTemp
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.entity.Request
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.entity.UpdateCategory
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.find
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDao(private val realm: Realm) : BaseDao<Product>(realm) {

    fun countForRepository(repoId: Long): Long =
        realm.query<Product>("repositoryId = $0", repoId)
            .count()
            .find()

    fun countForRepositoryFlow(repoId: Long): Flow<Long> =
        realm.query<Product>("repositoryId = $0", repoId)
            .count()
            .asFlow()

    fun productsForRepositoryFlow(repoId: Long): Flow<List<Product>> =
        realm.query<Product>("repositoryId = $0", repoId)
            .sort("label", Sort.ASCENDING)
            .asFlow()
            .map { it.list }

    fun exists(packageName: String): Boolean =
        realm.query<Product>("packageName = $0", packageName)
            .find()
            .isNotEmpty()

    fun get(packageName: String): List<Product> =
        realm.query<Product>("packageName = $0", packageName)
            .find()

    fun getFlow(packageName: String): Flow<List<Product>> =
        realm.query<Product>("packageName = $0", packageName)
            .asFlow()
            .mapLatest { it.list }

    fun get(packageName: String, repoId: Long): Product? =
        realm.query<Product>("packageName = $0 AND repositoryId = $1", packageName, repoId)
            .first()
            .find()

    fun getFlow(packageName: String, repoId: Long): Flow<Product?> =
        realm.query<Product>("packageName = $0 AND repositoryId = $1", packageName, repoId)
            .first()
            .asFlow()
            .mapLatest { it.obj }

    fun getIconDetails(): List<IconDetails> = realm.query<Product>()
        .distinct("packageName")
        .find { it.map { pr -> IconDetails(pr.packageName, pr.icon, pr.metadataIcon) } }


    fun getIconDetailsFlow(): Flow<List<IconDetails>> = realm.query<Product>()
        .distinct("packageName")
        .asFlow()
        .map { it.list.map { pr -> IconDetails(pr.packageName, pr.icon, pr.metadataIcon) } }

    fun deleteById(repoId: Long) = realm.writeBlocking {
        delete(query<Product>("repositoryId = $0", repoId))
    }

    fun getAllLicenses(): List<String> = realm.query<Product>()
        .distinct("licenses")
        .find { it.flatMap { pr -> pr.licenses }.distinct() }

    fun getAllLicensesFlow(): Flow<List<String>> = realm.query<Product>()
        .distinct("licenses")
        .asFlow()
        .map { it.list.flatMap { pr -> pr.licenses }.distinct() }

    fun getAuthorPackagesFlow(authorName: String): Flow<List<Product>> =
        realm.query<Product>("author.name = $0", authorName)
            .asFlow()
            .mapLatest { it.list }

    // TODO create micro queries replacing some of these calls
    //@Transaction
    fun queryObject(
        installed: Boolean, updates: Boolean,
        section: Section, filteredOutRepos: Set<Long> = emptySet(),
        category: String = FILTER_CATEGORY_ALL, filteredAntiFeatures: Set<String> = emptySet(),
        filteredLicenses: Set<String> = emptySet(),
        order: Order, ascending: Boolean, numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL,
        author: String = "",
    ): List<Product> = buildProductQuery(
        installed = installed,
        updates = updates,
        section = section,
        filteredOutRepos = filteredOutRepos,
        category = category,
        filteredAntiFeatures = filteredAntiFeatures,
        filteredLicenses = filteredLicenses,
        order = order,
        ascending = ascending,
        numberOfItems = numberOfItems,
        updateCategory = updateCategory,
        author = author,
    ).find()

    fun queryObject(request: Request): List<Product> = buildProductQuery(
        installed = request.installed,
        updates = request.updates,
        section = request.section,
        filteredOutRepos = request.filteredOutRepos.map { it.toLong() }.toSet(),
        category = request.category,
        filteredAntiFeatures = request.filteredAntiFeatures,
        filteredLicenses = request.filteredLicenses,
        order = request.order,
        ascending = request.ascending,
        numberOfItems = request.numberOfItems,
        updateCategory = request.updateCategory,
    ).find()

    fun queryFlowList(request: Request): Flow<List<Product>> = buildProductQuery(
        installed = request.installed,
        updates = request.updates,
        section = request.section,
        filteredOutRepos = request.filteredOutRepos.map { it.toLong() }.toSet(),
        category = request.category,
        filteredAntiFeatures = request.filteredAntiFeatures,
        filteredLicenses = request.filteredLicenses,
        order = request.order,
        ascending = request.ascending,
        numberOfItems = request.numberOfItems,
        updateCategory = request.updateCategory,
    ).asFlow()
        .map { it.list }

    fun buildProductQuery(
        installed: Boolean,
        updates: Boolean,
        section: Section,
        filteredOutRepos: Set<Long> = emptySet(),
        category: String = FILTER_CATEGORY_ALL,
        filteredAntiFeatures: Set<String> = emptySet(),
        filteredLicenses: Set<String> = emptySet(),
        order: Order,
        ascending: Boolean = false,
        numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL,
        author: String = "",
    ): RealmQuery<Product> {
        val query = realm.query<Product>()
            .sort("updated", Sort.ASCENDING)

        // Filtering
        //builder.addWhere("$TABLE_REPOSITORY.$ROW_ENABLED = 1")
        if (author.isNotEmpty()) {
            query.query("author.name = $0", author)
        }
        if (filteredOutRepos.isNotEmpty()) {
            query.query("repositoryId NOT IN $0", filteredOutRepos)
        }
        if (category != FILTER_CATEGORY_ALL) {
            query.query("$0 in categories", category)
        }
        if (filteredAntiFeatures.isNotEmpty())
            query.query("ANY antiFeatures NOT IN $0", filteredAntiFeatures)
        if (filteredLicenses.isNotEmpty())
            query.query("ANY licenses NOT IN $0", filteredLicenses)
        if (section == Section.FAVORITE) {
            query.query("extras.favorite", true) // TODO add Extras to Product
        }

        if (installed) {
            val installeds = realm.query<Installed>()
                .distinct("packageName")
                .find { it.map(Installed::packageName) }
            query.query("packageName in $0", installeds)
        }

        when (updateCategory) {
            UpdateCategory.ALL     -> Unit
            UpdateCategory.NEW     -> query.query("added = updated AND releases.@size > 0")
            UpdateCategory.UPDATED -> query.query("added < updated AND releases.@size > 0")
        }

        //query.distinct("packageName")

        if (updates) {
            // TODO
            query.query(
                "versionCode NOT IN extras.ignoredVersion AND extras.ignoredUpdates = $0 AND versionCode > installed.versionCode",
                false
            )
            /*builder.addWhere(
                """
                (COALESCE($TABLE_EXTRAS.$ROW_IGNORED_VERSION, -1) != $TABLE_PRODUCT.$ROW_VERSION_CODE AND
                COALESCE($TABLE_EXTRAS.$ROW_IGNORE_UPDATES, 0) = 0 AND $TABLE_PRODUCT.$ROW_COMPATIBLE != 0 AND
                $TABLE_PRODUCT.$ROW_VERSION_CODE > COALESCE($TABLE_INSTALLED.$ROW_VERSION_CODE, 0xffffffff) AND
                $signatureMatches)
                """
            )*/
        }

        val asc = if (ascending) Sort.ASCENDING
        else Sort.DESCENDING
        when (order) {
            Order.NAME        -> query.sort(Pair("label", asc))
            Order.DATE_ADDED  -> query.sort(Pair("added", asc), Pair("label", Sort.ASCENDING))
            Order.LAST_UPDATE -> query.sort(Pair("updated", asc), Pair("label", Sort.ASCENDING))
        }

        if (numberOfItems > 0)
            query.limit(numberOfItems)

        return query
    }

    /*fun buildProductQuery(
        installed: Boolean,
        updates: Boolean,
    ): SupportSQLiteQuery {
        val builder = QueryBuilder()

        // Selection
        builder += generateSelectFields()

        // TODO improve signature matching logic
        val signatureMatches = if (Preferences[Preferences.Key.DisableSignatureCheck]) "1"
        else """
            $TABLE_INSTALLED.$ROW_SIGNATURE IS NOT NULL AND
            $TABLE_PRODUCT.$ROW_SIGNATURES LIKE ('%' || $TABLE_INSTALLED.$ROW_SIGNATURE || '%') AND
            $TABLE_PRODUCT.$ROW_SIGNATURES != ''
            """

        builder += """
            MAX(($TABLE_PRODUCT.$ROW_COMPATIBLE AND
            ($TABLE_INSTALLED.$ROW_SIGNATURE IS NULL OR $signatureMatches)) ||
            PRINTF('%016X', $TABLE_PRODUCT.$ROW_VERSION_CODE))
        """

        builder.addFrom(TABLE_PRODUCT)

        // Joining
        builder.addJoin(
            TABLE_REPOSITORY,
            false,
            "$TABLE_PRODUCT.$ROW_REPOSITORY_ID = $TABLE_REPOSITORY.$ROW_ID",
        )
        builder.addJoin(
            TABLE_EXTRAS,
            true,
            "$TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_EXTRAS.$ROW_PACKAGE_NAME",
        )
        builder.addJoin(
            TABLE_INSTALLED,
            !installed && !updates,
            "$TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_INSTALLED.$ROW_PACKAGE_NAME",
        )
        builder.addJoin(
            TABLE_CATEGORY,
            false,
            "$TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_CATEGORY.$ROW_PACKAGE_NAME",
        )

        Log.v(this::class.simpleName, builder.build())
        return SimpleSQLiteQuery(builder.build(), builder.arguments.toTypedArray())
    }*/
}

private fun generateSelectFields(): String = """SELECT $TABLE_PRODUCT.$ROW_REPOSITORY_ID,
        $TABLE_PRODUCT.$ROW_PACKAGE_NAME, $TABLE_PRODUCT.$ROW_LABEL,
        $TABLE_PRODUCT.$ROW_SUMMARY, $TABLE_PRODUCT.$ROW_DESCRIPTION, MIN($TABLE_PRODUCT.$ROW_ADDED),
        $TABLE_PRODUCT.$ROW_UPDATED, $TABLE_PRODUCT.$ROW_ICON, $TABLE_PRODUCT.$ROW_METADATA_ICON,
        $TABLE_PRODUCT.$ROW_RELEASES, $TABLE_PRODUCT.$ROW_CATEGORIES, $TABLE_PRODUCT.$ROW_ANTIFEATURES,
        $TABLE_PRODUCT.$ROW_LICENSES, $TABLE_PRODUCT.$ROW_DONATES, $TABLE_PRODUCT.$ROW_SCREENSHOTS,
        $TABLE_PRODUCT.$ROW_VERSION_CODE, $TABLE_PRODUCT.$ROW_SUGGESTED_VERSION_CODE,
        $TABLE_PRODUCT.$ROW_SIGNATURES, $TABLE_PRODUCT.$ROW_COMPATIBLE,
        $TABLE_PRODUCT.$ROW_AUTHOR, $TABLE_PRODUCT.$ROW_SOURCE, $TABLE_PRODUCT.$ROW_WEB,
        $TABLE_PRODUCT.$ROW_TRACKER, $TABLE_PRODUCT.$ROW_CHANGELOG, $TABLE_PRODUCT.$ROW_WHATS_NEW,"""

@OptIn(ExperimentalCoroutinesApi::class)
class ProductTempDao(private val realm: Realm) : BaseDao<ProductTemp>(realm) {

    val all: RealmResults<ProductTemp>
        get() = realm.query<ProductTemp>()
            .find()

    val allFlow: Flow<RealmResults<ProductTemp>>
        get() = realm.query<ProductTemp>()
            .asFlow()
            .mapLatest { it.list }

    private fun insertRelease(vararg product: ReleaseTemp) = realm.writeBlocking {
        product.forEach(::copyToRealm)
    }

    fun putTemporary(products: List<Product>) {
        products.forEach {
            insert(it.asProductTemp())
        }
        /*releases.forEach { rel ->
            insertRelease(rel.asReleaseTemp())
        }*/
    }
}
