package com.machiav3lli.fdroid.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.entity.Source
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.utils.matchSearchQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)
class SearchVM(
    private val productsRepo: ProductsRepository,
    extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
) : ViewModel() {
    val sortFilter: StateFlow<String>
        private field = MutableStateFlow("")
    val query: StateFlow<String>
        private field = MutableStateFlow("")
    val source: StateFlow<Source>
        private field = MutableStateFlow(Source.SEARCH)

    val installed = installedRepo.getAll().map {
        it.associateBy(Installed::packageName).apply {
            Log.d(TAG, "Installed list size: ${this.size}")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = emptyMap()
    )

    private val request: StateFlow<Request> = combine(
        sortFilter,
        source,
    ) { _, src ->
        when (src) {
            Source.SEARCH_INSTALLED -> Request.SearchInstalled
            Source.SEARCH_NEW       -> Request.SearchNew
            else                    -> Request.Search
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = Request.Search
    )

    private val productsSource = combine(
        request,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { req, _, _ -> productsRepo.getProducts(req) }
        .flatMapLatest { it }
        .distinctUntilChanged()

    val filteredProducts: StateFlow<List<ProductItem>> = combine(
        productsSource,
        query.debounce(400),
        installed,
    ) { products, searchQuery, installedMap ->
        products.matchSearchQuery(searchQuery)
            .map { it.toItem(installedMap[it.product.packageName]) }.apply {
                Log.d(TAG, "Search products list size: ${this.size}")
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = emptyList()
    )

    fun setSortFilter(value: String) = viewModelScope.launch {
        sortFilter.update { value }
    }

    fun setSearchQuery(value: String) = viewModelScope.launch {
        query.update { value }
    }

    fun setSearchSource(newSource: Source) = viewModelScope.launch {
        source.update { newSource }
    }

    companion object {
        private const val TAG = "SearchVM"
    }
}