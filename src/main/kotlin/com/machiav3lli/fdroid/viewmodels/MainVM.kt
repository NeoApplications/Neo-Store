package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.IconDetails
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.Request
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)
open class MainVM(
    private val extrasRepo: ExtrasRepository,
    private val productsRepo: ProductsRepository,
    reposRepo: RepositoriesRepository,
    installedRepo: InstalledRepository,
) : ViewModel() {
    val successfulSyncs = reposRepo.getLatestUpdates()

    private val installed = installedRepo.getMap()

    val updates = combine(
        productsRepo.getProducts(Request.Updates),
        installed,
        extrasRepo.getAll(),
    ) { prods, installed, _ ->
        prods.map { it.toItem(installed[it.product.packageName]) }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    val dataState: StateFlow<DataState> = combine(
        reposRepo.getAll().mapLatest { it.associateBy(Repository::id) },
        extrasRepo.getAllFavorites().distinctUntilChanged(),
        productsRepo.getIconDetailsMap().distinctUntilChanged(),
    ) { reposMap, favorites, iconDetails ->
        DataState(
            reposMap = reposMap,
            favorites = favorites,
            iconDetails = iconDetails,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = DataState(),
    )

    val sortFilterState: StateFlow<SortFilterState> = combine(
        reposRepo.getAllEnabled(),
        // TODO merge the two calls into one
        combine(
            productsRepo.getAllCategories(),
            productsRepo.getAllCategoryDetails(),
        ) { cats, catDetails ->
            cats.map { cat ->
                catDetails.find { it.name == cat }
                    ?: CategoryDetails(cat, cat)
            }
        }.distinctUntilChanged(),
        reposRepo.getRepoAntiFeaturePairs().distinctUntilChanged(),
        productsRepo.getAllLicensesDistinct().distinctUntilChanged(),
    ) { enabledRepos, categories, antifeaturePairs, licenses ->
        SortFilterState(
            enabledRepos = enabledRepos,
            categories = categories,
            antifeaturePairs = antifeaturePairs,
            licenses = licenses,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = SortFilterState(),
    )

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.setFavorite(packageName, setBoolean)
        }
    }

    suspend fun productExist(packageName: String): Boolean = productsRepo.productExists(packageName)

    companion object {
        const val TAG = "MainVM"
    }
}

data class DataState(
    val reposMap: Map<Long, Repository> = emptyMap(),
    val favorites: List<String> = emptyList(),
    val iconDetails: Map<String, IconDetails> = emptyMap(),
)

data class SortFilterState(
    val enabledRepos: List<Repository> = emptyList(),
    val categories: List<CategoryDetails> = emptyList(),
    val antifeaturePairs: List<Pair<String, String>> = emptyList(),
    val licenses: List<String> = emptyList(),
)