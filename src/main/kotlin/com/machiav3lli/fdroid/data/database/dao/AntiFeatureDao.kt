package com.machiav3lli.fdroid.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.machiav3lli.fdroid.ROW_DESCRIPTION
import com.machiav3lli.fdroid.ROW_ICON
import com.machiav3lli.fdroid.ROW_LABEL
import com.machiav3lli.fdroid.ROW_NAME
import com.machiav3lli.fdroid.ROW_REPOSITORY_ID
import com.machiav3lli.fdroid.TABLE_ANTIFEATURE
import com.machiav3lli.fdroid.TABLE_ANTIFEATURE_TEMP
import com.machiav3lli.fdroid.data.database.entity.AntiFeature
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureTemp
import kotlinx.coroutines.flow.Flow

@Dao
interface AntiFeatureDao : BaseDao<AntiFeature> {
    @Transaction
    @Query(
        """SELECT $ROW_NAME, $ROW_LABEL, $ROW_DESCRIPTION, $ROW_ICON
        FROM $TABLE_ANTIFEATURE
        GROUP BY $ROW_NAME HAVING 1
        ORDER BY $ROW_REPOSITORY_ID ASC"""
    )
    fun getAllAntiFeatureDetails(): List<AntiFeatureDetails>

    @Transaction
    @Query(
        """SELECT $ROW_NAME, $ROW_LABEL, $ROW_DESCRIPTION, $ROW_ICON
        FROM $TABLE_ANTIFEATURE
        GROUP BY $ROW_NAME HAVING 1
        ORDER BY $ROW_REPOSITORY_ID ASC"""
    )
    fun getAllAntiFeatureDetailsFlow(): Flow<List<AntiFeatureDetails>>

    @Query("DELETE FROM $TABLE_ANTIFEATURE WHERE $ROW_REPOSITORY_ID = :id")
    suspend fun deleteByRepoId(id: Long): Int

    @Query("DELETE FROM $TABLE_ANTIFEATURE")
    suspend fun emptyTable()
}

@Dao
interface AntiFeatureTempDao : BaseDao<AntiFeatureTemp> {
    @Query("SELECT * FROM $TABLE_ANTIFEATURE_TEMP")
    fun getAll(): Array<AntiFeatureTemp>

    @Query("DELETE FROM $TABLE_ANTIFEATURE_TEMP")
    suspend fun emptyTable()
}