package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.ExodusInfo
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.max
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

class ExodusInfoDao(private val realm: Realm) : BaseDao<ExodusInfo>(realm) {

    fun get(packageName: String): ExodusInfo? =
        realm.query<ExodusInfo>("packageName = $0", packageName)
            .max<ExodusInfo>("version_code")
            .find()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFlow(packageName: String): Flow<ExodusInfo?> =
        realm.query<ExodusInfo>("packageName = $0", packageName)
            .max<ExodusInfo>("version_code")
            .asFlow()
}
