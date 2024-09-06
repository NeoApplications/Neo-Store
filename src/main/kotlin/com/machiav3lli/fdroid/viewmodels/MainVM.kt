package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.IconDetails
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Licenses
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.entity.Page
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.entity.Request
import com.machiav3lli.fdroid.entity.Source
import com.machiav3lli.fdroid.utility.matchSearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)
open class MainVM(val db: DatabaseX) : ViewModel() {
    private val cc = Dispatchers.IO
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    private val _sortFilterLatest = MutableStateFlow("")
    val sortFilterLatest: StateFlow<String> = _sortFilterLatest
    private val _sortFilterExplore = MutableStateFlow("")
    val sortFilterExplore: StateFlow<String> = _sortFilterExplore
    private val _sortFilterSearch = MutableStateFlow("")
    val sortFilterSearch: StateFlow<String> = _sortFilterSearch
    private val _sortFilterInstalled = MutableStateFlow("")
    val sortFilterInstalled: StateFlow<String> = _sortFilterInstalled

    private val querySearch = MutableStateFlow("")
    private val _sourceExplore = MutableStateFlow(Source.NONE)
    private val sourceExplore: StateFlow<Source> = _sourceExplore
    private val _sourceSearch = MutableStateFlow(Source.SEARCH)
    val sourceSearch: StateFlow<Source> = _sourceSearch

    private fun request(source: Source): Request = when (source) {
        Source.AVAILABLE        -> Request.All
        Source.FAVORITES        -> Request.Favorites
        Source.SEARCH           -> Request.Search
        Source.SEARCH_INSTALLED -> Request.SearchInstalled
        Source.SEARCH_NEW       -> Request.SearchNew
        Source.INSTALLED        -> Request.Installed
        Source.UPDATES          -> Request.Updates
        Source.UPDATED          -> Request.Updated
        Source.NEW              -> Request.New
        Source.NONE             -> Request.None
    }

    val repositories = db.getRepositoryDao().getAllFlow().distinctUntilChanged()

    val categories = db.getCategoryDao().getAllNamesFlow().distinctUntilChanged()

    val licenses = db.getProductDao().getAllLicensesFlow().distinctUntilChanged().mapLatest {
        it.map(Licenses::licenses).flatten().distinct()
    }

    val iconDetails = db.getProductDao().getIconDetailsFlow().distinctUntilChanged().mapLatest {
        it.associateBy(IconDetails::packageName)
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap(),
    )

    val installed = db.getInstalledDao().getAllFlow().mapLatest {
        it.associateBy(Installed::packageName)
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap()
    )

    private var requestExplore: StateFlow<Request> = combine(
        sortFilterExplore,
        sourceExplore,
        installed
    ) { _, src, _ ->
        request(src)
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = request(Source.NONE)
    )

    private var requestSearch: StateFlow<Request> = combine(
        sortFilterSearch,
        sourceSearch,
        installed
    ) { _, src, _ ->
        request(src)
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = request(Source.SEARCH)
    )

    val productsExplore: StateFlow<List<ProductItem>> = combine(
        requestExplore,
        installed,
        db.getProductDao().queryFlowList(requestExplore.value).distinctUntilChanged(),
        db.getExtrasDao().getAllFlow().distinctUntilChanged(),
    ) { req, _, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(req)
                .map { it.toItem(installed.value[it.packageName]) }
        }
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    private val productsSearch: StateFlow<List<Product>> = combine(
        requestSearch,
        installed,
        db.getProductDao().queryFlowList(requestSearch.value).distinctUntilChanged(),
        db.getExtrasDao().getAllFlow().distinctUntilChanged(),
    ) { req, _, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(req)
        }
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val filteredProdsSearch: StateFlow<List<ProductItem>> =
        combine(productsSearch, querySearch.debounce(400)) { products, query ->
            products.matchSearchQuery(query)
                .map { it.toItem(installed.value[it.packageName]) }
        }.stateIn(
            ioScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val installedProdsInstalled: StateFlow<List<ProductItem>> = combine(
        sortFilterInstalled,
        installed,
        db.getProductDao().queryFlowList(Request.Installed).distinctUntilChanged(),
        db.getExtrasDao().getAllFlow().distinctUntilChanged(),
    ) { _, installed, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(Request.Installed)
                .map { it.toItem(installed[it.packageName]) }
        }
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val downloaded = db.getDownloadedDao().getAllFlow()
        .debounce(250L)
        .stateIn(
            scope = ioScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList(),
        )

    val updatedProdsLatest: StateFlow<List<ProductItem>> = combine(
        sortFilterLatest,
        installed,
        db.getProductDao().queryFlowList(Request.Updated).distinctUntilChanged(),
        db.getExtrasDao().getAllFlow().distinctUntilChanged(),
    ) { _, installed, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(Request.Updated)
                .map { it.toItem(installed[it.packageName]) }
        }
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val newProdsLatest: StateFlow<List<ProductItem>> = combine(
        installed,
        db.getProductDao().queryFlowList(Request.New).distinctUntilChanged(),
        db.getExtrasDao().getAllFlow().distinctUntilChanged(),
    ) { installed, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(Request.New)
                .map { it.toItem(installed[it.packageName]) }
        }
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val updateProdsInstalled: StateFlow<List<ProductItem>> = combine(
        installed,
        db.getProductDao().queryFlowList(Request.Updates),
        db.getExtrasDao().getAllFlow(),
    ) { installed, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(Request.Updates)
                .map { it.toItem(installed[it.packageName]) }
        }
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    fun setSortFilter(page: Page, value: String) = viewModelScope.launch {
        when (page) {
            Page.EXPLORE   -> _sortFilterExplore.emit(value)
            Page.SEARCH    -> _sortFilterSearch.emit(value)
            Page.INSTALLED -> _sortFilterInstalled.emit(value)
            Page.LATEST    -> _sortFilterLatest.emit(value)
        }
    }

    fun setSearchQuery(value: String) {
        viewModelScope.launch { querySearch.emit(value) }
    }

    fun setExploreSource(ns: Source) {
        viewModelScope.launch { _sourceExplore.emit(ns) }
    }

    fun setSearchSource(ns: Source) {
        viewModelScope.launch { _sourceSearch.emit(ns) }
    }

    fun eraseDownloaded(downloaded: Downloaded) {
        viewModelScope.launch {
            deleteDownloaded(downloaded)
        }
    }

    private suspend fun deleteDownloaded(downloaded: Downloaded) {
        withContext(cc) {
            db.getDownloadedDao().delete(
                downloaded.packageName,
                downloaded.version,
                downloaded.repositoryId,
                downloaded.cacheFileName
            )
            Cache.eraseDownload(MainApplication.context, downloaded.cacheFileName)
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

    class Factory(val db: DatabaseX) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainVM::class.java)) {
                return MainVM(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
