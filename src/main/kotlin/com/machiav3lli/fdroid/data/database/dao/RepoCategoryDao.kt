package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.machiav3lli.fdroid.ROW_ICON
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_REPOCATEGORY
import com.machiav3lli.fdroid.TABLE_REPOCATEGORY_TEMP
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.RepoCategory
import com.machiav3lli.fdroid.data.database.entity.RepoCategoryTemp
import kotlinx.coroutines.flow.Flow

@Dao
interface RepoCategoryDao : BaseDao<RepoCategory> {
    @Transaction
    @Query(
        """SELECT $ROW_NAME, $ROW_LABEL, $ROW_ICON
        FROM $TABLE_REPOCATEGORY
        GROUP BY $ROW_NAME HAVING 1
        ORDER BY $ROW_REPOSITORY_ID ASC"""
    )
    fun getAllCategoryDetails(): List<CategoryDetails>

    @Transaction
    @Query(
        """SELECT $ROW_NAME, $ROW_LABEL, $ROW_ICON
        FROM $TABLE_REPOCATEGORY
        GROUP BY $ROW_NAME HAVING 1
        ORDER BY $ROW_REPOSITORY_ID ASC"""
    )
    fun getAllCategoryDetailsFlow(): Flow<List<CategoryDetails>>

    @Query("DELETE FROM $TABLE_REPOCATEGORY WHERE $ROW_REPOSITORY_ID = :id")
    suspend fun deleteByRepoId(id: Long): Int

    @Query("DELETE FROM $TABLE_REPOCATEGORY")
    suspend fun emptyTable()
}

@Dao
interface RepoCategoryTempDao : BaseDao<RepoCategoryTemp> {
    @Query("SELECT * FROM $TABLE_REPOCATEGORY_TEMP")
    fun getAll(): Array<RepoCategoryTemp>

    @Query("DELETE FROM $TABLE_REPOCATEGORY_TEMP")
    suspend fun emptyTable()
}