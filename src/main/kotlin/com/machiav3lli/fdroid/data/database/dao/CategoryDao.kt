package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.machiav3lli.fdroid.ROW_ENABLED
import com.machiav3lli.fdroid.ROW_ID
import com.machiav3lli.fdroid.ROW_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_CATEGORY
import com.machiav3lli.fdroid.TABLE_CATEGORY_TEMP
import com.machiav3lli.fdroid.TABLE_REPOSITORY
import com.machiav3lli.fdroid.data.database.entity.Category
import com.machiav3lli.fdroid.data.database.entity.CategoryTemp
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<Category> {
    @Query(
        """SELECT DISTINCT $TABLE_CATEGORY.$ROW_NAME
        FROM $TABLE_CATEGORY
        JOIN $TABLE_REPOSITORY
        ON $TABLE_CATEGORY.$ROW_REPOSITORY_ID = $TABLE_REPOSITORY.$ROW_ID
        WHERE $TABLE_REPOSITORY.$ROW_ENABLED != 0"""
    )
    fun getAllNames(): List<String>

    @Query(
        """SELECT DISTINCT $TABLE_CATEGORY.$ROW_NAME
        FROM $TABLE_CATEGORY
        JOIN $TABLE_REPOSITORY
        ON $TABLE_CATEGORY.$ROW_REPOSITORY_ID = $TABLE_REPOSITORY.$ROW_ID
        WHERE $TABLE_REPOSITORY.$ROW_ENABLED != 0"""
    )
    fun getAllNamesFlow(): Flow<List<String>>

    @Query("DELETE FROM $TABLE_CATEGORY WHERE $ROW_REPOSITORY_ID = :id")
    suspend fun deleteById(id: Long): Int

    @Query("DELETE FROM $TABLE_CATEGORY")
    suspend fun emptyTable()
}

@Dao
interface CategoryTempDao : BaseDao<CategoryTemp> {
    @Query("SELECT * FROM $TABLE_CATEGORY_TEMP")
    fun getAll(): Array<CategoryTemp>

    @Query("DELETE FROM $TABLE_CATEGORY_TEMP")
    suspend fun emptyTable()
}