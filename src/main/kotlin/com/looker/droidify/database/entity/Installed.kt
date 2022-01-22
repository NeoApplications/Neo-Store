package com.looker.droidify.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_installed")
class Installed(pName: String = "") {
    @PrimaryKey
    var package_name = pName

    var version = ""
    var version_code = 0L
    var signature = ""
}