package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.ROW_ADDED
import com.machiav3lli.fdroid.ROW_ANTIFEATURES
import com.machiav3lli.fdroid.ROW_AUTHOR
import com.machiav3lli.fdroid.ROW_ENABLED
import com.machiav3lli.fdroid.ROW_FAVORITE
import com.machiav3lli.fdroid.ROW_ID
import com.machiav3lli.fdroid.ROW_IGNORED_VERSION
import com.machiav3lli.fdroid.ROW_IGNORE_UPDATES
import com.machiav3lli.fdroid.ROW_IS_COMPATIBLE
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_LICENSES
import com.machiav3lli.fdroid.ROW_MINSDK_VERSION
import com.machiav3lli.fdroid.ROW_NAME
import com.machiav3lli.fdroid.ROW_PACKAGE_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.ROW_SELECTED
import com.machiav3lli.fdroid.ROW_SIGNATURE
import com.machiav3lli.fdroid.ROW_SIGNATURES
import com.machiav3lli.fdroid.ROW_TARGETSDK_VERSION
import com.machiav3lli.fdroid.ROW_UPDATED
import com.machiav3lli.fdroid.ROW_VERSION_CODE
import com.machiav3lli.fdroid.TABLE_CATEGORY
import com.machiav3lli.fdroid.TABLE_EXTRAS
import com.machiav3lli.fdroid.TABLE_INSTALLED
import com.machiav3lli.fdroid.TABLE_PRODUCT
import com.machiav3lli.fdroid.TABLE_PRODUCT_TEMP
import com.machiav3lli.fdroid.TABLE_RELEASE
import com.machiav3lli.fdroid.TABLE_REPOSITORY
import com.machiav3lli.fdroid.data.database.QueryBuilder
import com.machiav3lli.fdroid.data.database.entity.Category
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.IconDetails
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Licenses
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.database.entity.ProductTemp
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.entity.Section
import com.machiav3lli.fdroid.data.entity.UpdateCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao : BaseDao<Product> {
    @Query("SELECT COUNT(*) FROM $TABLE_PRODUCT WHERE repositoryId = :id")
    fun countForRepository(id: Long): Long

    @Query("SELECT COUNT(*) FROM $TABLE_PRODUCT WHERE repositoryId = :id")
    fun countForRepositoryFlow(id: Long): Flow<Long>

    @Query("SELECT * FROM $TABLE_PRODUCT WHERE repositoryId = :repoId ORDER BY label")
    fun productsForRepositoryFlow(repoId: Long): Flow<List<EmbeddedProduct>>

    @Query("SELECT EXISTS(SELECT 1 FROM $TABLE_PRODUCT WHERE packageName = :packageName)")
    fun exists(packageName: String): Boolean

    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName")
    fun get(packageName: String): List<EmbeddedProduct>

    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<EmbeddedProduct>>

    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName AND repositoryId = :repoId")
    fun get(packageName: String, repoId: Long): EmbeddedProduct?

    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName AND repositoryId = :repoId")
    fun getFlow(packageName: String, repoId: Long): Flow<EmbeddedProduct?>

    @Query("SELECT packageName, icon, metadataIcon FROM $TABLE_PRODUCT GROUP BY packageName HAVING 1")
    fun getIconDetails(): List<IconDetails>

    @Query("SELECT packageName, icon, metadataIcon FROM $TABLE_PRODUCT GROUP BY packageName HAVING 1")
    fun getIconDetailsFlow(): Flow<List<IconDetails>>

    @Query("DELETE FROM $TABLE_PRODUCT WHERE repositoryId = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT licenses FROM $TABLE_PRODUCT")
    fun getAllLicenses(): List<Licenses>

    @Query("SELECT DISTINCT licenses FROM $TABLE_PRODUCT")
    fun getAllLicensesFlow(): Flow<List<Licenses>>

    @Query("SELECT * FROM $TABLE_PRODUCT WHERE author LIKE '%' || :author || '%' ")
    fun getAuthorPackagesFlow(author: String): Flow<List<EmbeddedProduct>>

    @RawQuery
    fun queryObject(query: SupportSQLiteQuery): List<EmbeddedProduct>

    @Transaction
    fun queryObject(
        installed: Boolean, updates: Boolean,
        section: Section, filteredOutRepos: Set<String> = emptySet(),
        category: String = FILTER_CATEGORY_ALL, filteredAntiFeatures: Set<String> = emptySet(),
        filteredLicenses: Set<String> = emptySet(),
        order: Order, ascending: Boolean, numberOfItems: Int = 0,
        updateCategory: UpdateCategory = UpdateCategory.ALL,
        author: String = "", minSdkVersion: Int = 0, targetSdkVersion: Int = 0
    ): List<EmbeddedProduct> = queryObject(
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
            minSdkVersion = minSdkVersion,
            targetSdkVersion = targetSdkVersion,
        )
    )

    @Transaction
    @RawQuery(observedEntities = [Product::class, Installed::class, Extras::class, Repository::class, Category::class])
    fun queryFlowList(query: SupportSQLiteQuery): Flow<List<EmbeddedProduct>>

    fun queryFlowList(request: Request): Flow<List<EmbeddedProduct>> = queryFlowList(
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
            targetSdkVersion = request.targetSDK,
            minSdkVersion = request.minSDK,
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
        targetSdkVersion: Int = 0,
        minSdkVersion: Int = 0,
    ): SupportSQLiteQuery {
        val builder = QueryBuilder()

        if (section == Section.NONE) {
            return SimpleSQLiteQuery("SELECT * FROM $TABLE_PRODUCT LIMIT 0")
        }

        // Selection
        builder += """
        SELECT $TABLE_PRODUCT.*, 
        $TABLE_REPOSITORY.$ROW_ENABLED AS repo_enabled,
        $TABLE_EXTRAS.$ROW_FAVORITE AS is_favorite,
        $TABLE_INSTALLED.$ROW_VERSION_CODE AS installed_version_code,
        $TABLE_INSTALLED.$ROW_SIGNATURES AS installed_signature
        """

        // From & Joining
        builder += """
        FROM $TABLE_PRODUCT
        JOIN $TABLE_REPOSITORY ON $TABLE_PRODUCT.$ROW_REPOSITORY_ID = $TABLE_REPOSITORY.$ROW_ID
        LEFT JOIN $TABLE_EXTRAS ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_EXTRAS.$ROW_PACKAGE_NAME
        ${if (!installed && !updates) "LEFT " else ""}JOIN $TABLE_INSTALLED ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_INSTALLED.$ROW_PACKAGE_NAME
        LEFT JOIN $TABLE_CATEGORY ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_CATEGORY.$ROW_PACKAGE_NAME
        """

        // Filtering
        val whereConditions = mutableListOf<String>()
        whereConditions.add("$TABLE_REPOSITORY.$ROW_ENABLED = 1")
        whereConditions.add("$TABLE_PRODUCT.$ROW_REPOSITORY_ID NOT LIKE '%[^0-9]%'")

        if (author.isNotEmpty()) {
            whereConditions.add("$TABLE_PRODUCT.$ROW_AUTHOR = ?")
            builder.addArgument(author)
        }

        //// Groups
        if (filteredOutRepos.isNotEmpty()) {
            whereConditions.add(
                "$TABLE_PRODUCT.$ROW_REPOSITORY_ID NOT IN (${
                    filteredOutRepos.joinToString(
                        ","
                    )
                })"
            )
        }

        if (category != FILTER_CATEGORY_ALL) {
            whereConditions.add("$TABLE_CATEGORY.$ROW_NAME = ?")
            builder.addArgument(category)
        }

        filteredAntiFeatures.forEach {
            whereConditions.add("$TABLE_PRODUCT.$ROW_ANTIFEATURES NOT LIKE '%$it%'")
        }

        filteredLicenses.forEach {
            whereConditions.add("$TABLE_PRODUCT.$ROW_LICENSES NOT LIKE '%$it%'")
        }

        if (section == Section.FAVORITE) {
            whereConditions.add("COALESCE($TABLE_EXTRAS.$ROW_FAVORITE, 0) != 0")
        }

        //// Update state
        when (updateCategory) {
            UpdateCategory.NEW     -> {
                whereConditions.add("$TABLE_PRODUCT.$ROW_ADDED = $TABLE_PRODUCT.$ROW_UPDATED")
                whereConditions.add(
                    """
                (SELECT COUNT(*) 
                FROM $TABLE_RELEASE 
                WHERE $TABLE_RELEASE.$ROW_PACKAGE_NAME = $TABLE_PRODUCT.$ROW_PACKAGE_NAME) = 1
                """.trimIndent()
                )
            }

            UpdateCategory.UPDATED -> whereConditions.add("$TABLE_PRODUCT.$ROW_ADDED < $TABLE_PRODUCT.$ROW_UPDATED")
            else                   -> {}
        }

        if (updates) {
            whereConditions.add(
                """
            COALESCE($TABLE_EXTRAS.$ROW_IGNORE_UPDATES, 0) = 0
            AND EXISTS (
                    SELECT 1 FROM $TABLE_RELEASE 
                    WHERE $TABLE_RELEASE.$ROW_PACKAGE_NAME = $TABLE_PRODUCT.$ROW_PACKAGE_NAME
                    AND $TABLE_RELEASE.$ROW_REPOSITORY_ID = $TABLE_PRODUCT.$ROW_REPOSITORY_ID
                    AND $TABLE_RELEASE.$ROW_SELECTED = 1
                    AND $TABLE_RELEASE.$ROW_VERSION_CODE > COALESCE($TABLE_INSTALLED.$ROW_VERSION_CODE, 0xffffffff)
                    AND $TABLE_RELEASE.$ROW_VERSION_CODE != COALESCE($TABLE_EXTRAS.$ROW_IGNORED_VERSION, -1)
                    AND $TABLE_RELEASE.$ROW_IS_COMPATIBLE = 1
                )
            AND ($TABLE_INSTALLED.$ROW_SIGNATURES = ''
                OR EXISTS (
                    SELECT 1 FROM $TABLE_RELEASE 
                    WHERE $TABLE_RELEASE.$ROW_PACKAGE_NAME = $TABLE_PRODUCT.$ROW_PACKAGE_NAME
                    AND $TABLE_RELEASE.$ROW_REPOSITORY_ID = $TABLE_PRODUCT.$ROW_REPOSITORY_ID
                    AND $TABLE_RELEASE.$ROW_SELECTED = 1
                    AND $TABLE_INSTALLED.$ROW_SIGNATURES LIKE ('%' || $TABLE_RELEASE.$ROW_SIGNATURE || '%')
                    AND $TABLE_RELEASE.$ROW_SIGNATURE != ''
                ))
            """.trimIndent()
            )
        }

        //// SDK
        targetSdkVersion.takeIf { it > 1 }?.let { minTarget ->
            whereConditions.add(
                """
            EXISTS (
                SELECT 1 FROM $TABLE_RELEASE
                WHERE $TABLE_RELEASE.$ROW_PACKAGE_NAME = $TABLE_PRODUCT.$ROW_PACKAGE_NAME
                AND $TABLE_RELEASE.$ROW_REPOSITORY_ID = $TABLE_PRODUCT.$ROW_REPOSITORY_ID
                AND $TABLE_RELEASE.$ROW_TARGETSDK_VERSION >= ?
            )
        """.trimIndent()
            )
            builder.addArgument(minTarget.toString())
        }

        minSdkVersion.takeIf { it > 1 }?.let { minMin ->
            whereConditions.add(
                """
            EXISTS (
                SELECT 1 FROM $TABLE_RELEASE
                WHERE $TABLE_RELEASE.$ROW_PACKAGE_NAME = $TABLE_PRODUCT.$ROW_PACKAGE_NAME
                AND $TABLE_RELEASE.$ROW_REPOSITORY_ID = $TABLE_PRODUCT.$ROW_REPOSITORY_ID
                AND $TABLE_RELEASE.$ROW_MINSDK_VERSION >= ?
            )
        """.trimIndent()
            )
            builder.addArgument(minMin.toString())
        }

        builder += "WHERE ${whereConditions.joinToString(" AND ")}"

        // Group By
        builder += "GROUP BY $TABLE_PRODUCT.$ROW_PACKAGE_NAME"

        // Ordering
        val orderByClause = when (order) {
            Order.NAME        -> "$TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ${if (ascending) "ASC" else "DESC"}"
            Order.DATE_ADDED  -> "$TABLE_PRODUCT.$ROW_ADDED ${if (ascending) "ASC" else "DESC"}"
            Order.LAST_UPDATE -> "$TABLE_PRODUCT.$ROW_UPDATED ${if (ascending) "ASC" else "DESC"}"
        }
        builder += "ORDER BY $orderByClause, $TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ASC"

        // Limit
        if (numberOfItems > 0) builder += "LIMIT $numberOfItems"

        return SimpleSQLiteQuery(builder.build(), builder.arguments.toTypedArray())
    }

    @Query("DELETE FROM $TABLE_PRODUCT")
    suspend fun emptyTable()
}

@Dao
interface ProductTempDao : BaseDao<ProductTemp> {
    @Query("SELECT * FROM $TABLE_PRODUCT_TEMP")
    fun getAll(): Array<ProductTemp>

    @Query("DELETE FROM $TABLE_PRODUCT_TEMP")
    suspend fun emptyTable()
}