package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.RealmRepo
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.IconDetails
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.entity.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
open class InstalledVM(val db: RealmRepo) : ViewModel() {

    private val cc = Dispatchers.IO
    private val sortFilter = MutableStateFlow("")

    fun setSortFilter(value: String) {
        viewModelScope.launch { sortFilter.emit(value) }
    }

    val installed = db.installedDao.allFlow.mapLatest {
        it.associateBy(Installed::packageName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap(),
    )

    val installedProducts: StateFlow<List<Product>?> = combine(
        sortFilter,
        installed,
        db.productDao.queryFlowList(Request.productsInstalled()),
        db.extrasDao.allFlow,
    ) { _, _, _, _ ->
        withContext(cc) {
            db.productDao.queryObject(Request.productsInstalled())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    val updates: StateFlow<List<Product>?> = combine(
        installed,
        db.productDao.queryFlowList(Request.productsUpdates()),
        db.extrasDao.allFlow,
    ) { _, _, _ ->
        withContext(cc) {
            db.productDao.queryObject(Request.productsUpdates())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    val repositories = db.repositoryDao.allFlow.mapLatest { it }

    val categories = db.categoryDao.allNamesFlow.mapLatest { it }

    val licenses = db.productDao.getAllLicensesFlow()

    val iconDetails = db.productDao.getIconDetailsFlow().mapLatest {
        it.associateBy(IconDetails::packageName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap(),
    )

    val downloaded = db.downloadedDao.allFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    fun eraseDownloaded(downloaded: Downloaded) {
        viewModelScope.launch {
            deleteDownloaded(downloaded)
        }
    }

    private suspend fun deleteDownloaded(downloaded: Downloaded) {
        withContext(cc) {
            db.downloadedDao
                .delete(downloaded.packageName, downloaded.version, downloaded.cacheFileName)
        }
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveFavorite(packageName, setBoolean)
        }
    }

    private suspend fun saveFavorite(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val exstVal = db.extrasDao[packageName]
                ?: Extras(packageName)
            db.extrasDao
                .upsert(exstVal.apply { favorite = setBoolean })
        }
    }

    class Factory(val db: RealmRepo) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InstalledVM::class.java)) {
                return InstalledVM(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
