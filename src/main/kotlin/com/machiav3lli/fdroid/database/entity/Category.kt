package com.machiav3lli.fdroid.database.entity

import io.realm.kotlin.ext.parent
import io.realm.kotlin.types.EmbeddedRealmObject
import kotlinx.serialization.Serializable

@Serializable
open class Category() : EmbeddedRealmObject {
    var label = ""

    constructor(label: String) : this() {
        this.label = label
    }

    val repositoryId: Long
        get() = parent<Product>().repositoryId
    val packageName
        get() = parent<Product>().packageName
}
