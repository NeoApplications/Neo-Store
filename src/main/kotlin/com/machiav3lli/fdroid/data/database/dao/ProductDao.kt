package com.machiav3lli.fdroid.data.database.dao

import android.os.Build
import androidx.room.Dao
import androidx.room.MapColumn
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
import com.machiav3lli.fdroid.ROW_IGNORE_VULNS
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
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.QueryBuilder
import com.machiav3lli.fdroid.data.database.entity.Category
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Licenses
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.database.entity.ProductIconDetails
import com.machiav3lli.fdroid.data.database.entity.ProductTemp
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.Order
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.entity.Section
import com.machiav3lli.fdroid.data.entity.UpdateCategory
import com.machiav3lli.fdroid.utils.extension.android.Android
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao : BaseDao<Product> {
    @Query("SELECT COUNT(*) FROM $TABLE_PRODUCT WHERE repositoryId = :id")
    suspend fun countForRepository(id: Long): Long

    @Query("SELECT COUNT(*) FROM $TABLE_PRODUCT WHERE repositoryId = :id")
    fun countForRepositoryFlow(id: Long): Flow<Long>

    @Transaction
    @Query("SELECT * FROM $TABLE_PRODUCT WHERE repositoryId = :repoId ORDER BY label")
    fun productsForRepositoryFlow(repoId: Long): Flow<List<EmbeddedProduct>>

    @Query("SELECT EXISTS(SELECT 1 FROM $TABLE_PRODUCT WHERE packageName = :packageName)")
    suspend fun exists(packageName: String): Boolean

    @Transaction
    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName")
    suspend fun get(packageName: String): List<EmbeddedProduct>

    @Transaction
    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName")
    fun getFlow(packageName: String): Flow<List<EmbeddedProduct>>

    @Transaction
    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName AND repositoryId = :repoId")
    suspend fun get(packageName: String, repoId: Long): EmbeddedProduct?

    @Transaction
    @Query("SELECT * FROM $TABLE_PRODUCT WHERE packageName = :packageName AND repositoryId = :repoId")
    fun getFlow(packageName: String, repoId: Long): Flow<EmbeddedProduct?>

    @Query("SELECT * FROM producticondetails")
    fun getIconDetailsMapFlow(): Flow<Map<@MapColumn(columnName = ROW_PACKAGE_NAME) String, ProductIconDetails>>

    @Query("DELETE FROM $TABLE_PRODUCT WHERE repositoryId = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT DISTINCT licenses FROM $TABLE_PRODUCT")
    suspend fun getAllLicenses(): List<Licenses>

    @Query("SELECT DISTINCT licenses FROM $TABLE_PRODUCT")
    fun getAllLicensesFlow(): Flow<List<Licenses>>

    @Transaction
    @Query("SELECT * FROM $TABLE_PRODUCT WHERE author LIKE '%' || :author || '%' ")
    fun getAuthorPackagesFlow(author: String): Flow<List<EmbeddedProduct>>

    @RawQuery
    suspend fun queryObject(query: SupportSQLiteQuery): List<EmbeddedProduct>

    @Transaction
    suspend fun queryObject(
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

    fun queryFlowOfPackages(pkgs: Set<String>): Flow<List<EmbeddedProduct>> = queryFlowList(
        buildProductQuery(
            installed = false,
            updates = false,
            section = Section.All,
            specificPackages = pkgs,
            order = Order.LAST_UPDATE,
            ascending = false,
            numberOfItems = pkgs.size,
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
        specificPackages: Set<String> = emptySet(),
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
        JOIN (
            SELECT p2.$ROW_PACKAGE_NAME,
                   p2.$ROW_REPOSITORY_ID,
                   ${
            if (Android.sdk(Build.VERSION_CODES.R)) """
                   ROW_NUMBER() OVER (
                       PARTITION BY p2.$ROW_PACKAGE_NAME 
                       ORDER BY COALESCE(
                           (SELECT MAX(rel.$ROW_VERSION_CODE) 
                            FROM $TABLE_RELEASE rel 
                            WHERE rel.$ROW_PACKAGE_NAME = p2.$ROW_PACKAGE_NAME 
                            AND rel.$ROW_REPOSITORY_ID = p2.$ROW_REPOSITORY_ID), 
                           0
                       ) DESC
                   ) as rn""" else """
                   (SELECT COUNT(*)
                    FROM $TABLE_PRODUCT p3
                    JOIN $TABLE_REPOSITORY r3 ON p3.$ROW_REPOSITORY_ID = r3.$ROW_ID
                    WHERE p3.$ROW_PACKAGE_NAME = p2.$ROW_PACKAGE_NAME
                    AND r3.$ROW_ENABLED = 1
                    AND p3.$ROW_REPOSITORY_ID NOT LIKE '%[^0-9]%'
                    ${
                if (filteredOutRepos.isNotEmpty()) "AND p3.$ROW_REPOSITORY_ID NOT IN (${
                    filteredOutRepos.joinToString(
                        ","
                    )
                })" else ""
            }
                    AND COALESCE(
                        (SELECT MAX(rel.$ROW_VERSION_CODE) 
                         FROM $TABLE_RELEASE rel 
                         WHERE rel.$ROW_PACKAGE_NAME = p3.$ROW_PACKAGE_NAME 
                         AND rel.$ROW_REPOSITORY_ID = p3.$ROW_REPOSITORY_ID), 
                        0
                    ) > COALESCE(
                        (SELECT MAX(rel.$ROW_VERSION_CODE) 
                         FROM $TABLE_RELEASE rel 
                         WHERE rel.$ROW_PACKAGE_NAME = p2.$ROW_PACKAGE_NAME 
                         AND rel.$ROW_REPOSITORY_ID = p2.$ROW_REPOSITORY_ID), 
                        0
                    )
                   ) + 1 as rn
                   """
        }
            FROM $TABLE_PRODUCT p2
            JOIN $TABLE_REPOSITORY r2 ON p2.$ROW_REPOSITORY_ID = r2.$ROW_ID
            WHERE r2.$ROW_ENABLED = 1
            AND p2.$ROW_REPOSITORY_ID NOT LIKE '%[^0-9]%'
            ${
            if (filteredOutRepos.isNotEmpty()) "AND p2.$ROW_REPOSITORY_ID NOT IN (${
                filteredOutRepos.joinToString(
                    ","
                )
            })" else ""
        }
        ) ranked_products ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = ranked_products.$ROW_PACKAGE_NAME
                          AND $TABLE_PRODUCT.$ROW_REPOSITORY_ID = ranked_products.$ROW_REPOSITORY_ID
                          AND ranked_products.rn = 1
        JOIN $TABLE_REPOSITORY ON $TABLE_PRODUCT.$ROW_REPOSITORY_ID = $TABLE_REPOSITORY.$ROW_ID
        ${if (!installed && !updates) "LEFT " else ""}JOIN $TABLE_INSTALLED ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_INSTALLED.$ROW_PACKAGE_NAME
        LEFT JOIN $TABLE_EXTRAS ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_EXTRAS.$ROW_PACKAGE_NAME
        LEFT JOIN $TABLE_CATEGORY ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_CATEGORY.$ROW_PACKAGE_NAME
        """

        // Filtering
        val whereConditions = mutableListOf<String>()

        if (specificPackages.isNotEmpty()) {
            whereConditions.add(
                "$TABLE_PRODUCT.$ROW_PACKAGE_NAME IN (${
                    specificPackages.joinToString(",", transform = { "'$it'" })
                })"
            )
        }

        if (author.isNotEmpty()) {
            whereConditions.add("$TABLE_PRODUCT.$ROW_AUTHOR = ?")
            builder.addArgument(author)
        }

        //// Groups
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
                (SELECT COUNT(DISTINCT $TABLE_RELEASE.$ROW_VERSION_CODE) 
                FROM $TABLE_RELEASE 
                WHERE $TABLE_RELEASE.$ROW_PACKAGE_NAME = $TABLE_PRODUCT.$ROW_PACKAGE_NAME) = 1
                """.trimIndent()
                )
            }

            UpdateCategory.UPDATED -> {
                whereConditions.add(
                    """
                $TABLE_PRODUCT.$ROW_ADDED < $TABLE_PRODUCT.$ROW_UPDATED
                OR (SELECT COUNT(DISTINCT $TABLE_RELEASE.$ROW_VERSION_CODE) 
                FROM $TABLE_RELEASE
                WHERE $TABLE_RELEASE.$ROW_PACKAGE_NAME = $TABLE_PRODUCT.$ROW_PACKAGE_NAME) != 1
                """.trimIndent()
                )
            }

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
                    ${
                    if (Preferences[Preferences.Key.DisableSignatureCheck]) "" else """
                    AND $TABLE_INSTALLED.$ROW_SIGNATURES LIKE ('%' || $TABLE_RELEASE.$ROW_SIGNATURE || '%')
                    AND $TABLE_RELEASE.$ROW_SIGNATURE != ''
                    """
                }))
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

        if (whereConditions.isNotEmpty()) {
            builder += "WHERE ${whereConditions.joinToString(" AND ")}"
        }

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

    @Transaction
    suspend fun getInstalledProductsWithVulnerabilities(repoId: Long): List<EmbeddedProduct> =
        queryObject(
            SimpleSQLiteQuery(
                """
                SELECT $TABLE_PRODUCT.*
                FROM $TABLE_PRODUCT
                JOIN $TABLE_INSTALLED ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_INSTALLED.$ROW_PACKAGE_NAME
                LEFT JOIN $TABLE_EXTRAS ON $TABLE_PRODUCT.$ROW_PACKAGE_NAME = $TABLE_EXTRAS.$ROW_PACKAGE_NAME
                WHERE $TABLE_PRODUCT.$ROW_REPOSITORY_ID = $repoId
                AND $TABLE_PRODUCT.$ROW_ANTIFEATURES LIKE '%${AntiFeature.KNOWN_VULN.key}%'
                AND COALESCE($TABLE_EXTRAS.$ROW_IGNORE_VULNS, 0) = 0
                GROUP BY $TABLE_PRODUCT.$ROW_PACKAGE_NAME
                ORDER BY $TABLE_PRODUCT.$ROW_LABEL COLLATE LOCALIZED ASC
            """.trimIndent()
            )
        )

    @Query("DELETE FROM $TABLE_PRODUCT")
    suspend fun emptyTable()
}

@Dao
interface ProductTempDao : BaseDao<ProductTemp> {
    @Query("SELECT * FROM $TABLE_PRODUCT_TEMP")
    suspend fun getAll(): Array<ProductTemp>

    @Query("DELETE FROM $TABLE_PRODUCT_TEMP")
    suspend fun emptyTable()
}