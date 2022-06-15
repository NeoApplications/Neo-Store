package com.looker.droidify.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.looker.droidify.database.entity.Category
import com.looker.droidify.database.entity.CategoryTemp

@Dao
interface CategoryDao : BaseDao<Category> {
    @get:Query(
        """SELECT DISTINCT category.label
        FROM category AS category
        JOIN repository AS repository
        ON category.repositoryId = repository._id
        WHERE repository.enabled != 0"""
    )
    val allNames: List<String>

    @get:Query(
        """SELECT DISTINCT category.label
        FROM category AS category
        JOIN repository AS repository
        ON category.repositoryId = repository._id
        WHERE repository.enabled != 0"""
    )
    val allNamesLive: LiveData<List<String>>

    @Query("DELETE FROM category WHERE repositoryId = :id")
    fun deleteById(id: Long): Int
}

@Dao
interface CategoryTempDao : BaseDao<CategoryTemp> {
    @get:Query("SELECT * FROM temporary_category")
    val all: Array<CategoryTemp>

    @Query("DELETE FROM temporary_category")
    fun emptyTable()
}