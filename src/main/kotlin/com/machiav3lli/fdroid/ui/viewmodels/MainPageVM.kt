package com.machiav3lli.fdroid.ui.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.entity.Request
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.entity.Source
import com.machiav3lli.fdroid.service.DownloadService
import com.machiav3lli.fdroid.utility.matchSearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class MainPageVM(
    val db: DatabaseX,
    primarySource: Source,
    secondarySource: Source,
) : ViewModel() {
    // TODO add better sort/filter fields

    private val sortFilter = MutableStateFlow("")

    fun setSortFilter(value: String) {
        viewModelScope.launch { sortFilter.emit(value) }
    }

    private val query = MutableStateFlow("")

    fun setSearchQuery(value: String) {
        viewModelScope.launch { query.emit(value) }
    }

    private val sections = MutableStateFlow<Section>(Section.All)

    fun setSections(value: Section) {
        viewModelScope.launch { sections.emit(value) }
    }

    fun request(source: Source): Request {
        var mSections: Section = Section.All
        sections.value.let { if (source.sections) mSections = it }
        return when (source) {
            Source.AVAILABLE -> Request.ProductsAll(mSections)
            Source.INSTALLED -> Request.ProductsInstalled(mSections)
            Source.UPDATES   -> Request.ProductsUpdates(mSections)
            Source.UPDATED   -> Request.ProductsUpdated(mSections)
            Source.NEW       -> Request.ProductsNew(mSections)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val installed = db.installedDao.allFlow.mapLatest {
        it.associateBy(Installed::packageName)
    }

    private var primaryRequest: StateFlow<Request> = combineTransform(
        sortFilter,
        sections,
        installed
    ) { a, _, _ ->
        val newRequest = request(primarySource)
        emit(newRequest)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = request(primarySource)
    )

    val primaryProducts: StateFlow<List<Product>?> = combine(
        primaryRequest,
        db.productDao.queryFlowList(primaryRequest.value),
        sortFilter
    ) { a, _, _ ->
        withContext(Dispatchers.IO) {
            db.productDao.queryObject(a)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    @OptIn(FlowPreview::class)
    val filteredProducts: StateFlow<List<Product>?> =
        combine(primaryProducts, query.debounce(400)) { products, query ->
            withContext(Dispatchers.IO) {
                products?.matchSearchQuery(query)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    private var secondaryRequest = MutableStateFlow(request(secondarySource))

    val secondaryProducts: StateFlow<List<Product>?> = combine(
        secondaryRequest,
        installed,
        db.productDao.queryFlowList(secondaryRequest.value),
        db.extrasDao.allFlow
    ) { a, _, _, _ ->
        withContext(Dispatchers.IO) {
            db.productDao.queryObject(a)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val repositories = db.repositoryDao.allFlow.mapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories = db.categoryDao.allNamesFlow.mapLatest { it }

    val downloadsMap = mutableStateMapOf<String, Pair<ProductItem, DownloadService.State>>()

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveFavorite(packageName, setBoolean)
        }
    }

    private suspend fun saveFavorite(packageName: String, setBoolean: Boolean) {
        withContext(Dispatchers.IO) {
            val oldValue = db.extrasDao[packageName]
            if (oldValue != null) db.extrasDao
                .insertReplace(oldValue.copy(favorite = setBoolean))
            else db.extrasDao
                .insertReplace(Extras(packageName, favorite = setBoolean))
        }
    }

    fun updateDownloadState(state: DownloadService.State) {
        when (state) {
            is DownloadService.State.Downloading,
            is DownloadService.State.Pending,
            is DownloadService.State.Connecting,
            -> {
                val installed = db.installedDao.get(state.packageName)
                val product: Product
                db.productDao.get(state.packageName).also { products ->
                    product = products.filter {
                        it.compatible && (installed == null || it.signatures.contains(installed.signature))
                    }.maxBy { it.suggestedVersionCode }
                }
                downloadsMap[state.packageName] = Pair(product.toItem(installed), state)
            }
            is DownloadService.State.Success,
            -> {
                val installed = db.installedDao.get(state.packageName)
                if (installed != null && installed.versionCode == state.release.versionCode)
                    downloadsMap.remove(state.packageName)
                else {
                    val product: Product
                    db.productDao.get(state.packageName).also { products ->
                        product = products.filter {
                            it.compatible && (installed == null || it.signatures.contains(installed.signature))
                        }.maxBy { it.suggestedVersionCode }
                    }
                    downloadsMap[state.packageName] = Pair(product.toItem(installed), state)
                }
            }
            is DownloadService.State.Error,
            is DownloadService.State.Cancel,
            -> {
                downloadsMap.remove(state.packageName)
            }
        }
    }
}

class ExploreVM(db: DatabaseX) : MainPageVM(db, Source.AVAILABLE, Source.AVAILABLE) {
    class Factory(val db: DatabaseX) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExploreVM::class.java)) {
                return ExploreVM(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class LatestVM(db: DatabaseX) : MainPageVM(db, Source.UPDATED, Source.NEW) {
    class Factory(val db: DatabaseX) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LatestVM::class.java)) {
                return LatestVM(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

class InstalledVM(db: DatabaseX) : MainPageVM(db, Source.INSTALLED, Source.UPDATES) {
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
