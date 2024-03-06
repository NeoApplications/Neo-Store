package com.machiav3lli.fdroid.database.dao

import com.machiav3lli.fdroid.database.entity.Category
import com.machiav3lli.fdroid.database.entity.Repository
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.find
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CategoryDao(private val realm: Realm) {

    val allNames: List<String> = realm.query<Repository>("enabled == $0", true).find()
        .run {
            val repoIds = this.map { it.id }
            if (!isNullOrEmpty()) realm.query<Category>("repositoryId IN $0", repoIds)
                .distinct("label")
                .find { it.map(Category::label) }
            else emptyList()
        }

    val allNamesFlow: Flow<List<String>>
        get() = combine(
            realm.query<Category>().distinct("label").asFlow(),
            realm.query<Repository>("enabled == $0", true).asFlow()
        ) { cats, repos ->
            val repoIds = repos.list.map { it.id }
            if (repoIds.isNotEmpty() && cats.list.isNotEmpty())
                realm.query<Category>("repositoryId IN $0", repoIds)
                    .distinct("label")
                    .find { it.map(Category::label) }
            else emptyList()
        }
}
