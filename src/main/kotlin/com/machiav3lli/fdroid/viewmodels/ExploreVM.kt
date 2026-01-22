package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.entity.Source
import com.machiav3lli.fdroid.data.entity.TopDownloadType
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.utils.extension.Quadruple
import com.machiav3lli.fdroid.utils.extension.Quintuple
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class ExploreVM(
    private val productsRepo: ProductsRepository,
    extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
) : ViewModel() {
    private val sortFilter = MutableStateFlow("")
    private val source = MutableStateFlow(Source.NONE)
    private val topAppType = MutableStateFlow(TopDownloadType.TOTAL_RECENT)

    private val installed = installedRepo.getMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyMap()
        )

    private val topApps = topAppType.flatMapLatest {
        when (it) {
            TopDownloadType.TOTAL_ALLTIME -> productsRepo.getAllTimeTopApps()
            TopDownloadType.TOTAL_RECENT  -> productsRepo.getRecentTopApps(it.key, 3)
            else                          -> productsRepo.getRecentTopApps(it.key, 1)
        }
    }

    val categoryProductsState: StateFlow<CategoryProductsState> = combine(
        sortFilter,
        combine(
            productsRepo.getAllCategories(),
            productsRepo.getAllCategoryDetails(),
        ) { cats, catDetails ->
            cats.map { cat ->
                catDetails.find { it.name == cat }
                    ?: CategoryDetails(cat, cat)
            }
        },
        source,
        installed,
        extrasRepo.getAll(),
    ) { sortFilter, categories, src, installed, extras ->
        Quintuple(
            sortFilter,
            categories,
            when (src) {
                Source.AVAILABLE -> Request.All
                Source.FAVORITES -> Request.Favorites
                else             -> Request.None
            },
            installed,
            extras,
        )
    }.flatMapLatest { (sortFilter, cats, req, installed, _) ->
        productsRepo.getProducts(req).map { products ->
            Quadruple(sortFilter, installed, products, cats)
        }
    }.map { (sortFilter, installed, products, categories) ->
        CategoryProductsState(
            sortFilter = sortFilter,
            categories = categories,
            items = products.map { it.toItem(installed[it.product.packageName]) },
            installedMap = installed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = CategoryProductsState()
    )

    val topProductsState: StateFlow<TopProductsState> = combine(
        topAppType,
        productsRepo.getDownloadStatsNotEmpty(),
        topApps.flatMapLatest { tops ->
            val packageToIndex = tops.mapIndexed { index, top ->
                top.packageName to index
            }.toMap()

            productsRepo.getSpecificProducts(tops.map { it.packageName }.toSet())
                .map { products ->
                    products.sortedBy { packageToIndex[it.product.packageName] ?: Int.MAX_VALUE }
                }
        },
        installed,
    ) { topAppType, statsNotEmpty, topDownloaded, installed ->
        TopProductsState(
            statsNotEmpty = statsNotEmpty,
            topAppType = topAppType,
            items = topDownloaded.map { it.toItem(installed[it.product.packageName]) },
            installedMap = installed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = TopProductsState()
    )

    fun setSortFilter(value: String) = sortFilter.update { value }

    fun setTopAppsType(type: TopDownloadType) = topAppType.update { type }

    fun setExploreSource(newSource: Source) = source.update { newSource }

    companion object {
        private const val TAG = "ExploreVM"
    }
}

data class CategoryProductsState(
    val sortFilter: String = "",
    val categories: List<CategoryDetails> = emptyList(),
    val items: List<ProductItem> = emptyList(),
    val installedMap: Map<String, Installed> = emptyMap(),
)

data class TopProductsState(
    val statsNotEmpty: Boolean = false,
    val topAppType: TopDownloadType = TopDownloadType.TOTAL_RECENT,
    val items: List<ProductItem> = emptyList(),
    val installedMap: Map<String, Installed> = emptyMap(),
)