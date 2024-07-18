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
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.entity.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
open class InstalledVM(val db: DatabaseX) : ViewModel() {

    private val cc = Dispatchers.IO
    private val _sortFilter = MutableStateFlow("")
    val sortFilter: StateFlow<String> = _sortFilter

    fun setSortFilter(value: String) {
        viewModelScope.launch { _sortFilter.emit(value) }
    }

    val installed = db.getInstalledDao().getAllFlow().distinctUntilChanged().mapLatest {
        it.associateBy(Installed::packageName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap(),
    )

    val installedProducts: StateFlow<List<ProductItem>> = combine(
        sortFilter,
        installed,
        db.getProductDao().queryFlowList(Request.Installed).distinctUntilChanged(),
        db.getExtrasDao().getAllFlow().distinctUntilChanged(),
    ) { _, installed, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(Request.Installed)
                .map { it.toItem(installed[it.packageName]) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val updates: StateFlow<List<ProductItem>> = combine(
        installed,
        db.getProductDao().queryFlowList(Request.Updates),
        db.getExtrasDao().getAllFlow(),
    ) { installed, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(Request.Updates)
                .map { it.toItem(installed[it.packageName]) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val repositories = db.getRepositoryDao().getAllFlow().distinctUntilChanged()

    val categories = db.getCategoryDao().getAllNamesFlow().distinctUntilChanged()

    val licenses = db.getProductDao().getAllLicensesFlow().distinctUntilChanged().mapLatest {
        it.map(Licenses::licenses).flatten().distinct()
    }

    val iconDetails = db.getProductDao().getIconDetailsFlow().distinctUntilChanged().mapLatest {
        it.associateBy(IconDetails::packageName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap(),
    )

    val downloaded = db.getDownloadedDao().getAllFlow()
        .debounce(250L)
        .stateIn(
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
                .upsert(oldValue.copy(favorite = setBoolean))
            else db.getExtrasDao()
                .upsert(Extras(packageName, favorite = setBoolean))
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
