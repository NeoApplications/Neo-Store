package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.Repository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.query.find
import io.realm.kotlin.query.max
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import org.mongodb.kbson.ObjectId

@OptIn(ExperimentalCoroutinesApi::class)
class RepositoryDao(private val realm: Realm) : BaseDao<Repository>(realm) {

    fun getCount(): Long = realm.query<Repository>()
        .count()
        .find()

    fun insertPatch(vararg repos: Repository): List<Repository> = repos.map { repo ->
        val old = all.find { it.address == repo.address || it.mirrors == repo.mirrors }
        if (old != null) upsert(repo.apply { key = old.key })
        else insert(repo.apply { key = ObjectId() })
    }

    fun get(id: Long): Repository? =
        realm.query<Repository>("id = $0", id)
            .first()
            .find()

    fun getFlow(id: Long): Flow<Repository?> =
        realm.query<Repository>("id = $0", id)
            .first()
            .asFlow()
            .mapLatest { it.obj }

    val all: RealmResults<Repository>
        get() = realm.query<Repository>()
            .sort("id", Sort.ASCENDING)
            .find()

    val allFlow: Flow<RealmResults<Repository>>
        get() = realm.query<Repository>()
            .sort("id", Sort.ASCENDING)
            .asFlow()
            .mapLatest { it.list }

    val allEnabledIds: List<Long>
        get() = realm.query<Repository>("enabled = $0", true)
            .sort("id", Sort.ASCENDING)
            .find { it.map(Repository::id) }

    val allDisabledIds: List<Long>
        get() = realm.query<Repository>("enabled = $0", false)
            .sort("id", Sort.ASCENDING)
            .find { it.map(Repository::id) }

    fun deleteById(id: Long) = realm.writeBlocking {
        delete(query<Repository>("id = $0", id))
    }

    val latestAddedId: Long
        get() = realm.query<Repository>()
            .max<Long>("id")
            .find() ?: 0
}