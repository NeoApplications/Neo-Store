package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.Extras
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.find
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class ExtrasDao(private val realm: Realm) : BaseDao<Extras>(realm) {

    val all: RealmResults<Extras>
        get() = realm.query<Extras>()
            .find()

    val allFlow: Flow<RealmResults<Extras>>
        get() = realm.query<Extras>()
            .asFlow()
            .mapLatest { it.list }

    operator fun get(packageName: String): Extras? =
        realm.query<Extras>("packageName = $0", packageName)
            .first()
            .find()

    fun getFlow(packageName: String): Flow<Extras?> =
        realm.query<Extras>("packageName = $0", packageName)
            .first()
            .asFlow()
            .mapLatest { it.obj }

    val favorites: List<String>
        get() = realm.query<Extras>("favorite = $0", true)
            .find { it.map(Extras::packageName) }

    val favoritesFlow: Flow<List<String>>
        get() = realm.query<Extras>("favorite = $0", true)
            .asFlow()
            .mapLatest { it.list.map(Extras::packageName) }

    suspend fun delete(packageName: String) = realm.write {
        delete(query<Extras>("packageName = $0", packageName))
    }
}