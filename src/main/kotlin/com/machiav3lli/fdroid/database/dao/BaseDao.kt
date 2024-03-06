package com.machiav3lli.fdroid.database.dao

import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.types.RealmObject

open class BaseDao<T : RealmObject>(private val realm: Realm) {
    fun insert(obj: T) = realm.writeBlocking {
        copyToRealm(obj, updatePolicy = UpdatePolicy.ERROR)
    }

    fun insert(vararg objs: T) = realm.writeBlocking {
        objs.forEach { obj ->
            copyToRealm(obj, updatePolicy = UpdatePolicy.ERROR)
        }
    }

    fun upsert(obj: T) = realm.writeBlocking {
        copyToRealm(obj, updatePolicy = UpdatePolicy.ALL)
    }

    fun upsert(vararg objs: T) = realm.writeBlocking {
        objs.forEach { obj ->
            copyToRealm(obj, updatePolicy = UpdatePolicy.ALL)
        }
    }

    fun delete(obj: T) = realm.writeBlocking {
        delete(obj)
    }

    fun emptyTable() = realm.writeBlocking {
        deleteAll()
    }
}