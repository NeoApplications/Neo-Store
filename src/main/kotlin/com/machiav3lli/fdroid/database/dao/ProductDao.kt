package com.machiav3lli.fdroid.database.dao

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
import com.machiav3lli.fdroid.ROW_CAN_UPDATE
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
import com.machiav3lli.fdroid.TABLE_CATEGORY_NAME
import com.machiav3lli.fdroid.TABLE_EXTRAS
import com.machiav3lli.fdroid.TABLE_EXTRAS_NAME
import com.machiav3lli.fdroid.TABLE_INSTALLED
import com.machiav3lli.fdroid.TABLE_INSTALLED_NAME
import com.machiav3lli.fdroid.TABLE_PRODUCT
import com.machiav3lli.fdroid.TABLE_PRODUCT_NAME
import com.machiav3lli.fdroid.TABLE_REPOSITORY
import com.machiav3lli.fdroid.TABLE_REPOSITORY_NAME
import com.machiav3lli.fdroid.database.QueryBuilder
import com.machiav3lli.fdroid.database.entity.CategoryTemp
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.ProductTemp
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

    @Query("SELECT * FROM product WHERE packageName = :packageName")
    fun get(packageName: String): List<Product>

    @Query("SELECT * FROM product WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<Product?>>

    @Query("DELETE FROM product WHERE repositoryId = :id")
    fun deleteById(id: Long): Int

    @RawQuery
    fun queryObject(query: SupportSQLiteQuery): List<Product>

    @Query("SELECT * FROM product WHERE author LIKE '%' || :author || '%' ")
    fun getAuthorPackagesFlow(author: String): Flow<List<Product>>

    fun queryObject(request: Request): List<Product> = queryObject(
        buildProductQuery(
            installed = request.installed,
            updates = request.updates,
            section = request.section,
            filteredOutRepos = request.filteredOutRepos,
            category = request.category,
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
        category: String = FILTER_CATEGORY_ALL, order: Order,
        ascending: Boolean, numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL,
        author: String = "",
    ): List<Product> = queryObject(
        buildProductQuery(
            installed = installed,
            updates = updates,
            section = section,
            filteredOutRepos = filteredOutRepos,
            category = category,
            order = order,
            ascending = ascending,
            numberOfItems = numberOfItems,
            updateCategory = updateCategory,
            author = author,
        )
    )

    @RawQuery(observedEntities = [Product::class])
    fun queryFlowList(query: SupportSQLiteQuery): Flow<List<Product>>

    fun queryFlowList(request: Request): Flow<List<Product>> = queryFlowList(
        buildProductQuery(
            installed = request.installed,
            updates = request.updates,
            section = request.section,
            filteredOutRepos = request.filteredOutRepos,
            category = request.category,
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
        order: Order,
        ascending: Boolean = false,
        numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL,
        author: String = "",
    ): SupportSQLiteQuery {
        val builder = QueryBuilder()

        // TODO improve signature matching logic
        val signatureMatches = """$TABLE_INSTALLED.$ROW_SIGNATURE IS NOT NULL AND
        $TABLE_PRODUCT.$ROW_SIGNATURES LIKE ('%' || $TABLE_INSTALLED.$ROW_SIGNATURE || '%') AND
        $TABLE_PRODUCT.$ROW_SIGNATURES != ''"""

        // Select the return fields
        builder += """SELECT $TABLE_PRODUCT.rowid AS $ROW_ID, $TABLE_PRODUCT.$ROW_REPOSITORY_ID,
        $TABLE_PRODUCT.$ROW_PACKAGE_NAME, $TABLE_PRODUCT.$ROW_LABEL,
        $TABLE_PRODUCT.$ROW_SUMMARY, $TABLE_PRODUCT.$ROW_DESCRIPTION,
        (COALESCE($TABLE_EXTRAS.$ROW_IGNORED_VERSION, -1) != $TABLE_PRODUCT.$ROW_VERSION_CODE AND
        COALESCE($TABLE_EXTRAS.$ROW_IGNORE_UPDATES, 0) = 0 AND $TABLE_PRODUCT.$ROW_COMPATIBLE != 0 AND
        $TABLE_PRODUCT.$ROW_VERSION_CODE > COALESCE($TABLE_INSTALLED.$ROW_VERSION_CODE, 0xffffffff) AND
        $signatureMatches) AS $ROW_CAN_UPDATE, $TABLE_PRODUCT.$ROW_ICON,
        $TABLE_PRODUCT.$ROW_METADATA_ICON, $TABLE_PRODUCT.$ROW_RELEASES, $TABLE_PRODUCT.$ROW_CATEGORIES,
        $TABLE_PRODUCT.$ROW_ANTIFEATURES, $TABLE_PRODUCT.$ROW_LICENSES, $TABLE_PRODUCT.$ROW_DONATES,
        $TABLE_PRODUCT.$ROW_SCREENSHOTS, $TABLE_PRODUCT.$ROW_VERSION_CODE,
        $TABLE_PRODUCT.$ROW_SUGGESTED_VERSION_CODE, $TABLE_PRODUCT.$ROW_SIGNATURES,
        $TABLE_PRODUCT.$ROW_COMPATIBLE, $TABLE_PRODUCT.$ROW_AUTHOR,
        $TABLE_PRODUCT.$ROW_SOURCE, $TABLE_PRODUCT.$ROW_WEB,
        $TABLE_PRODUCT.$ROW_TRACKER, $TABLE_PRODUCT.$ROW_CHANGELOG,
        $TABLE_PRODUCT.$ROW_WHATS_NEW,"""

        // Take product as main table
        builder += """MAX(($TABLE_PRODUCT.$ROW_COMPATIBLE AND
        ($TABLE_INSTALLED.$ROW_SIGNATURE IS NULL OR $signatureMatches)) ||
        PRINTF('%016X', $TABLE_PRODUCT.$ROW_VERSION_CODE)) FROM $TABLE_PRODUCT_NAME AS $TABLE_PRODUCT"""

        // Merge the matching repositories
        builder += """JOIN $TABLE_REPOSITORY_NAME AS $TABLE_REPOSITORY
        ON $TABLE_PRODUCT.$ROW_REPOSITORY_ID = $TABLE_REPOSITORY.$ROW_ID"""

        // Merge the matching extras
        builder += """LEFT JOIN $TABLE_EXTRAS_NAME AS $TABLE_EXTRAS
        ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_EXTRAS.$ROW_PACKAGE_NAME"""

        // Merge the matching installed
        if (!installed && !updates) builder += "LEFT"
        builder += """JOIN $TABLE_INSTALLED_NAME AS $TABLE_INSTALLED
        ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_INSTALLED.$ROW_PACKAGE_NAME"""

        // Merge the matching category
        builder += """JOIN $TABLE_CATEGORY_NAME AS $TABLE_CATEGORY
        ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_CATEGORY.$ROW_PACKAGE_NAME"""

        // Filter only active repositories
        builder += """WHERE $TABLE_REPOSITORY.$ROW_ENABLED != 0"""

        // Filter based on the developer
        if (author.isNotEmpty()) {
            builder += "AND $TABLE_PRODUCT.$ROW_AUTHOR = ?"
            builder %= author
        }

        // Filter out repositories
        if (filteredOutRepos.isNotEmpty()) {
            builder += "AND $TABLE_PRODUCT.$ROW_REPOSITORY_ID NOT IN(${filteredOutRepos.joinToString { "'$it'" }})"
        }

        // Filter out categories
        if (category != FILTER_CATEGORY_ALL) {
            builder += "AND $TABLE_CATEGORY.$ROW_LABEL = ?"
            builder %= category
        }

        // Filter only the selected repository/category
        when (section) {
            is Section.Category -> {
                builder += "AND $TABLE_CATEGORY.$ROW_LABEL = ?"
                builder %= section.name
            }
            is Section.Repository -> {
                builder += "AND $TABLE_PRODUCT.$ROW_REPOSITORY_ID = ?"
                builder %= section.id.toString()
            }
            is Section.FAVORITE -> {
                builder += "AND COALESCE($TABLE_EXTRAS.$ROW_FAVORITE, 0) != 0"
            }
            else -> {}
        }

        when (updateCategory) {
            UpdateCategory.ALL -> Unit
            UpdateCategory.NEW -> builder += """AND $TABLE_PRODUCT.$ROW_ADDED = $TABLE_PRODUCT.$ROW_UPDATED"""
            UpdateCategory.UPDATED -> builder += """AND $TABLE_PRODUCT.$ROW_ADDED < $TABLE_PRODUCT.$ROW_UPDATED"""
        }

        // Sum up all products with the same package name
        builder += "GROUP BY $TABLE_PRODUCT.$ROW_PACKAGE_NAME HAVING 1"

        // Filter if only can update
        if (updates) {
            builder += "AND $ROW_CAN_UPDATE"
        }

        // Set sorting order
        builder += "ORDER BY"
        when (order) {
            Order.NAME -> Unit
            Order.DATE_ADDED -> builder += "$TABLE_PRODUCT.$ROW_ADDED ${if (ascending) "ASC" else "DESC"},"
            Order.LAST_UPDATE -> builder += "$TABLE_PRODUCT.$ROW_UPDATED ${if (ascending) "ASC" else "DESC"},"
        }::class
        builder += "$TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ${if (!ascending && order == Order.NAME) "DESC" else "ASC"}${if (numberOfItems > 0) " LIMIT $numberOfItems" else ""}"

        return SimpleSQLiteQuery(builder.build(), builder.arguments.toTypedArray())
    }
}

@Dao
interface ProductTempDao : BaseDao<ProductTemp> {
    @get:Query("SELECT * FROM temporary_product")
    val all: Array<ProductTemp>

    @Query("DELETE FROM temporary_product")
    fun emptyTable()

    @Insert
    fun insertCategory(vararg product: CategoryTemp)

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

    @get:Query("SELECT * FROM extras")
    val all: List<Extras>

    @get:Query("SELECT * FROM extras")
    val allFlow: Flow<List<Extras>>

    @get:Query("SELECT packageName FROM extras WHERE favorite != 0")
    val favorites: Array<String>

    @get:Query("SELECT packageName FROM extras WHERE favorite != 0")
    val favoritesFlow: Flow<Array<String>>
}