package com.machiav3lli.fdroid.database.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.ROW_ADDED
import com.machiav3lli.fdroid.ROW_ANTIFEATURES
import com.machiav3lli.fdroid.ROW_AUTHOR
import com.machiav3lli.fdroid.ROW_CATEGORIES
import com.machiav3lli.fdroid.ROW_CHANGELOG
import com.machiav3lli.fdroid.ROW_COMPATIBLE
import com.machiav3lli.fdroid.ROW_DESCRIPTION
import com.machiav3lli.fdroid.ROW_DONATES
import com.machiav3lli.fdroid.ROW_ENABLED
import com.machiav3lli.fdroid.ROW_FAVORITE
import com.machiav3lli.fdroid.ROW_ICON
import com.machiav3lli.fdroid.ROW_ID
import com.machiav3lli.fdroid.ROW_IGNORED_VERSION
import com.machiav3lli.fdroid.ROW_IGNORE_UPDATES
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_LICENSES
import com.machiav3lli.fdroid.ROW_METADATA_ICON
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_RELEASES
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.ROW_SCREENSHOTS
import com.machiav3lli.fdroid.ROW_SIGNATURE
import com.machiav3lli.fdroid.ROW_SIGNATURES
import com.machiav3lli.fdroid.ROW_SOURCE
import com.machiav3lli.fdroid.ROW_SUGGESTED_VERSION_CODE
import com.machiav3lli.fdroid.ROW_SUMMARY
import com.machiav3lli.fdroid.ROW_TRACKER
import com.machiav3lli.fdroid.ROW_UPDATED
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import com.machiav3lli.fdroid.ROW_WEB
import com.machiav3lli.fdroid.ROW_WHATS_NEW
import com.machiav3lli.fdroid.TABLE_CATEGORY
import com.machiav3lli.fdroid.TABLE_EXTRAS
import com.machiav3lli.fdroid.TABLE_INSTALLED
import com.machiav3lli.fdroid.TABLE_PRODUCT
import com.machiav3lli.fdroid.TABLE_REPOSITORY
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.QueryBuilder
import com.machiav3lli.fdroid.database.entity.Category
import com.machiav3lli.fdroid.database.entity.CategoryTemp
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.IconDetails
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Licenses
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.ProductTemp
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.database.entity.asProductTemp
import com.machiav3lli.fdroid.entity.Order
import com.machiav3lli.fdroid.entity.Request
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.entity.UpdateCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao : BaseDao<Product> {
    @Query("SELECT COUNT(*) FROM product WHERE repositoryId = :id")
    fun countForRepository(id: Long): Long

    @Query("SELECT COUNT(*) FROM product WHERE repositoryId = :id")
    fun countForRepositoryLive(id: Long): LiveData<Long>

    @Query("SELECT COUNT(*) FROM product WHERE repositoryId = :id")
    fun countForRepositoryFlow(id: Long): Flow<Long>

    @Query("SELECT * FROM product WHERE repositoryId = :repoId ORDER BY label")
    fun productsForRepositoryFlow(repoId: Long): Flow<List<Product>>

    @Query("SELECT EXISTS(SELECT 1 FROM product WHERE packageName = :packageName)")
    fun exists(packageName: String): Boolean

    @Query("SELECT * FROM product WHERE packageName = :packageName")
    fun get(packageName: String): List<Product>

    @Query("SELECT * FROM product WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<Product>>

    @Query("SELECT * FROM product WHERE packageName = :packageName AND repositoryId = :repoId")
    fun get(packageName: String, repoId: Long): Product?

    @Query("SELECT * FROM product WHERE packageName = :packageName AND repositoryId = :repoId")
    fun getFlow(packageName: String, repoId: Long): Flow<Product?>


    @Query("SELECT packageName, icon, metadataIcon FROM product GROUP BY packageName HAVING 1")
    fun getIconDetails(): List<IconDetails>


    @Query("SELECT packageName, icon, metadataIcon FROM product GROUP BY packageName HAVING 1")
    fun getIconDetailsFlow(): Flow<List<IconDetails>>

    @Query("DELETE FROM product WHERE repositoryId = :id")
    fun deleteById(id: Long): Int

    @Query("SELECT DISTINCT licenses FROM product")
    fun getAllLicenses(): List<Licenses>

    @Query("SELECT DISTINCT licenses FROM product")
    fun getAllLicensesFlow(): Flow<List<Licenses>>

    @Query("SELECT * FROM product WHERE author LIKE '%' || :author || '%' ")
    fun getAuthorPackagesFlow(author: String): Flow<List<Product>>

    @RawQuery
    fun queryObject(query: SupportSQLiteQuery): List<Product>

    fun queryObject(request: Request): List<Product> = queryObject(
        buildProductQuery(
            installed = request.installed,
            updates = request.updates,
            section = request.section,
            filteredOutRepos = request.filteredOutRepos,
            category = request.category,
            filteredAntiFeatures = request.filteredAntiFeatures,
            filteredLicenses = request.filteredLicenses,
            order = request.order,
            ascending = request.ascending,
            numberOfItems = request.numberOfItems,
            updateCategory = request.updateCategory
        )
    )

    @Transaction
    fun queryObject(
        installed: Boolean, updates: Boolean,
        section: Section, filteredOutRepos: Set<String> = emptySet(),
        category: String = FILTER_CATEGORY_ALL, filteredAntiFeatures: Set<String> = emptySet(),
        filteredLicenses: Set<String> = emptySet(),
        order: Order, ascending: Boolean, numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL,
        author: String = "",
    ): List<Product> = queryObject(
        buildProductQuery(
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
        )
    )

    @RawQuery(observedEntities = [Product::class, Installed::class, Extras::class, Repository::class, Category::class])
    fun queryFlowList(query: SupportSQLiteQuery): Flow<List<Product>>

    fun queryFlowList(request: Request): Flow<List<Product>> = queryFlowList(
        buildProductQuery(
            installed = request.installed,
            updates = request.updates,
            section = request.section,
            filteredOutRepos = request.filteredOutRepos,
            category = request.category,
            filteredAntiFeatures = request.filteredAntiFeatures,
            filteredLicenses = request.filteredLicenses,
            order = request.order,
            ascending = request.ascending,
            numberOfItems = request.numberOfItems,
            updateCategory = request.updateCategory,
        )
    )

    fun buildProductQuery(
        installed: Boolean,
        updates: Boolean,
        section: Section,
        filteredOutRepos: Set<String> = emptySet(),
        category: String = FILTER_CATEGORY_ALL,
        filteredAntiFeatures: Set<String> = emptySet(),
        filteredLicenses: Set<String> = emptySet(),
        order: Order,
        ascending: Boolean = false,
        numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL,
        author: String = "",
    ): SupportSQLiteQuery {
        val builder = QueryBuilder()

        if (section == Section.NONE) {
            builder += "SELECT * FROM $TABLE_PRODUCT LIMIT 0"
            return SimpleSQLiteQuery(builder.build(), builder.arguments.toTypedArray())
        }

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
            true,
            "$TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_CATEGORY.$ROW_PACKAGE_NAME",
        )

        // Filtering
        builder.addWhere("$TABLE_REPOSITORY.$ROW_ENABLED = 1")
        if (author.isNotEmpty()) {
            builder.addWhere("$TABLE_PRODUCT.$ROW_AUTHOR = ?").addArgument(author)
        }
        if (filteredOutRepos.isNotEmpty()) {
            builder.addWhere("$TABLE_PRODUCT.$ROW_REPOSITORY_ID NOT IN (${filteredOutRepos.joinToString { it }})")
        }
        if (category != FILTER_CATEGORY_ALL) {
            builder.addWhere("$TABLE_CATEGORY.$ROW_LABEL = ?").addArgument(category)
        }
        filteredAntiFeatures.forEach {
            builder.addWhere("$TABLE_PRODUCT.$ROW_ANTIFEATURES NOT LIKE '%$it%'")
        }
        filteredLicenses.forEach {
            builder.addWhere("$TABLE_PRODUCT.$ROW_LICENSES NOT LIKE '%$it%'")
        }
        if (section == Section.FAVORITE) {
            builder.addWhere("COALESCE($TABLE_EXTRAS.$ROW_FAVORITE, 0) != 0")
        }
        builder.addWhere("$TABLE_PRODUCT.$ROW_REPOSITORY_ID NOT LIKE '%[^0-9]%'")

        when (updateCategory) {
            UpdateCategory.ALL     -> Unit
            UpdateCategory.NEW     -> {
                builder.addWhere("$TABLE_PRODUCT.$ROW_ADDED = $TABLE_PRODUCT.$ROW_UPDATED") // TODO fix for multiple sources
                builder.addWhere("$TABLE_PRODUCT.$ROW_RELEASES NOT LIKE '%|%'")
            }

            UpdateCategory.UPDATED -> builder.addWhere("$TABLE_PRODUCT.$ROW_ADDED < $TABLE_PRODUCT.$ROW_UPDATED")
        }

        builder.addGroupBy("$TABLE_PRODUCT.$ROW_PACKAGE_NAME")

        if (updates) {
            builder.addWhere(
                """
                (COALESCE($TABLE_EXTRAS.$ROW_IGNORED_VERSION, -1) != $TABLE_PRODUCT.$ROW_VERSION_CODE AND
                COALESCE($TABLE_EXTRAS.$ROW_IGNORE_UPDATES, 0) = 0 AND $TABLE_PRODUCT.$ROW_COMPATIBLE != 0 AND
                $TABLE_PRODUCT.$ROW_VERSION_CODE > COALESCE($TABLE_INSTALLED.$ROW_VERSION_CODE, 0xffffffff) AND
                $signatureMatches)
                """
            )
        }

        // Ordering
        builder.addOrderBy(
            when (order) {
                Order.NAME        -> "$TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ${if (!ascending) "DESC" else "ASC"}"
                Order.DATE_ADDED  -> "$TABLE_PRODUCT.$ROW_ADDED ${if (ascending) "ASC" else "DESC"}, $TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ASC"
                Order.LAST_UPDATE -> "$TABLE_PRODUCT.$ROW_UPDATED ${if (ascending) "ASC" else "DESC"}, $TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ASC"
            }.let {
                "$it ${if (numberOfItems > 0) " LIMIT $numberOfItems" else ""}"
            }
        )

        Log.v(this::class.simpleName, builder.build())
        return SimpleSQLiteQuery(builder.build(), builder.arguments.toTypedArray())
    }
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

@Dao
interface ProductTempDao : BaseDao<ProductTemp> {
    @Query("SELECT * FROM temporary_product")
    fun getAll(): Array<ProductTemp>

    @Query("DELETE FROM temporary_product")
    fun emptyTable()

    @Insert
    fun insertCategory(vararg product: CategoryTemp)

    //@Insert
    //fun insertRelease(vararg product: ReleaseTemp)

    @Transaction
    fun putTemporary(products: List<Product>) {
        products.forEach {
            insert(it.asProductTemp())
            it.categories.forEach { category ->
                insertCategory(CategoryTemp().apply {
                    repositoryId = it.repositoryId
                    packageName = it.packageName
                    label = category
                })
            }
            /*it.releases.forEach { rel ->
                insertRelease(rel.asReleaseTemp())
            }*/
        }
    }
}

@Dao
interface ExtrasDao : BaseDao<Extras> {
    @Query("DELETE FROM extras WHERE packageName = :packageName")
    fun delete(packageName: String)

    @Query("SELECT * FROM extras WHERE packageName = :packageName")
    operator fun get(packageName: String): Extras?

    @Query("SELECT * FROM extras WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<Extras?>

    @Query("SELECT * FROM extras")
    fun getAll(): List<Extras>

    @Query("SELECT * FROM extras")
    fun getAllFlow(): Flow<List<Extras>>

    @Query("SELECT packageName FROM extras WHERE favorite != 0")
    fun getFavorites(): Array<String>

    @Query("SELECT packageName FROM extras WHERE favorite != 0")
    fun getFavoritesFlow(): Flow<Array<String>>
}