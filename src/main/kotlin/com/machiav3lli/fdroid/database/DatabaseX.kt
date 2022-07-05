package com.machiav3lli.fdroid.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.machiav3lli.fdroid.database.dao.CategoryDao
import com.machiav3lli.fdroid.database.dao.CategoryTempDao
import com.machiav3lli.fdroid.database.dao.ExtrasDao
import com.machiav3lli.fdroid.database.dao.InstalledDao
import com.machiav3lli.fdroid.database.dao.ProductDao
import com.machiav3lli.fdroid.database.dao.ProductTempDao
import com.machiav3lli.fdroid.database.dao.ReleaseDao
import com.machiav3lli.fdroid.database.dao.RepositoryDao
import com.machiav3lli.fdroid.database.entity.Category
import com.machiav3lli.fdroid.database.entity.CategoryTemp
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.ProductTemp
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.database.entity.Repository.Companion.defaultRepositories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        Repository::class,
        Product::class,
        Release::class,
        ProductTemp::class,
        Category::class,
        CategoryTemp::class,
        Installed::class,
        Extras::class
    ], version = 8
)
@TypeConverters(Converters::class)
abstract class DatabaseX : RoomDatabase() {
    abstract val repositoryDao: RepositoryDao
    abstract val productDao: ProductDao
    abstract val releaseDao: ReleaseDao
    abstract val productTempDao: ProductTempDao
    abstract val categoryDao: CategoryDao
    abstract val categoryTempDao: CategoryTempDao
    abstract val installedDao: InstalledDao
    abstract val extrasDao: ExtrasDao

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
                        .build()
                    INSTANCE?.let { instance ->
                        GlobalScope.launch(Dispatchers.IO) {
                            if (instance.repositoryDao.count == 0) defaultRepositories.forEach {
                                instance.repositoryDao.put(it)
                            }
                        }
                    }
                }
                return INSTANCE!!
            }
        }
    }

    fun cleanUp(pairs: Set<Pair<Long, Boolean>>) {
        runInTransaction {
            pairs.windowed(10, 10, true).map {
                it.map { it.first }
                    .toLongArray()
                    .forEach { id ->
                        productDao.deleteById(id)
                        categoryDao.deleteById(id)
                    }
                it.filter { it.second }
                    .map { it.first }
                    .toLongArray()
                    .forEach { id -> repositoryDao.deleteById(id) }
            }
        }
    }

    fun finishTemporary(repository: Repository, success: Boolean) {
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