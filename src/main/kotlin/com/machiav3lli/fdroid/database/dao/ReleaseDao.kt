package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.ReleaseTemp
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseDao(private val realm: Realm) : BaseDao<Release>(realm) {

    // This one for the mode combining releases of different sources
    fun get(packageName: String): List<Release> =
        realm.query<Release>("packageName = $0", packageName)
            .find()

    fun getFlow(packageName: String): Flow<List<Release>> =
        realm.query<Release>("packageName = $0", packageName)
            .asFlow()
            .mapLatest { it.list }

    // This one for the separating releases of different sources
    fun get(packageName: String, signature: String): List<Release> =
        realm.query<Release>("packageName = $0 AND signature = $1", packageName, signature)
            .find()

    fun getFlow(packageName: String, signature: String): Flow<List<Release>> =
        realm.query<Release>("packageName = $0 AND signature = $1", packageName, signature)
            .asFlow()
            .mapLatest { it.list }
}


@OptIn(ExperimentalCoroutinesApi::class)
class ReleaseTempDao(private val realm: Realm) : BaseDao<ReleaseTemp>(realm) {

    val all: RealmResults<ReleaseTemp>
        get() = realm.query<ReleaseTemp>()
            .find()

    val allFlow: Flow<RealmResults<ReleaseTemp>>
        get() = realm.query<ReleaseTemp>()
            .asFlow()
            .mapLatest { it.list }

}
