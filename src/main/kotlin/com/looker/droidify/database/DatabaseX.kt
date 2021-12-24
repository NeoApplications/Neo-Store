package com.looker.droidify.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.looker.droidify.entity.Repository.Companion.defaultRepositories

@Database(
    entities = [
        Repository::class,
        Product::class,
        ProductTemp::class,
        Category::class,
        CategoryTemp::class,
        Installed::class,
        Lock::class
    ], version = 1
)
@TypeConverters(Converters::class)
abstract class DatabaseX : RoomDatabase() {
    abstract val repositoryDao: RepositoryDao
    abstract val productDao: ProductDao
    abstract val productTempDao: ProductTempDao
    abstract val categoryDao: CategoryDao
    abstract val categoryTempDao: CategoryTempDao
    abstract val installedDao: InstalledDao
    abstract val lockDao: LockDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseX? = null

        fun getInstance(context: Context): DatabaseX {
            synchronized(this) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                        .databaseBuilder(
                            context.applicationContext,
                            DatabaseX::class.java,
                            "main_database.db"
                        )
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                    INSTANCE?.let { instance ->
                        if (instance.repositoryDao.count == 0) defaultRepositories.forEach {
                            instance.repositoryDao.put(it)
                        }
                    }
                }
                return INSTANCE!!
            }
        }
    }

    fun cleanUp(pairs: Set<Pair<Long, Boolean>>) {
        runInTransaction {
            val result = pairs.windowed(10, 10, true).map {
                val ids = it.map { it.first }.toLongArray()
                val productsCount = productDao.deleteById(*ids)
                val categoriesCount = categoryDao.deleteById(*ids)
                val deleteIds = it.filter { it.second }.map { it.first }.toLongArray()
                repositoryDao.deleteById(*deleteIds)
                productsCount != 0 || categoriesCount != 0
            }
        }
        // Use live objects and observers instead
        /*if (result.any { it }) {
            com.looker.droidify.database.Database.notifyChanged(com.looker.droidify.database.Database.Subject.Products)
        }*/
    }

    fun finishTemporary(repository: com.looker.droidify.entity.Repository, success: Boolean) {
        runInTransaction {
            if (success) {
                productDao.deleteById(repository.id)
                categoryDao.deleteById(repository.id)
                productDao.insert(*(productTempDao.all))
                categoryDao.insert(*(categoryTempDao.all))
                repositoryDao.put(repository)
            }
            productTempDao.emptyTable()
            categoryTempDao.emptyTable()
        }
    }
}