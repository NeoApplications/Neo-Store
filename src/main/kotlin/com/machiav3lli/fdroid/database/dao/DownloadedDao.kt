package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.Downloaded
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadedDao(private val realm: Realm) : BaseDao<Downloaded>(realm) {

    val all: RealmResults<Downloaded>
        get() = realm.query<Downloaded>()
            .find()

    val allFlow: Flow<RealmResults<Downloaded>>
        get() = realm.query<Downloaded>()
            .asFlow()
            .mapLatest { it.list }

    fun get(packageName: String): List<Downloaded> =
        realm.query<Downloaded>("packageName = $0", packageName)
            .find()

    fun getFlow(packageName: String): Flow<List<Downloaded>> =
        realm.query<Downloaded>("packageName = $0", packageName)
            .asFlow()
            .mapLatest { it.list }

    fun getLatest(packageName: String): Downloaded? =
        realm.query<Downloaded>("packageName = $0", packageName)
            .sort("changed", Sort.DESCENDING)
            .first()
            .find()

    fun getLatestFlow(packageName: String): Flow<Downloaded?> =
        realm.query<Downloaded>("packageName = $0", packageName)
            .sort("changed", Sort.DESCENDING)
            .first()
            .asFlow()
            .mapLatest { it.obj }

    suspend fun deleteAll(packageName: String) = realm.write {
        delete(query<Downloaded>("packageName = $0", packageName))
    }

    suspend fun delete(packageName: String, version: String, cacheFileName: String) = realm.write {
        delete(
            query<Downloaded>(
                "packageName = $0 AND version = $1 AND cacheFileName = $2",
                packageName,
                version,
                cacheFileName
            )
        )
    }
}
