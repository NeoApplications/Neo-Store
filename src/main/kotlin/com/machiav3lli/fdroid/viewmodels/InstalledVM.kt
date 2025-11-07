package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.entity.DownloadState
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)
class InstalledVM(
    private val downloadedRepo: DownloadedRepository,
    private val productsRepo: ProductsRepository,
    extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
) : ViewModel() {
    private val sortFilter = MutableStateFlow("")

    private val installed = installedRepo.getMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyMap()
        )

    val productsPair = combine(
        sortFilter,
        installed,
        extrasRepo.getAll(),
    ) { sortFilter, installed, extras ->
        Triple(sortFilter, installed, extras)
    }.flatMapLatest { (_, installed, _) ->
        combine(
            productsRepo.getProducts(Request.Installed),
            productsRepo.getProducts(Request.Updates),
        ) { installedProds, updatedProds ->
            ProductsState(
                installedProds.map { it.toItem(installed[it.product.packageName]) },
                updatedProds.map { it.toItem(installed[it.product.packageName]) },
            )
        }
    }.distinctUntilChanged()

    private val downloaded = downloadedRepo.getAllFlow()
        .debounce(250L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    val sortedDownloads = downloaded
        .mapLatest { it.sortedByDescending { it.changed / 20_000L } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    val installedPageState: StateFlow<InstalledPageState> = combine(
        installed,
        productsPair,
        downloaded,
        sortFilter,
    ) { installed, products, downloaded, sortFilter ->
        val sortedActiveDownloads = downloaded
            .filter { it.state is DownloadState.Downloading && it.changed + 300_000L > System.currentTimeMillis() }
            .sortedBy { it.state.name }
        InstalledPageState(
            installedMap = installed,
            installedProducts = products.installedProducts,
            updates = products.updatedProducts,
            activeDownloads = sortedActiveDownloads,
            updatesAvailable = products.updatedProducts.isNotEmpty(),
            isDownloading = sortedActiveDownloads.isNotEmpty(),
            sortFilter = sortFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = InstalledPageState()
    )

    fun setSortFilter(value: String) = sortFilter.update { value }

    fun eraseDownloaded(downloaded: Downloaded) = viewModelScope.launch {
        downloadedRepo.delete(downloaded)
        Cache.eraseDownload(NeoApp.context, downloaded.cacheFileName)
    }

    companion object {
        private const val TAG = "InstalledVM"
    }
}

data class InstalledPageState(
    val installedMap: Map<String, Installed> = emptyMap(),
    val installedProducts: List<ProductItem> = emptyList(),
    val updates: List<ProductItem> = emptyList(),
    val activeDownloads: List<Downloaded> = emptyList(),
    val updatesAvailable: Boolean = false,
    val isDownloading: Boolean = false,
    val sortFilter: String = ""
)

data class ProductsState(
    val installedProducts: List<ProductItem>,
    val updatedProducts: List<ProductItem>,
)