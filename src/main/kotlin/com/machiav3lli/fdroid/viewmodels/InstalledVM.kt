package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.IconDetails
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Licenses
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.entity.Request
import com.machiav3lli.fdroid.entity.Section
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

open class InstalledVM(val db: DatabaseX) : ViewModel() {

    private val cc = Dispatchers.IO
    private val sortFilter = MutableStateFlow("")

    fun setSortFilter(value: String) {
        viewModelScope.launch { sortFilter.emit(value) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val installed = db.getInstalledDao().getAllFlow().mapLatest {
        it.associateBy(Installed::packageName)
    }

    private var primaryRequest: StateFlow<Request> = combine(
        sortFilter,
        installed
    ) { _, _ ->
        Request.ProductsInstalled(Section.All)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Request.ProductsInstalled(Section.All)
    )

    val installedProducts: StateFlow<List<Product>?> = combine(
        primaryRequest,
        installed,
        db.getProductDao().queryFlowList(primaryRequest.value),
        sortFilter,
        db.getExtrasDao().getAllFlow(),
    ) { _, _, list, _, _ ->
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    val updates: StateFlow<List<Product>?> = combine(
        installed,
        db.getProductDao().queryFlowList(Request.ProductsUpdates(Section.All)),
        db.getExtrasDao().getAllFlow(),
    ) { _, list, _ ->
        list
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val repositories = db.getRepositoryDao().getAllFlow().mapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories = db.getCategoryDao().getAllNamesFlow().mapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class)
    val licenses = db.getProductDao().getAllLicensesFlow().mapLatest {
        it.map(Licenses::licenses).flatten().distinct()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val iconDetails = db.getProductDao().getIconDetailsFlow().mapLatest {
        it.associateBy(IconDetails::packageName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap(),
    )

    val downloaded = db.getDownloadedDao().getAllFlow().stateIn(
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
            db.getDownloadedDao()
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
            val oldValue = db.getExtrasDao()[packageName]
            if (oldValue != null) db.getExtrasDao()
                .insertReplace(oldValue.copy(favorite = setBoolean))
            else db.getExtrasDao()
                .insertReplace(Extras(packageName, favorite = setBoolean))
        }
    }

    class Factory(val db: DatabaseX) :
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
