package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.content.Preferences
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
    private val _searchInput = MutableStateFlow(SearchInput())

    private val installed = installedRepo.getMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyMap()
        )

    private val productsSource = combine(
        _searchInput.debounce { 400 },
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { input, _, _ ->
        productsRepo.getProducts(
            when (input.source) {
                Source.SEARCH_INSTALLED -> Request.SearchInstalled
                Source.SEARCH_NEW       -> Request.SearchNew
                Source.SEARCH_FAVORITES -> Request.SearchFavorites
                else                    -> Request.Search
            }
        )
    }
        .flatMapLatest { it }
        .distinctUntilChanged()

    val pageState: StateFlow<SearchPageState> = combine(
        _searchInput.debounce { 400 },
        installed,
        productsSource,
    ) { input, installedMap, products ->
        val filtered = products
            .matchSearchQuery(input.query)
            .map { it.toItem(installedMap[it.product.packageName]) }

        SearchPageState(
            isInitialized = true,
            sortFilter = input.sortFilter,
            query = input.query,
            source = input.source,
            installedMap = installedMap,
            filteredProducts = filtered,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = SearchPageState()
    )

    val isLoading: StateFlow<Boolean> = combine(
        _searchInput,
        pageState,
    ) { input, state ->
        input.query != state.query ||
        input.source != state.source ||
        input.sortFilter != state.sortFilter
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = false
    )

    val searchHistory: StateFlow<Set<String>>
        field = MutableStateFlow(loadHistory())

    fun setSortFilter(value: String) = _searchInput.update { it.copy(sortFilter = value) }

    fun setSearchQuery(value: String) = _searchInput.update { it.copy(query = value) }

    fun setSearchSource(newSource: Source) = _searchInput.update { it.copy(source = newSource) }

    fun submitSearchQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            val current = searchHistory.value.toMutableList()
            current.remove(query)
            current.add(0, query)
            val trimmed = current.take(MAX_HISTORY_SIZE).toSet()
            searchHistory.update { trimmed }
            saveHistory(trimmed)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            searchHistory.update { emptySet() }
            saveHistory(emptySet())
        }
    }

    private fun loadHistory(): Set<String> =
        Preferences[Preferences.Key.SearchHistory]

    private fun saveHistory(history: Set<String>) {
        Preferences[Preferences.Key.SearchHistory] = history
    }

    private data class SearchInput(
        val sortFilter: String = "",
        val query: String = "",
        val source: Source = Source.SEARCH,
    )

    companion object {
        private const val TAG = "SearchVM"
        private const val MAX_HISTORY_SIZE = 50
    }
}

data class SearchPageState(
    val isInitialized: Boolean = false,
    val sortFilter: String = "",
    val query: String = "",
    val source: Source = Source.SEARCH,
    val installedMap: Map<String, Installed> = emptyMap(),
    val filteredProducts: List<ProductItem> = emptyList(),
)