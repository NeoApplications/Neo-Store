package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.Installed
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class InstalledDao(private val realm: Realm) : BaseDao<Installed>(realm) {

    val all: RealmResults<Installed>
        get() = realm.query<Installed>().find()

    val allFlow: Flow<RealmResults<Installed>>
        get() = realm.query<Installed>()
            .asFlow()
            .mapLatest { it.list }

    fun get(packageName: String): Installed? =
        realm.query<Installed>("packageName = $0", packageName).first().find()

    fun getFlow(packageName: String): Flow<Installed?> =
        realm.query<Installed>("packageName = $0", packageName)
            .first()
            .asFlow()
            .mapLatest { it.obj }

    suspend fun delete(packageName: String) = realm.write {
        delete(query<Installed>("packageName = $0", packageName))
    }
}