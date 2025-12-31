package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RepoPageVM(
    private val reposRepo: RepositoriesRepository
) : ViewModel() {
    private val repoId = MutableStateFlow(0L)

    private val repo = repoId
        .flatMapLatest { reposRepo.getById(it) }
        .distinctUntilChanged()

    private val products = repoId
        .flatMapLatest { reposRepo.getProducts(it) }
        .mapLatest { it.map(EmbeddedProduct::toItem) }

    val repoPageState: StateFlow<RepoPageState> = combine(
        repo,
        products,
    ) { repo, prods ->
        RepoPageState(
            repo = repo,
            products = prods.toPersistentList(),
            productsCount = prods.size,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        RepoPageState()
    )

    fun setRepo(id: Long) = repoId.update { id }

    fun updateRepo(newValue: Repository?) {
        newValue?.let {
            viewModelScope.launch {
                reposRepo.upsert(it)
            }
        }
    }
}


data class RepoPageState(
    val repo: Repository? = null,
    val products: PersistentList<ProductItem> = persistentListOf(),
    val productsCount: Int = 0,
)