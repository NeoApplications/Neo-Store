package com.looker.droidify.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.looker.droidify.*
import com.looker.droidify.database.entity.*
import com.looker.droidify.entity.Order
import com.looker.droidify.entity.Section
import com.looker.droidify.entity.UpdateCategory
import com.looker.droidify.ui.fragments.Request

interface BaseDao<T> {
    @Insert
    fun insert(vararg product: T)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReplace(vararg product: T)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(vararg obj: T): Int

    @Delete
    fun delete(obj: T)
}

@Dao
interface RepositoryDao : BaseDao<Repository> {
    @get:Query("SELECT COUNT(_id) FROM repository")
    val count: Int

    fun put(repository: Repository): Repository {
        repository.let { item ->
            val newId = if (item.id > 0L) update(item).toLong() else returnInsert(item)
            return if (newId != repository.id) repository.copy(id = newId) else repository
        }
    }

    @Insert
    fun returnInsert(product: Repository): Long

    @Query("SELECT * FROM repository WHERE _id = :id")
    fun get(id: Long): Repository?

    @Query("SELECT * FROM repository WHERE _id = :id")
    fun getLive(id: Long): LiveData<Repository?>

    @get:Query("SELECT * FROM repository ORDER BY _id ASC")
    val all: List<Repository>

    @get:Query("SELECT * FROM repository ORDER BY _id ASC")
    val allLive: LiveData<List<Repository>>

    @get:Query("SELECT _id FROM repository WHERE enabled == 0 ORDER BY _id ASC")
    val allDisabled: List<Long>

    // TODO clean up products and other tables afterwards
    @Query("DELETE FROM repository WHERE _id = :id")
    fun deleteById(id: Long): Int
}

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
        (COALESCE($TABLE_LOCK.$ROW_VERSION_CODE, -1) NOT IN (0, $TABLE_PRODUCT.$ROW_VERSION_CODE) AND
        $TABLE_PRODUCT.$ROW_COMPATIBLE != 0 AND
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

        // Merge the matching locks
        builder += """LEFT JOIN $TABLE_LOCK_NAME AS $TABLE_LOCK
        ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_LOCK.$ROW_PACKAGE_NAME"""

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
interface ReleaseDao : BaseDao<Release> {
    // This one for the mode combining releases of different sources
    @Query("SELECT * FROM `release` WHERE packageName = :packageName")
    fun get(packageName: String): List<Release?>

    // This one for the separating releases of different sources
    @Query("SELECT * FROM `release` WHERE packageName = :packageName AND signature = :signature")
    fun get(packageName: String, signature: String): List<Release?>
}

@Dao
interface CategoryDao : BaseDao<Category> {
    @get:Query(
        """SELECT DISTINCT category.label
        FROM category AS category
        JOIN repository AS repository
        ON category.repositoryId = repository._id
        WHERE repository.enabled != 0"""
    )
    val allNames: List<String>

    @get:Query(
        """SELECT DISTINCT category.label
        FROM category AS category
        JOIN repository AS repository
        ON category.repositoryId = repository._id
        WHERE repository.enabled != 0"""
    )
    val allNamesLive: LiveData<List<String>>

    @Query("DELETE FROM category WHERE repositoryId = :id")
    fun deleteById(id: Long): Int
}

// TODO make sure that apps that not uninstalled by Droid-ify still get removed
@Dao
interface InstalledDao : BaseDao<Installed> {
    fun put(vararg installed: Installed) {
        installed.forEach { insertReplace(it) }
    }

    @get:Query("SELECT * FROM memory_installed")
    val allLive: LiveData<List<Installed>>

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    fun get(packageName: String): Installed?

    @Query("SELECT * FROM memory_installed WHERE packageName = :packageName")
    fun getLive(packageName: String): LiveData<Installed?>

    @Query("DELETE FROM memory_installed WHERE packageName = :packageName")
    fun delete(packageName: String)

    @Query("DELETE FROM memory_installed")
    fun emptyTable()
}

@Dao
interface LockDao : BaseDao<Lock> {
    @Query("DELETE FROM memory_lock WHERE packageName = :packageName")
    fun delete(packageName: String)
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
            insert(it.let { it.asProductTemp() })
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
interface CategoryTempDao : BaseDao<CategoryTemp> {
    @get:Query("SELECT * FROM temporary_category")
    val all: Array<CategoryTemp>

    @Query("DELETE FROM temporary_category")
    fun emptyTable()
}