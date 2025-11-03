package com.machiav3lli.fdroid.viewmodels

import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.IconDetails
import com.machiav3lli.fdroid.data.database.entity.Licenses
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalCoroutinesApi::class,
    FlowPreview::class,
)
open class MainVM(
    private val extrasRepo: ExtrasRepository,
    productsRepo: ProductsRepository,
    reposRepo: RepositoriesRepository,
) : ViewModel() {
    val navigationState: StateFlow<Pair<ThreePaneScaffoldRole, String>>
        private field = MutableStateFlow(Pair(ListDetailPaneScaffoldRole.List, ""))

    val successfulSyncs = reposRepo.getLatestUpdates()

    val dataState: StateFlow<DataState> = combine(
        reposRepo.getAll().mapLatest { it.associateBy(Repository::id) },
        extrasRepo.getAllFavorites(),
        // TODO simplify
        productsRepo.getIconDetails().distinctUntilChanged()
            .mapLatest { it.associateBy(IconDetails::packageName) },
    ) { reposMap, favorites, iconDetails ->
        DataState(
            reposMap = reposMap,
            favorites = favorites,
            iconDetails = iconDetails,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
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
        },
        // TODO simplify
        reposRepo.getRepoAntiFeatures().map { afs ->
            val catsMap = afs.associateBy(AntiFeatureDetails::name)
            val enumMap = AntiFeature.entries.associateBy(AntiFeature::key)
            (catsMap.keys + enumMap.keys).map { name ->
                catsMap[name]?.let { Pair(it.name, it.label) } ?: Pair(name, "")
            }
        },
        // TODO simplify
        productsRepo.getAllLicenses().distinctUntilChanged().mapLatest {
            it.map(Licenses::licenses).flatten().distinct()
        },
    ) { enabledRepos, categories, antifeaturePairs, licenses ->
        SortFilterState(
            enabledRepos = enabledRepos,
            categories = categories,
            antifeaturePairs = antifeaturePairs,
            licenses = licenses,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        initialValue = SortFilterState(),
    )

    fun setNavigatorRole(role: ThreePaneScaffoldRole, packageName: String = "") {
        viewModelScope.launch { navigationState.update { Pair(role, packageName) } }
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.setFavorite(packageName, setBoolean)
        }
    }

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