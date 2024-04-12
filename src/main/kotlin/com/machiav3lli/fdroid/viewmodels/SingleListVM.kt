package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Licenses
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.entity.Request
import com.machiav3lli.fdroid.entity.Section
import com.machiav3lli.fdroid.entity.Source
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

open class SingleListVM(
    val db: DatabaseX,
    source: Source,
) : ViewModel() {
    private val cc = Dispatchers.IO
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
            Source.AVAILABLE -> Request.productsAll(mSections)
            Source.SEARCH    -> Request.productsSearch()
            Source.INSTALLED -> Request.productsInstalled()
            Source.UPDATES   -> Request.productsUpdates()
            Source.UPDATED   -> Request.productsUpdated()
            Source.NEW       -> Request.productsNew()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val installed = db.getInstalledDao().getAllFlow().mapLatest {
        it.associateBy(Installed::packageName)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap()
    )

    private var request: StateFlow<Request> = combineTransform(
        sortFilter,
        sections,
        installed
    ) { _, _, _ ->
        val newRequest = request(source)
        emit(newRequest)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = request(source)
    )

    private val products: StateFlow<List<Product>?> = combine(
        request,
        installed,
        db.getProductDao().queryFlowList(request.value),
        db.getExtrasDao().getAllFlow(),
    ) { req, _, _, _ ->
        withContext(cc) {
            db.getProductDao().queryObject(req)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    @OptIn(FlowPreview::class)
    val filteredProducts: StateFlow<List<Product>?> =
        combine(products, query.debounce(400)) { products, query ->
            products?.matchSearchQuery(query)
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val repositories = db.getRepositoryDao().getAllFlow().mapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class)
    val categories = db.getCategoryDao().getAllNamesFlow().mapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class)
    val licenses = db.getProductDao().getAllLicensesFlow().mapLatest {
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
