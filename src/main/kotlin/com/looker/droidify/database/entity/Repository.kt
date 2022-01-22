package com.looker.droidify.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.looker.droidify.entity.Repository

// TODO LATER: reduce redundancy by merging the entity and database classes
@Entity
class Repository {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "_id")
    var id: Long = 0

    var enabled = 0
    var deleted = false

    @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    var data: Repository? = null

    val trueData: Repository?
        get() = data?.copy(id = id)

    class IdAndDeleted {
        @ColumnInfo(name = "_id")
        var id = 0L

        var deleted = false
    }
}