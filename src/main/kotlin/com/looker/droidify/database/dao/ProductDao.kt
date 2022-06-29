package com.looker.droidify.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.looker.droidify.ROW_ADDED
import com.looker.droidify.ROW_ANTIFEATURES
import com.looker.droidify.ROW_CAN_UPDATE
import com.looker.droidify.ROW_CATEGORIES
import com.looker.droidify.ROW_COMPATIBLE
import com.looker.droidify.ROW_DESCRIPTION
import com.looker.droidify.ROW_DONATES
import com.looker.droidify.ROW_ENABLED
import com.looker.droidify.ROW_ICON
import com.looker.droidify.ROW_ID
import com.looker.droidify.ROW_IGNORED_VERSION
import com.looker.droidify.ROW_IGNORE_UPDATES
import com.looker.droidify.ROW_LABEL
import com.looker.droidify.ROW_LICENSES
import com.looker.droidify.ROW_MATCH_RANK
import com.looker.droidify.ROW_METADATA_ICON
import com.looker.droidify.ROW_PACKAGE_NAME
import com.looker.droidify.ROW_RELEASES
import com.looker.droidify.ROW_REPOSITORY_ID
import com.looker.droidify.ROW_SCREENSHOTS
import com.looker.droidify.ROW_SIGNATURE
import com.looker.droidify.ROW_SIGNATURES
import com.looker.droidify.ROW_SUMMARY
import com.looker.droidify.ROW_UPDATED
import com.looker.droidify.ROW_VERSION_CODE
import com.looker.droidify.TABLE_CATEGORY
import com.looker.droidify.TABLE_CATEGORY_NAME
import com.looker.droidify.TABLE_EXTRAS
import com.looker.droidify.TABLE_EXTRAS_NAME
import com.looker.droidify.TABLE_INSTALLED
import com.looker.droidify.TABLE_INSTALLED_NAME
import com.looker.droidify.TABLE_PRODUCT
import com.looker.droidify.TABLE_PRODUCT_NAME
import com.looker.droidify.TABLE_REPOSITORY
import com.looker.droidify.TABLE_REPOSITORY_NAME
import com.looker.droidify.database.QueryBuilder
import com.looker.droidify.database.entity.CategoryTemp
import com.looker.droidify.database.entity.Extras
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.ProductTemp
import com.looker.droidify.database.entity.asProductTemp
import com.looker.droidify.entity.Order
import com.looker.droidify.entity.Section
import com.looker.droidify.entity.UpdateCategory
import com.looker.droidify.ui.fragments.Request

@Dao
interface ProductDao : BaseDao<Product> {
    @Query("SELECT COUNT(*) FROM product WHERE repositoryId = :id")
    fun countForRepository(id: Long): Long

    @Query("SELECT COUNT(*) FROM product WHERE repositoryId = :id")
    fun countForRepositoryLive(id: Long): LiveData<Long>

    @Query("SELECT * FROM product WHERE packageName = :packageName")
    fun get(packageName: String): List<Product?>

    @Query("SELECT * FROM product WHERE packageName = :packageName")
    fun getLive(packageName: String): LiveData<List<Product?>>

    @Query("DELETE FROM product WHERE repositoryId = :id")
    fun deleteById(id: Long): Int

    @RawQuery
    fun queryObject(query: SupportSQLiteQuery): List<Product>

    fun queryObject(request: Request): List<Product> = queryObject(
        buildProductQuery(
            request.installed,
            request.updates,
            request.searchQuery,
            request.section,
            request.order,
            request.numberOfItems,
            request.updateCategory
        )
    )

    @Transaction
    fun queryObject(
        installed: Boolean, updates: Boolean, searchQuery: String,
        section: Section, order: Order, numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL
    ): List<Product> = queryObject(
        buildProductQuery(
            installed,
            updates,
            searchQuery,
            section,
            order,
            numberOfItems,
            updateCategory
        )
    )

    @RawQuery(observedEntities = [Product::class])
    fun queryLiveList(query: SupportSQLiteQuery): LiveData<List<Product>>

    fun queryLiveList(request: Request): LiveData<List<Product>> = queryLiveList(
        buildProductQuery(
            request.installed,
            request.updates,
            request.searchQuery,
            request.section,
            request.order,
            request.numberOfItems,
            request.updateCategory
        )
    )

    fun buildProductQuery(
        installed: Boolean,
        updates: Boolean,
        searchQuery: String,
        section: Section,
        order: Order,
        numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL
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
        $TABLE_PRODUCT.$ROW_SCREENSHOTS, $TABLE_PRODUCT.$ROW_VERSION_CODE,"""

        // Calculate the matching score with the search query
        if (searchQuery.isNotEmpty()) {
            builder += """((($TABLE_PRODUCT.$ROW_LABEL LIKE ? OR
          $TABLE_PRODUCT.$ROW_SUMMARY LIKE ?) * 7) |
          (($TABLE_PRODUCT.$ROW_PACKAGE_NAME LIKE ?) * 3) |
          ($TABLE_PRODUCT.$ROW_DESCRIPTION LIKE ?)) AS $ROW_MATCH_RANK,"""
            builder %= List(4) { "%$searchQuery%" }
        } else {
            builder += "0 AS $ROW_MATCH_RANK,"
        }

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
        if (section is Section.Category) {
            builder += """JOIN $TABLE_CATEGORY_NAME AS $TABLE_CATEGORY
          ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_CATEGORY.$ROW_PACKAGE_NAME"""
        }

        // Filter only active repositories
        builder += """WHERE $TABLE_REPOSITORY.$ROW_ENABLED != 0"""

        // Filter only the selected repository/category
        if (section is Section.Category) {
            builder += "AND $TABLE_CATEGORY.$ROW_LABEL = ?"
            builder %= section.name
        } else if (section is Section.Repository) {
            builder += "AND $TABLE_PRODUCT.$ROW_REPOSITORY_ID = ?"
            builder %= section.id.toString()
        }

        // Filter only apps that have some  matching score to the search query
        if (searchQuery.isNotEmpty()) {
            builder += """AND $ROW_MATCH_RANK > 0"""
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
        if (searchQuery.isNotEmpty()) builder += """$ROW_MATCH_RANK DESC,"""
        when (order) {
            Order.NAME -> Unit
            Order.DATE_ADDED -> builder += "$TABLE_PRODUCT.$ROW_ADDED DESC,"
            Order.LAST_UPDATE -> builder += "$TABLE_PRODUCT.$ROW_UPDATED DESC,"
        }::class
        builder += "$TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ASC${if (numberOfItems > 0) " LIMIT $numberOfItems" else ""}"

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
    fun getLive(packageName: String): LiveData<Extras?>

    @get:Query("SELECT * FROM extras")
    val all: List<Extras>

    @get:Query("SELECT * FROM extras")
    val allLive: LiveData<List<Extras>>

    @get:Query("SELECT packageName FROM extras WHERE favorite != 0")
    val favorites: Array<String>

    @get:Query("SELECT packageName FROM extras WHERE favorite != 0")
    val favoritesLive: LiveData<Array<String>>
}