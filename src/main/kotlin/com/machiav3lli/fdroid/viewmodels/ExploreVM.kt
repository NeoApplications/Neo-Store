package com.machiav3lli.fdroid.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Licenses
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.entity.Source
import com.machiav3lli.fdroid.data.entity.TopDownloadType
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
class ExploreVM(
    private val productsRepo: ProductsRepository,
    extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
) : ViewModel() {
    val sortFilter: StateFlow<String>
        private field = MutableStateFlow("")
    val source: StateFlow<Source>
        private field = MutableStateFlow(Source.NONE)
    val topAppType: StateFlow<TopDownloadType>
        private field = MutableStateFlow(TopDownloadType.TOTAL_RECENT)

    val categories = combine(
        productsRepo.getAllCategories(),
        productsRepo.getAllCategoryDetails(),
    ) { cats, catDetails ->
        cats.map { cat ->
            catDetails.find { it.name == cat }
                ?: CategoryDetails(cat, cat)
        }
    }.distinctUntilChanged()

    val licenses = productsRepo.getAllLicenses().distinctUntilChanged().mapLatest {
        it.map(Licenses::licenses).flatten().distinct()
    }

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
            Source.AVAILABLE -> Request.All
            Source.FAVORITES -> Request.Favorites
            else             -> Request.None
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = Request.None
    )

    val products: StateFlow<List<ProductItem>> = combine(
        request,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { req, _, _ -> productsRepo.getProducts(req) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }.apply {
                Log.d(TAG, "Explore products list size: ${this.size}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    private val topApps = topAppType.flatMapLatest {
        when (it) {
            TopDownloadType.TOTAL_ALLTIME -> productsRepo.getAllTimeTopApps()
            TopDownloadType.TOTAL_RECENT  -> productsRepo.getRecentTopApps(it.key, 3)
            else                          -> productsRepo.getRecentTopApps(it.key, 1)
        }
    }

    val topDownloadedProducts = topApps.flatMapLatest { tops ->
        productsRepo.getSpecificProducts(tops.map { it.packageName }.toSet())
            .map {
                it.sortedBy { prd ->
                    tops.indexOfFirst { top ->
                        top.packageName == prd.product.packageName
                    }
                }
            }
    }.mapLatest { list ->
        list.map { it.toItem(installed.value[it.product.packageName]) }.apply {
            Log.d(TAG, "Top downloaded products list size: ${this.size}")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = emptyList()
    )

    fun setSortFilter(value: String) = viewModelScope.launch {
        sortFilter.update { value }
    }

    fun setTopAppsType(type: TopDownloadType) = viewModelScope.launch {
        topAppType.update { type }
    }

    fun setExploreSource(newSource: Source) = viewModelScope.launch {
        source.update { newSource }
    }

    companion object {
        private const val TAG = "ExploreVM"
    }
}