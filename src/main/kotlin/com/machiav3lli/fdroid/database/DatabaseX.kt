package com.machiav3lli.fdroid.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.machiav3lli.fdroid.database.dao.CategoryDao
import com.machiav3lli.fdroid.database.dao.CategoryTempDao
import com.machiav3lli.fdroid.database.dao.ExodusInfoDao
import com.machiav3lli.fdroid.database.dao.ExtrasDao
import com.machiav3lli.fdroid.database.dao.InstalledDao
import com.machiav3lli.fdroid.database.dao.ProductDao
import com.machiav3lli.fdroid.database.dao.ProductTempDao
import com.machiav3lli.fdroid.database.dao.ReleaseDao
import com.machiav3lli.fdroid.database.dao.RepositoryDao
import com.machiav3lli.fdroid.database.dao.TrackerDao
import com.machiav3lli.fdroid.database.entity.Category
import com.machiav3lli.fdroid.database.entity.CategoryTemp
import com.machiav3lli.fdroid.database.entity.ExodusInfo
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.ProductTemp
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV10
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV11
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV12
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV9
import com.machiav3lli.fdroid.database.entity.Repository.Companion.defaultRepositories
import com.machiav3lli.fdroid.database.entity.Tracker
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
        Extras::class,
        ExodusInfo::class,
        Tracker::class,
    ],
    version = 13,
    exportSchema = true,
    autoMigrations = [AutoMigration(
        from = 8,
        to = 9,
        spec = DatabaseX.Companion.MigrationSpec8to9::class
    ), AutoMigration(
        from = 9,
        to = 10,
        spec = DatabaseX.Companion.MigrationSpec9to10::class
    ), AutoMigration(
        from = 10,
        to = 11,
        spec = DatabaseX.Companion.MigrationSpec10to11::class
    ), AutoMigration(
        from = 11,
        to = 12,
        spec = DatabaseX.Companion.MigrationSpec11to12::class
    ), AutoMigration(
        from = 12,
        to = 13,
    )]
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
    abstract val exodusInfoDao: ExodusInfoDao
    abstract val trackerDao: TrackerDao
    // TODO downloaded releases class/dao

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

        class MigrationSpec8to9 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(8)
            }
        }

        class MigrationSpec9to10 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(9)
            }
        }

        class MigrationSpec10to11 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(10)
            }
        }

        class MigrationSpec11to12 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(11)
            }
        }

        fun onPostMigrate(from: Int) {
            val preRepos = mutableListOf<Repository>()
            if (from == 8) preRepos.addAll(addedReposV9)
            if (from == 9) preRepos.addAll(addedReposV10)
            if (from == 10) preRepos.addAll(addedReposV11)
            if (from == 11) preRepos.addAll(addedReposV12)
            GlobalScope.launch(Dispatchers.IO) {
                preRepos.forEach {
                    INSTANCE?.repositoryDao?.put(it)
                }
            }
        }
    }

    fun cleanUp(pairs: Set<Pair<Long, Boolean>>) {
        runInTransaction {
            pairs.windowed(10, 10, true).map {
                it.map { pair -> pair.first }
                    .toLongArray()
                    .forEach { id ->
                        productDao.deleteById(id)
                        categoryDao.deleteById(id)
                    }
                it.filter { pair -> pair.second }
                    .map { pair -> pair.first }
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
