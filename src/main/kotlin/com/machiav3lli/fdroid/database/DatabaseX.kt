package com.machiav3lli.fdroid.database

/*@Database(
    entities = [
    ],
    version = 22,
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
                                .getCount() == 0L
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

        @DeleteColumn(tableName = TABLE_PRODUCT, columnName = ROW_RELEASES)
        @DeleteColumn(tableName = TABLE_PRODUCT, columnName = ROW_VERSION_CODE)
        @DeleteColumn(tableName = TABLE_PRODUCT, columnName = ROW_SIGNATURES)
        @DeleteColumn(tableName = TABLE_PRODUCT, columnName = ROW_COMPATIBLE)
        class MigrationSpec21to22 : AutoMigrationSpec

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
            GlobalScope.launch(Dispatchers.IO) {
                preRepos.forEach {
                    INSTANCE?.getRepositoryDao()?.put(it)
                    if (from == 20) INSTANCE?.getDownloadedDao()?.emptyTable()
                }
            }
        }
    }

    fun cleanUp(vararg pairs: Pair<Long, Boolean>) {
        runInTransaction {
            pairs.forEach { pair ->
                val id = pair.first
                getProductDao().deleteById(id)
                getCategoryDao().deleteById(id)
                if (pair.second) getRepositoryDao().deleteById(id)
            }
        }
    }

    fun cleanUp(pairs: Set<Pair<Long, Boolean>>) = cleanUp(*pairs.toTypedArray())

    fun finishTemporary(repository: Repository, success: Boolean) {
        runInTransaction {
            if (success) {
                getProductDao().deleteById(repository.id)
                getCategoryDao().deleteById(repository.id)
                getProductDao().insert(*(getProductTempDao().all))
                getCategoryDao().insert(*(getCategoryTempDao().all))
                getReleaseDao().insert(*(getReleaseTempDao().all))
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
    factory { get<DatabaseX>().getRepositoryDao() }
    factory { get<DatabaseX>().getProductDao() }
    factory { get<DatabaseX>().getReleaseDao() }
    factory { get<DatabaseX>().getReleaseTempDao() }
    factory { get<DatabaseX>().getProductTempDao() }
    factory { get<DatabaseX>().getCategoryDao() }
    factory { get<DatabaseX>().getCategoryTempDao() }
    factory { get<DatabaseX>().getInstalledDao() }
    factory { get<DatabaseX>().getExtrasDao() }
    factory { get<DatabaseX>().getExodusInfoDao() }
    factory { get<DatabaseX>().getTrackerDao() }
    factory { get<DatabaseX>().getDownloadedDao() }
    factory { get<DatabaseX>().getInstallTaskDao() }
}
*/