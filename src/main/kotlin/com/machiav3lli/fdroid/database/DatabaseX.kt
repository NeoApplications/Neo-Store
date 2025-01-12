package com.machiav3lli.fdroid.database

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RenameColumn
import androidx.room.RenameTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.machiav3lli.fdroid.ROW_ID
import com.machiav3lli.fdroid.TABLE_EXODUS_INFO
import com.machiav3lli.fdroid.TABLE_INSTALL_TASK
import com.machiav3lli.fdroid.TABLE_REPOSITORY
import com.machiav3lli.fdroid.TABLE_TRACKER
import com.machiav3lli.fdroid.database.dao.CategoryDao
import com.machiav3lli.fdroid.database.dao.CategoryTempDao
import com.machiav3lli.fdroid.database.dao.DownloadedDao
import com.machiav3lli.fdroid.database.dao.ExodusInfoDao
import com.machiav3lli.fdroid.database.dao.ExtrasDao
import com.machiav3lli.fdroid.database.dao.InstallTaskDao
import com.machiav3lli.fdroid.database.dao.InstalledDao
import com.machiav3lli.fdroid.database.dao.ProductDao
import com.machiav3lli.fdroid.database.dao.ProductTempDao
import com.machiav3lli.fdroid.database.dao.ReleaseDao
import com.machiav3lli.fdroid.database.dao.ReleaseTempDao
import com.machiav3lli.fdroid.database.dao.RepositoryDao
import com.machiav3lli.fdroid.database.dao.TrackerDao
import com.machiav3lli.fdroid.database.entity.Category
import com.machiav3lli.fdroid.database.entity.CategoryTemp
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.database.entity.ExodusInfo
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.InstallTask
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.ProductTemp
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.database.entity.ReleaseTemp
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV10
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV11
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV12
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV14
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV15
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV17
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV18
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV19
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV20
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV21
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV22
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV23
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV29
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV30
import com.machiav3lli.fdroid.database.entity.Repository.Companion.addedReposV9
import com.machiav3lli.fdroid.database.entity.Repository.Companion.archiveRepos
import com.machiav3lli.fdroid.database.entity.Repository.Companion.defaultRepositories
import com.machiav3lli.fdroid.database.entity.Repository.Companion.removedReposV28
import com.machiav3lli.fdroid.database.entity.Repository.Companion.removedReposV29
import com.machiav3lli.fdroid.database.entity.Tracker
import com.machiav3lli.fdroid.service.worker.SyncWorker.Companion.enableRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

@Database(
    entities = [
        Repository::class,
        Product::class,
        Release::class,
        ReleaseTemp::class,
        ProductTemp::class,
        Category::class,
        CategoryTemp::class,
        Installed::class,
        Extras::class,
        ExodusInfo::class,
        Tracker::class,
        Downloaded::class,
        InstallTask::class,
    ],
    version = 30,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = 8,
            to = 9,
            spec = DatabaseX.Companion.MigrationSpec8to9::class
        ),
        AutoMigration(
            from = 9,
            to = 10,
            spec = DatabaseX.Companion.MigrationSpec9to10::class
        ),
        AutoMigration(
            from = 10,
            to = 11,
            spec = DatabaseX.Companion.MigrationSpec10to11::class
        ),
        AutoMigration(
            from = 11,
            to = 12,
            spec = DatabaseX.Companion.MigrationSpec11to12::class
        ),
        AutoMigration(
            from = 12,
            to = 13,
        ),
        AutoMigration(
            from = 13,
            to = 14,
            spec = DatabaseX.Companion.MigrationSpec13to14::class
        ),
        AutoMigration(
            from = 14,
            to = 15,
            spec = DatabaseX.Companion.MigrationSpec14to15::class
        ),
        AutoMigration(
            from = 15,
            to = 16,
        ),
        AutoMigration(
            from = 16,
            to = 17,
            spec = DatabaseX.Companion.MigrationSpec16to17::class
        ),
        AutoMigration(
            from = 17,
            to = 18,
            spec = DatabaseX.Companion.MigrationSpec17to18::class
        ),
        AutoMigration(
            from = 18,
            to = 19,
            spec = DatabaseX.Companion.MigrationSpec18to19::class
        ),
        AutoMigration(
            from = 19,
            to = 20,
            spec = DatabaseX.Companion.MigrationSpec19to20::class
        ),
        AutoMigration(
            from = 20,
            to = 21,
            spec = DatabaseX.Companion.MigrationSpec20to21::class
        ),
        AutoMigration(
            from = 21,
            to = 22,
            spec = DatabaseX.Companion.MigrationSpec21to22::class
        ),
        AutoMigration(
            from = 22,
            to = 23,
            spec = DatabaseX.Companion.MigrationSpec22to23::class
        ),
        AutoMigration(
            from = 23,
            to = 24,
        ),
        AutoMigration(
            from = 24,
            to = 25,
            spec = DatabaseX.Companion.MigrationSpec24to25::class
        ),
        AutoMigration(
            from = 25,
            to = 26,
            spec = DatabaseX.Companion.AutoMigration25to26::class
        ),
        AutoMigration(
            from = 26,
            to = 27,
        ),
        AutoMigration(
            from = 27,
            to = 28,
            spec = DatabaseX.Companion.AutoMigration27to28::class
        ),
        AutoMigration(
            from = 28,
            to = 29,
            spec = DatabaseX.Companion.AutoMigration28to29::class
        ),
        AutoMigration(
            from = 29,
            to = 30,
            spec = DatabaseX.Companion.AutoMigration29to30::class
        ),
    ]
)
@TypeConverters(Converters::class)
abstract class DatabaseX : RoomDatabase() {
    abstract fun getRepositoryDao(): RepositoryDao
    abstract fun getProductDao(): ProductDao
    abstract fun getReleaseDao(): ReleaseDao
    abstract fun getReleaseTempDao(): ReleaseTempDao
    abstract fun getProductTempDao(): ProductTempDao
    abstract fun getCategoryDao(): CategoryDao
    abstract fun getCategoryTempDao(): CategoryTempDao
    abstract fun getInstalledDao(): InstalledDao
    abstract fun getExtrasDao(): ExtrasDao
    abstract fun getExodusInfoDao(): ExodusInfoDao
    abstract fun getTrackerDao(): TrackerDao
    abstract fun getDownloadedDao(): DownloadedDao
    abstract fun getInstallTaskDao(): InstallTaskDao

    companion object {
        @Volatile
        private var INSTANCE: DatabaseX? = null

        fun getInstance(context: Context): DatabaseX {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        DatabaseX::class.java,
                        "main_database.db"
                    )
                    .fallbackToDestructiveMigration()
                    .build()
                instance.let { instance ->
                    GlobalScope.launch(Dispatchers.IO) {
                        if (instance.getRepositoryDao()
                                .getCount() == 0
                        ) defaultRepositories.forEach {
                            instance.getRepositoryDao().put(it)
                        }
                    }
                }
                INSTANCE = instance
                instance
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

        class MigrationSpec13to14 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(13)
            }
        }

        class MigrationSpec14to15 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(14)
            }
        }

        class MigrationSpec16to17 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(16)
            }
        }

        class MigrationSpec17to18 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(17)
            }
        }

        class MigrationSpec18to19 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(18)
            }
        }

        class MigrationSpec19to20 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(19)
            }
        }

        class MigrationSpec20to21 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(20)
            }
        }

        class MigrationSpec21to22 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(21)
            }
        }

        class MigrationSpec22to23 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(22)
            }
        }

        class MigrationSpec24to25 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(24)
            }
        }

        @RenameColumn(tableName = TABLE_REPOSITORY, fromColumnName = "_id", toColumnName = ROW_ID)
        @RenameTable(fromTableName = "ExodusInfo", toTableName = TABLE_EXODUS_INFO)
        @RenameTable(fromTableName = "Tracker", toTableName = TABLE_TRACKER)
        @RenameTable(fromTableName = "InstallTask", toTableName = TABLE_INSTALL_TASK)
        class AutoMigration25to26 : AutoMigrationSpec

        class AutoMigration27to28 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(27)
            }
        }

        class AutoMigration28to29 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(28)
            }
        }

        class AutoMigration29to30 : AutoMigrationSpec {
            override fun onPostMigrate(db: SupportSQLiteDatabase) {
                super.onPostMigrate(db)
                onPostMigrate(29)
            }
        }

        fun onPostMigrate(from: Int) {
            val preRepos = mutableListOf<Repository>()
            if (from == 8) preRepos.addAll(addedReposV9)
            if (from == 9) preRepos.addAll(addedReposV10)
            if (from == 10) preRepos.addAll(addedReposV11)
            if (from == 11) preRepos.addAll(addedReposV12)
            if (from == 13) preRepos.addAll(addedReposV14)
            if (from == 14) preRepos.addAll(addedReposV15)
            if (from == 16) preRepos.addAll(addedReposV17)
            if (from == 17) preRepos.addAll(addedReposV18)
            if (from == 18) preRepos.addAll(addedReposV19)
            if (from == 19) preRepos.addAll(addedReposV20)
            if (from == 20) preRepos.addAll(addedReposV21)
            if (from == 21) preRepos.addAll(addedReposV22)
            if (from == 22) preRepos.addAll(addedReposV23)
            if (from == 28) preRepos.addAll(addedReposV29)
            if (from == 28) preRepos.addAll(addedReposV30)
            GlobalScope.launch(Dispatchers.IO) {
                preRepos.forEach {
                    INSTANCE?.getRepositoryDao()?.put(it)
                    if (from == 20) INSTANCE?.getDownloadedDao()?.emptyTable()
                }
                if (from == 24) archiveRepos.forEach {
                    enableRepo(it, false)
                    INSTANCE?.getRepositoryDao()?.deleteByAddress(it.address)
                }
                if (from == 27) removedReposV28.forEach {
                    enableRepo(it, false)
                    INSTANCE?.getRepositoryDao()?.deleteByAddress(it.address)
                }
                if (from == 28) removedReposV29.forEach {
                    enableRepo(it, false)
                    INSTANCE?.getRepositoryDao()?.deleteByAddress(it.address)
                }
            }
        }
    }

    fun cleanUp(vararg pairs: Pair<Long, Boolean>) {
        runInTransaction {
            pairs.forEach { (id, enabled) ->
                getProductDao().deleteById(id)
                getCategoryDao().deleteById(id)
                getReleaseDao().deleteById(id)
                if (enabled) getRepositoryDao().deleteById(id)
            }
        }
    }

    fun cleanUp(pairs: Set<Pair<Long, Boolean>>) = cleanUp(*pairs.toTypedArray())

    fun finishTemporary(repository: Repository, success: Boolean) {
        runInTransaction {
            if (success) {
                getProductDao().deleteById(repository.id)
                getCategoryDao().deleteById(repository.id)
                getReleaseDao().deleteById(repository.id)
                getProductDao().insert(*(getProductTempDao().getAll()))
                getCategoryDao().insert(*(getCategoryTempDao().getAll()))
                getReleaseDao().insert(*(getReleaseTempDao().getAll()))
                getRepositoryDao().put(repository)
            }
            getProductTempDao().emptyTable()
            getCategoryTempDao().emptyTable()
            getReleaseTempDao().emptyTable()
        }
    }
}

val databaseModule = module {
    single { DatabaseX.getInstance(androidContext()) }
    single { get<DatabaseX>().getRepositoryDao() }
    single { get<DatabaseX>().getProductDao() }
    single { get<DatabaseX>().getReleaseDao() }
    single { get<DatabaseX>().getReleaseTempDao() }
    single { get<DatabaseX>().getProductTempDao() }
    single { get<DatabaseX>().getCategoryDao() }
    single { get<DatabaseX>().getCategoryTempDao() }
    single { get<DatabaseX>().getInstalledDao() }
    single { get<DatabaseX>().getExtrasDao() }
    single { get<DatabaseX>().getExodusInfoDao() }
    single { get<DatabaseX>().getTrackerDao() }
    single { get<DatabaseX>().getDownloadedDao() }
    single { get<DatabaseX>().getInstallTaskDao() }
}
