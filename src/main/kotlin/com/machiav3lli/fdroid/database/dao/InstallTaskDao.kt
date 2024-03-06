package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.InstallTask
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class InstallTaskDao(private val realm: Realm) : BaseDao<InstallTask>(realm) {

    val all: RealmResults<InstallTask>
        get() = realm.query<InstallTask>()
            .sort("added", Sort.ASCENDING)
            .find()

    val allFlow: Flow<RealmResults<InstallTask>>
        get() = realm.query<InstallTask>()
            .sort("added", Sort.ASCENDING)
            .asFlow()
            .mapLatest { it.list }

    fun get(fileName: String): InstallTask? =
        realm.query<InstallTask>("cacheFileName = $0", fileName)
            .sort("added", Sort.ASCENDING)
            .first()
            .find()

    fun get(packageName: String, versionCode: Long): InstallTask? =
        realm.query<InstallTask>("packageName = $0 AND versionCode = $1", packageName, versionCode)
            .first()
            .find()

    fun getFlow(packageName: String, versionCode: Long): Flow<InstallTask?> =
        realm.query<InstallTask>("packageName = $0 AND versionCode = $1", packageName, versionCode)
            .first()
            .asFlow()
            .mapLatest { it.obj }

    suspend fun delete(packageName: String) = realm.write {
        delete(query<InstallTask>("packageName = $0", packageName))
    }
}