package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.Tracker
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrackerDao(private val realm: Realm) : BaseDao<Tracker>(realm) {

    val allFlow: Flow<RealmResults<Tracker>>
        get() = realm.query<Tracker>()
            .asFlow()
            .map { it.list }

    val all: RealmResults<Tracker>
        get() = realm.query<Tracker>().find()

    fun get(key: Int): Tracker? = realm.query<Tracker>("key = $0", key).first().find()

    fun getFlow(key: Int): Flow<Tracker?> = realm.query<Tracker>("key = $0", key)
        .asFlow()
        .map { it.list.first() }
}
