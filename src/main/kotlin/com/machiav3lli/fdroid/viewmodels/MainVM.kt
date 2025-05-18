package com.machiav3lli.fdroid.viewmodels

import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.Downloaded
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.IconDetails
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Licenses
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.Page
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.entity.Source
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.utils.matchSearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
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
import kotlinx.coroutines.plus

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)
open class MainVM(
    private val db: DatabaseX,
    private val downloadedRepo: DownloadedRepository,
    private val productsRepo: ProductsRepository,
    private val extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
    reposRepo: RepositoriesRepository,
) : ViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    private val _sortFilterLatest = MutableStateFlow("")
    val sortFilterLatest: StateFlow<String> = _sortFilterLatest
    private val _sortFilterExplore = MutableStateFlow("")
    val sortFilterExplore: StateFlow<String> = _sortFilterExplore
    private val _sortFilterSearch = MutableStateFlow("")
    val sortFilterSearch: StateFlow<String> = _sortFilterSearch
    private val _sortFilterInstalled = MutableStateFlow("")
    val sortFilterInstalled: StateFlow<String> = _sortFilterInstalled
    val navigationState: StateFlow<Pair<ThreePaneScaffoldRole, String>>
        private field = MutableStateFlow(Pair(ListDetailPaneScaffoldRole.List, ""))

    val querySearch: StateFlow<String>
        private field = MutableStateFlow("")
    private val _sourceExplore = MutableStateFlow(Source.NONE)
    private val sourceExplore: StateFlow<Source> = _sourceExplore
    private val _sourceSearch = MutableStateFlow(Source.SEARCH)
    val sourceSearch: StateFlow<Source> = _sourceSearch

    private fun request(source: Source): Request = when (source) {
        Source.AVAILABLE        -> Request.All
        Source.FAVORITES        -> Request.Favorites
        Source.SEARCH           -> Request.Search
        Source.SEARCH_INSTALLED -> Request.SearchInstalled
        Source.SEARCH_NEW       -> Request.SearchNew
        Source.INSTALLED        -> Request.Installed
        Source.UPDATES          -> Request.Updates
        Source.UPDATED          -> Request.Updated
        Source.NEW              -> Request.New
        Source.NONE             -> Request.None
    }

    val favorites = extrasRepo.getAllFavorites().distinctUntilChanged()

    val repositories = reposRepo.getAll().distinctUntilChanged()

    val categories = combine(
        productsRepo.getAllCategories(),
        productsRepo.getAllCategoryDetails(),
    ) { cats, catDetails ->
        cats.map { cat ->
            catDetails.find { it.name == cat }
                ?: CategoryDetails(cat, cat)
        }
    }
        .distinctUntilChanged()

    val antifeaturePairs = reposRepo.getRepoAntiFeatures().map { afs ->
        val catsMap = afs.associateBy(AntiFeatureDetails::name)
        val enumMap = AntiFeature.entries.associateBy { it.key }
        (catsMap.keys + enumMap.keys).map { name ->
            catsMap[name]?.let { Pair(it.name, it.label) } ?: Pair(name, "")
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList(),
    )

    val successfulSyncs = reposRepo.getLatestUpdates()

    val licenses = productsRepo.getAllLicenses().distinctUntilChanged().mapLatest {
        it.map(Licenses::licenses).flatten().distinct()
    }

    val iconDetails = productsRepo.getIconDetails().distinctUntilChanged().mapLatest {
        it.associateBy(IconDetails::packageName)
    }

    val installed = installedRepo.getAll().map {
        it.associateBy(Installed::packageName)
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyMap()
    )

    private var requestExplore: StateFlow<Request> = combine(
        sortFilterExplore,
        sourceExplore,
    ) { _, src ->
        request(src)
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = request(Source.NONE)
    )

    private var requestSearch: StateFlow<Request> = combine(
        sortFilterSearch,
        sourceSearch,
    ) { _, src ->
        request(src)
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = request(Source.SEARCH)
    )

    val productsExplore: Flow<List<ProductItem>> = combine(
        requestExplore,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { req, _, _ -> productsRepo.getProducts(req) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }
        }

    private val productsSearch: Flow<List<EmbeddedProduct>> = combine(
        requestSearch,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { req, _, _ -> productsRepo.getProducts(req) }
        .flatMapLatest { it }
        .distinctUntilChanged()

    val filteredProdsSearch: Flow<List<ProductItem>> = combine(
        productsSearch,
        querySearch.debounce(400),
        installed,
    ) { products, query, installed ->
        products.matchSearchQuery(query)
            .map { it.toItem(installed[it.product.packageName]) }
    }.stateIn(
        scope = ioScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val installedProdsInstalled: Flow<List<ProductItem>> = combine(
        sortFilterInstalled,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _, _ -> productsRepo.getProducts(Request.Installed) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }
        }

    val downloaded = downloadedRepo.getAllFlow()
        .debounce(250L)
        .distinctUntilChanged()

    val updatedProdsLatest: Flow<List<ProductItem>> = combine(
        sortFilterLatest,
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _, _ -> productsRepo.getProducts(Request.Updated) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }
        }

    val newProdsLatest: Flow<List<ProductItem>> = combine(
        installed,
        extrasRepo.getAll().distinctUntilChanged(),
    ) { _, _ -> productsRepo.getProducts(Request.New) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }
        }

    val updateProdsInstalled: Flow<List<ProductItem>> = combine(
        installed,
        extrasRepo.getAll(),
    ) { _, _ -> productsRepo.getProducts(Request.Updates) }
        .flatMapLatest { it }
        .distinctUntilChanged()
        .mapLatest { list ->
            list.map { it.toItem(installed.value[it.product.packageName]) }
        }

    fun setSortFilter(page: Page, value: String) = viewModelScope.launch {
        when (page) {
            Page.EXPLORE   -> _sortFilterExplore.update { value }
            Page.SEARCH    -> _sortFilterSearch.update { value }
            Page.INSTALLED -> _sortFilterInstalled.update { value }
            Page.LATEST    -> _sortFilterLatest.update { value }
        }
    }

    fun setSearchQuery(value: String) {
        viewModelScope.launch { querySearch.update { value } }
    }

    fun setNavigatorRole(role: ThreePaneScaffoldRole, packageName: String = "") {
        viewModelScope.launch { navigationState.update { Pair(role, packageName) } }
    }

    fun setExploreSource(ns: Source) {
        viewModelScope.launch { _sourceExplore.update { ns } }
    }

    fun setSearchSource(ns: Source) {
        viewModelScope.launch { _sourceSearch.update { ns } }
    }

    fun eraseDownloaded(downloaded: Downloaded) {
        viewModelScope.launch {
            downloadedRepo.delete(downloaded)
            Cache.eraseDownload(NeoApp.context, downloaded.cacheFileName)
        }
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.setFavorite(packageName, setBoolean)
        }
    }
}
