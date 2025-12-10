package com.machiav3lli.fdroid.viewmodels

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.database.entity.Repository.Companion.newRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class PrefsVM(
    installedRepo: InstalledRepository,
    private val reposRepo: RepositoriesRepository,
    private val extrasRepo: ExtrasRepository,
) : ViewModel() {
    private val addressFingerprint = MutableStateFlow(Pair("", ""))
    private val reposSearchQuery = MutableStateFlow("")

    private val repositories = reposRepo.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            initialValue = emptyList()
        )

    private val installed = installedRepo.getMap()
        .distinctUntilChanged()

    private val extras = extrasRepo.getAll().distinctUntilChanged()

    val reposState: StateFlow<ReposPageState> = combine(
        repositories,
        reposSearchQuery,
        addressFingerprint,
    ) { repos, query, auth ->
        val (enabledRepos, disabledRepo) = repos.filter {
            "${it.address} ${it.name} ${it.description}".contains(query, ignoreCase = true)
        }.partition { it.enabled }

        ReposPageState(
            enabledRepos = enabledRepos,
            disabledRepo = disabledRepo,
            query = query,
            auth = auth,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = ReposPageState()
    )

    val otherPrefsState: StateFlow<OtherPrefsState> = combine(
        repositories,
        extras,
        installed,
    ) { repos, extras, installed ->
        OtherPrefsState(
            repos = repos,
            extras = extras,
            installedMap = installed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = OtherPrefsState()
    )

    fun setSearchQuery(value: String) = reposSearchQuery.update { value }

    fun setIntent(address: String?, fingerprint: String?) = addressFingerprint.update {
        Pair(address ?: "", fingerprint ?: "")
    }

    suspend fun isDuplicateAddress(address: String): Boolean =
        address.isNotEmpty() && reposRepo.isDuplicateAddress(address)

    suspend fun addNewRepository(address: String = "", fingerprint: String = ""): Long {
        return reposRepo.insertReturn(
            newRepository(
                fallbackName = "new repository",
                address = address,
                fingerprint = fingerprint
            )
        )
    }

    fun insertExtras(vararg items: Extras) {
        viewModelScope.launch {
            extrasRepo.upsert(*items)
        }
    }

    fun insertRepos(vararg newValue: Repository) {
        viewModelScope.launch {
            reposRepo.insertOrUpdate(*newValue)
        }
    }
}

@Parcelize
data class SheetNavigationData(
    val repositoryId: Long = 0L,
    val editMode: Boolean = false,
) : Parcelable


data class ReposPageState(
    val enabledRepos: List<Repository> = emptyList(),
    val disabledRepo: List<Repository> = emptyList(),
    val query: String = "",
    val auth: Pair<String, String> = Pair("", ""),
)

data class OtherPrefsState(
    val repos: List<Repository> = emptyList(),
    val extras: List<Extras> = emptyList(),
    val installedMap: Map<String, Installed> = emptyMap(),
)