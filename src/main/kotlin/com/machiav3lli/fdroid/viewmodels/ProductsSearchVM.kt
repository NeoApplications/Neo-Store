package com.machiav3lli.fdroid.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.ProductSearchManager
import com.machiav3lli.fdroid.database.entity.ProductAS
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProductsSearchVM(
    private val searchManager: ProductSearchManager,
    val db: DatabaseX,
) : ViewModel() {

    var state by mutableStateOf(ProductSearchState())
        private set

    val d = db.getProductDao().getAllLicensesFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            searchManager.init()
            // TODO get products from DB and insert them
        }
    }

    fun onSearchQueryChange(query: String) {
        state = state.copy(searchQuery = query)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400L)
            val todos = searchManager.searchProducts(query)
            state = state.copy(todos = todos)
        }
    }


    override fun onCleared() {
        searchManager.closeSession()
        super.onCleared()
    }
}

data class ProductSearchState(
    val todos: List<ProductAS> = emptyList(),
    val searchQuery: String = ""
)