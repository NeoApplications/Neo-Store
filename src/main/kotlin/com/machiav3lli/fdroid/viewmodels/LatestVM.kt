package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class LatestVM(
    private val productsRepo: ProductsRepository,
    extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
) : ViewModel() {
    private val sortFilter = MutableStateFlow("")

    private val installed = installedRepo.getMap()
        .distinctUntilChanged()

    private val newProducts = combine(
        sortFilter,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _, _ ->
        productsRepo.getProducts(Request.New)
    }
        .flatMapLatest { it }
        .distinctUntilChanged()

    private val updatedProducts = combine(
        sortFilter,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _, _ ->
        productsRepo.getProducts(Request.Updated)
    }
        .flatMapLatest { it }
        .distinctUntilChanged()

    val pageState: StateFlow<LatestPageState> = combine(
        sortFilter,
        installed,
        updatedProducts,
        newProducts,
    ) { sortFilter, installed, updated, new ->
        LatestPageState(
            sortFilter = sortFilter,
            installedMap = installed,
            updatedProducts = updated.map {
                it.toItem(installed[it.product.packageName])
            },
            newProducts = new.map {
                it.toItem(installed[it.product.packageName])
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = LatestPageState()
    )

    fun setSortFilter(value: String) = sortFilter.update { value }

    companion object {
        private const val TAG = "LatestVM"
    }
}

data class LatestPageState(
    val sortFilter: String = "",
    val installedMap: Map<String, Installed> = emptyMap(),
    val updatedProducts: List<ProductItem> = emptyList(),
    val newProducts: List<ProductItem> = emptyList(),
)