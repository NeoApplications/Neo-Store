package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Licenses
import com.machiav3lli.fdroid.database.entity.Product
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
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
open class SingleListVM(
    val db: DatabaseX,
    src: Source,
) : ViewModel() {
    private val cc = Dispatchers.IO
    private val _sortFilter = MutableStateFlow("")
    val sortFilter: StateFlow<String> = _sortFilter
    private val _source = MutableStateFlow(src)
    val source: StateFlow<Source> = _source

    fun setSortFilter(value: String) {
        viewModelScope.launch { _sortFilter.emit(value) }
    }

    private val query = MutableStateFlow("")

    fun setSearchQuery(value: String) {
        viewModelScope.launch { query.emit(value) }
    }

    fun setSource(ns: Source) {
        viewModelScope.launch { _source.emit(ns) }
    }

    fun request(source: Source): Request = when (source) {
        Source.AVAILABLE        -> Request.productsAll()
        Source.FAVORITES        -> Request.productsFavorites()
        Source.SEARCH           -> Request.productsSearch()
        Source.SEARCH_INSTALLED -> Request.productsSearchInstalled()
        Source.SEARCH_NEW       -> Request.productsSearchNew()
        Source.INSTALLED        -> Request.productsInstalled()
        Source.UPDATES          -> Request.productsUpdates()
        Source.UPDATED          -> Request.productsUpdated()
        Source.NEW              -> Request.productsNew()
    }

    val installed = db.getInstalledDao().getAllFlow().distinctUntilChanged().mapLatest {
        it.associateBy(Installed::packageName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap()
    )

    private var request: StateFlow<Request> = combine(
        sortFilter,
        _source,
        installed
    ) { _, src, _ ->
        request(src)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = request(src)
    )

    private val products: StateFlow<List<Product>> = combine(
        request,
        installed,
        db.getProductDao().queryFlowList(request.value).distinctUntilChanged(),
        db.getExtrasDao().getAllFlow().distinctUntilChanged(),
    ) { req, _, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(req)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    @OptIn(FlowPreview::class)
    val filteredProducts: StateFlow<List<ProductItem>> =
        combine(products, query.debounce(400)) { products, query ->
            products.matchSearchQuery(query)
                .map { it.toItem(installed.value[it.packageName]) }
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptyList()
        )

    val repositories = db.getRepositoryDao().getAllFlow().distinctUntilChanged()

    val categories = db.getCategoryDao().getAllNamesFlow().distinctUntilChanged()

    val licenses = db.getProductDao().getAllLicensesFlow().distinctUntilChanged().mapLatest {
        it.map(Licenses::licenses).flatten().distinct()
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
}

class ExploreVM(db: DatabaseX) : SingleListVM(db, Source.AVAILABLE) {
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

class SearchVM(db: DatabaseX) : SingleListVM(db, Source.SEARCH) {
    class Factory(val db: DatabaseX) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SearchVM::class.java)) {
                return SearchVM(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
