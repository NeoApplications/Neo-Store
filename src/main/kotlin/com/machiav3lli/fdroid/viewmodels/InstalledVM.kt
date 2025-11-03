package com.machiav3lli.fdroid.viewmodels

import android.util.Log
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
import kotlinx.coroutines.flow.map
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
    private val sortFilter: StateFlow<String>
        private field = MutableStateFlow("")

    private val installed = installedRepo.getAll().map {
        it.associateBy(Installed::packageName).apply {
            Log.d(TAG, "Installed list size: ${this.size}")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = emptyMap()
    )

    private val installedProducts: StateFlow<List<ProductItem>> = combine(
        sortFilter,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _, _ -> productsRepo.getProducts(Request.Installed) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }.apply {
                Log.d(TAG, "Installed products list size: ${this.size}")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    private val updateProducts = combine(
        installed,
        extrasRepo.getAll(),
    ) { _, _ -> productsRepo.getProducts(Request.Updates) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }.apply {
                Log.d(TAG, "Update products list size: ${this.size}")
            }
        }

    private val downloaded = downloadedRepo.getAllFlow()
        .debounce(250L)

    val sortedDownloads = downloaded
        .mapLatest { it.sortedByDescending { it.changed / 20_000L } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    val installedPageState: StateFlow<InstalledPageState> = combine(
        installed,
        installedProducts,
        updateProducts,
        downloaded,
        sortFilter,
    ) { installed, installedProducts, updateProducts, downloaded, sortFilter ->
        val sortedActiveDownloads = downloaded
            .filter { it.state is DownloadState.Downloading && it.changed + 600_000L > System.currentTimeMillis() }
            .sortedBy { it.state.name }
        InstalledPageState(
            installedMap = installed,
            installedProducts = installedProducts,
            updates = updateProducts,
            activeDownloads = sortedActiveDownloads,
            updatesAvailable = updateProducts.isNotEmpty(),
            isDownloading = sortedActiveDownloads.isNotEmpty(),
            sortFilter = sortFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = InstalledPageState()
    )

    fun setSortFilter(value: String) = viewModelScope.launch {
        sortFilter.update { value }
    }

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