package com.machiav3lli.fdroid.database

import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.database.dao.CategoryDao
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
import com.machiav3lli.fdroid.database.entity.Repository
import io.realm.kotlin.Realm
import org.koin.dsl.module

class RealmRepo(val realm: Realm) {
    val repositoryDao = RepositoryDao(realm)
    val productDao = ProductDao(realm)
    val releaseDao = ReleaseDao(realm)
    val releaseTempDao = ReleaseTempDao(realm)
    val productTempDao = ProductTempDao(realm)
    val categoryDao = CategoryDao(realm)
    val installedDao = InstalledDao(realm)
    val extrasDao = ExtrasDao(realm)
    val exodusInfoDao = ExodusInfoDao(realm)
    val trackerDao = TrackerDao(realm)
    val downloadedDao = DownloadedDao(realm)
    val installTaskDao = InstallTaskDao(realm)


    fun cleanUp(vararg pairs: Pair<Long, Boolean>) {
        realm.writeBlocking {
            pairs.forEach { pair ->
                val id = pair.first
                productDao.deleteById(id)
                if (pair.second) repositoryDao.deleteById(id)
            }
        }
    }

    fun cleanUp(pairs: Set<Pair<Long, Boolean>>) = cleanUp(*pairs.toTypedArray())

    fun finishTemporary(repository: Repository, success: Boolean) {
        realm.writeBlocking {
            if (success) {
                productDao.deleteById(repository.id)
                productDao.insert(*productTempDao.all.toTypedArray())
                releaseDao.insert(*releaseTempDao.all.toTypedArray())
                repositoryDao.upsert(repository)
            }
            productTempDao.emptyTable()
            releaseTempDao.emptyTable()
        }
    }
}

val realmRepoModule = module {
    single {
        RealmRepo(MainApplication.realm)
    }
}
