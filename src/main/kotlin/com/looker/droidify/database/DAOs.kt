package com.looker.droidify.database

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.looker.droidify.*
import com.looker.droidify.database.entity.*
import com.looker.droidify.entity.Order
import com.looker.droidify.entity.Section

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
    @Query("SELECT COUNT(*) FROM product WHERE repository_id = :id")
    fun countForRepository(id: Long): Long

    @Query("SELECT COUNT(*) FROM product WHERE repository_id = :id")
    fun countForRepositoryLive(id: Long): LiveData<Long>

    @Query("SELECT * FROM product WHERE package_name = :packageName")
    fun get(packageName: String): List<Product?>

    @Query("SELECT * FROM product WHERE package_name = :packageName")
    fun getLive(packageName: String): LiveData<List<Product?>>

    @Query("DELETE FROM product WHERE repository_id = :id")
    fun deleteById(id: Long): Int

    @RawQuery
    fun queryObject(query: SupportSQLiteQuery): List<Product>

    @Transaction
    fun queryObject(
        installed: Boolean, updates: Boolean, searchQuery: String,
        section: Section, order: Order, numberOfItems: Int = 0
    ): List<Product> = queryObject(
        buildProductQuery(installed, updates, searchQuery, section, order, numberOfItems)
    )

    @RawQuery(observedEntities = [Product::class])
    fun queryLiveList(query: SupportSQLiteQuery): LiveData<List<Product>>

    fun queryLiveList(
        installed: Boolean, updates: Boolean, searchQuery: String,
        section: Section, order: Order, numberOfItems: Int = 0
    ): LiveData<List<Product>> = queryLiveList(
        buildProductQuery(installed, updates, searchQuery, section, order, numberOfItems)
    )

    @RawQuery(observedEntities = [Product::class])
    fun queryList(
        query: SupportSQLiteQuery
    ): DataSource.Factory<Int, Product>

    fun queryList(
        installed: Boolean, updates: Boolean, searchQuery: String,
        section: Section, order: Order, numberOfItems: Int = 0
    ): DataSource.Factory<Int, Product> = queryList(
        buildProductQuery(installed, updates, searchQuery, section, order, numberOfItems)
    )

    // TODO add an UpdateCategory argument
    fun buildProductQuery(
        installed: Boolean, updates: Boolean, searchQuery: String,
        section: Section, order: Order, numberOfItems: Int = 0
    ): SupportSQLiteQuery {
        val builder = QueryBuilder()

        val signatureMatches = """installed.${ROW_SIGNATURE} IS NOT NULL AND
        product.${ROW_SIGNATURES} LIKE ('%.' || installed.${ROW_SIGNATURE} || '.%') AND
        product.${ROW_SIGNATURES} != ''"""

        // Select the return fields
        builder += """SELECT product.rowid AS _id, product.${ROW_REPOSITORY_ID},
        product.${ROW_PACKAGE_NAME}, product.${ROW_NAME},
        product.${ROW_SUMMARY}, installed.${ROW_VERSION},
        (COALESCE(lock.${ROW_VERSION_CODE}, -1) NOT IN (0, product.${ROW_VERSION_CODE}) AND
        product.${ROW_COMPATIBLE} != 0 AND product.${ROW_VERSION_CODE} >
        COALESCE(installed.${ROW_VERSION_CODE}, 0xffffffff) AND $signatureMatches)
        AS ${ROW_CAN_UPDATE}, product.${ROW_COMPATIBLE},
        product.${ROW_ICON}, product.${ROW_METADATA_ICON}, product.${ROW_RELEASES},"""

        // Calculate the matching score with the search query
        if (searchQuery.isNotEmpty()) {
            builder += """(((product.${ROW_NAME} LIKE ? OR
          product.${ROW_SUMMARY} LIKE ?) * 7) |
          ((product.${ROW_PACKAGE_NAME} LIKE ?) * 3) |
          (product.${ROW_DESCRIPTION} LIKE ?)) AS ${ROW_MATCH_RANK},"""
            builder %= List(4) { "%$searchQuery%" }
        } else {
            builder += "0 AS ${ROW_MATCH_RANK},"
        }

        // Take product as main table
        builder += """MAX((product.${ROW_COMPATIBLE} AND
        (installed.${ROW_SIGNATURE} IS NULL OR $signatureMatches)) ||
        PRINTF('%016X', product.${ROW_VERSION_CODE})) FROM $ROW_PRODUCT_NAME AS product"""

        // Merge the matching repositories
        builder += """JOIN $ROW_REPOSITORY_NAME AS repository
        ON product.${ROW_REPOSITORY_ID} = repository.${ROW_ID}"""

        // Merge the matching locks
        builder += """LEFT JOIN $ROW_LOCK_NAME AS lock
        ON product.${ROW_PACKAGE_NAME} = lock.${ROW_PACKAGE_NAME}"""

        // Merge the matching installed
        if (!installed && !updates) builder += "LEFT"
        builder += """JOIN $ROW_INSTALLED_NAME AS installed
        ON product.${ROW_PACKAGE_NAME} = installed.${ROW_PACKAGE_NAME}"""

        // Merge the matching category
        if (section is Section.Category) {
            builder += """JOIN $ROW_CATEGORY_NAME AS category
          ON product.${ROW_PACKAGE_NAME} = category.${ROW_PACKAGE_NAME}"""
        }

        // Filter only active repositories
        builder += """WHERE repository.${ROW_ENABLED} != 0"""

        // Filter only the selected repository/category
        if (section is Section.Category) {
            builder += "AND category.${ROW_NAME} = ?"
            builder %= section.name
        } else if (section is Section.Repository) {
            builder += "AND product.${ROW_REPOSITORY_ID} = ?"
            builder %= section.id.toString()
        }

        // Filter only apps that have some  matching score to the search query
        if (searchQuery.isNotEmpty()) {
            builder += """AND $ROW_MATCH_RANK > 0"""
        }

        // Sum up all products with the same package name
        builder += "GROUP BY product.${ROW_PACKAGE_NAME} HAVING 1"

        // Filter if only can update
        if (updates) {
            builder += "AND $ROW_CAN_UPDATE"
        }

        // Set sorting order
        builder += "ORDER BY"
        if (searchQuery.isNotEmpty()) builder += """$ROW_MATCH_RANK DESC,"""
        when (order) {
            Order.NAME -> Unit
            Order.DATE_ADDED -> builder += "product.${ROW_ADDED} DESC,"
            Order.LAST_UPDATE -> builder += "product.${ROW_UPDATED} DESC,"
        }::class
        builder += "product.${ROW_NAME} COLLATE LOCALIZED ASC${if (numberOfItems > 0) " LIMIT $numberOfItems" else ""}"

        return SimpleSQLiteQuery(builder.build())
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
        """SELECT DISTINCT category.name
        FROM category AS category
        JOIN repository AS repository
        ON category.repository_id = repository._id
        WHERE repository.enabled != 0"""
    )
    val allNames: List<String>

    @Query("DELETE FROM category WHERE repository_id = :id")
    fun deleteById(id: Long): Int
}

// TODO make sure that apps that not uninstalled by Droid-ify still get removed
@Dao
interface InstalledDao : BaseDao<Installed> {
    fun put(vararg installed: Installed) {
        installed.forEach { insertReplace(it) }
    }

    @Query("SELECT * FROM memory_installed WHERE package_name = :packageName")
    fun get(packageName: String): Installed?

    @Query("SELECT * FROM memory_installed WHERE package_name = :packageName")
    fun getLive(packageName: String): LiveData<Installed?>

    @Query("DELETE FROM memory_installed WHERE package_name = :packageName")
    fun delete(packageName: String)
}

@Dao
interface LockDao : BaseDao<Lock> {
    @Query("DELETE FROM memory_lock WHERE package_name = :packageName")
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
    fun putTemporary(products: List<com.looker.droidify.entity.Product>) {
        products.forEach {
            val signatures = it.signatures.joinToString { ".$it" }
                .let { if (it.isNotEmpty()) "$it." else "" }
            insert(it.let {
                ProductTemp().apply {
                    repository_id = it.repositoryId
                    package_name = it.packageName
                    name = it.name
                    summary = it.summary
                    description = it.description
                    added = it.added
                    updated = it.updated
                    version_code = it.versionCode
                    this.signatures = signatures
                    compatible = if (it.compatible) 1 else 0
                    data = it
                    icon = it.icon
                    metadataIcon = it.metadataIcon
                    releases = it.releases
                }
            })
            it.categories.forEach { category ->
                insertCategory(CategoryTemp().apply {
                    repository_id = it.repositoryId
                    package_name = it.packageName
                    name = category
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