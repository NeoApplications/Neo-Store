package com.machiav3lli.fdroid.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class LatestVM(
    private val productsRepo: ProductsRepository,
    extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
) : ViewModel() {
    val sortFilter: StateFlow<String>
        private field = MutableStateFlow("")

    val favorites = extrasRepo.getAllFavorites().distinctUntilChanged()

    val installed = installedRepo.getAll().map {
        it.associateBy(Installed::packageName).apply {
            Log.d(TAG, "Installed list size: ${this.size}")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = emptyMap()
    )

    val updatedProducts: StateFlow<List<ProductItem>> = combine(
        sortFilter,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _, _ -> productsRepo.getProducts(Request.Updated) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }.apply {
                Log.d(TAG, "Updated products list size: ${this.size}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    val newProducts: StateFlow<List<ProductItem>> = combine(
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _ -> productsRepo.getProducts(Request.New) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }.apply {
                Log.d(TAG, "New products list size: ${this.size}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    fun setSortFilter(value: String) = viewModelScope.launch {
        sortFilter.update { value }
    }

    companion object {
        private const val TAG = "LatestVM"
    }
}