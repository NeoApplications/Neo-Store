package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.ProductIconDetails
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)
class InstalledVM(
    private val downloadedRepo: DownloadedRepository,
    productsRepo: ProductsRepository,
    extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
    reposRepo: RepositoriesRepository,
) : ViewModel() {
    private val sortFilter = MutableStateFlow("")

    private val installed = installedRepo.getMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyMap()
        )

    val installedProducts = combine(
        productsRepo.getProducts(Request.Installed),
        installed,
        sortFilter,
        extrasRepo.getAll(),
    ) { prods, installed, _, _ ->
        prods.map { it.toItem(installed[it.product.packageName]) }
    }

    private val sortedDownloads = downloadedRepo.getAllFlow()
        .map { it.sortedByDescending { it.changed / 10_000L } }
        .debounce(100L)

    val installedPageState: StateFlow<InstalledPageState> = combine(
        installed,
        installedProducts,
        sortFilter,
    ) { installed, products, sortFilter ->
        InstalledPageState(
            installedMap = installed,
            installedProducts = products,
            sortFilter = sortFilter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = InstalledPageState()
    )

    val downloadedPageState: StateFlow<DownloadedPageState> = combine(
        sortedDownloads,
        reposRepo.getAllMap().distinctUntilChanged(),
        productsRepo.getIconDetailsMap().distinctUntilChanged(),
    ) { downloads, reposMap, iconDetails ->
        DownloadedPageState(
            sortedDownloaded = downloads,
            reposMap = reposMap,
            iconDetails = iconDetails
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DownloadedPageState()
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
    val sortFilter: String = ""
)

data class DownloadedPageState(
    val sortedDownloaded: List<Downloaded> = emptyList(),
    val reposMap: Map<Long, Repository> = emptyMap(),
    val iconDetails: Map<String, ProductIconDetails> = emptyMap(),
)