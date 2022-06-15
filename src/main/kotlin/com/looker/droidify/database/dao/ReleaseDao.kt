package com.looker.droidify.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.looker.droidify.database.entity.Release

@Dao
interface ReleaseDao : BaseDao<Release> {
    // This one for the mode combining releases of different sources
    @Query("SELECT * FROM `release` WHERE packageName = :packageName")
    fun get(packageName: String): List<Release?>

    // This one for the separating releases of different sources
    @Query("SELECT * FROM `release` WHERE packageName = :packageName AND signature = :signature")
    fun get(packageName: String, signature: String): List<Release?>
}
