package com.machiav3lli.fdroid.viewmodels

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.database.entity.Repository.Companion.newRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.parcelize.Parcelize

class PrefsVM(
    installedRepo: InstalledRepository,
    private val reposRepo: RepositoriesRepository,
    private val extrasRepo: ExtrasRepository,
) : ViewModel() {
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    val repositories = reposRepo.getAll()
        .stateIn(
            ioScope,
            SharingStarted.Lazily,
            emptyList()
        )

    private val _reposSearchQuery = MutableStateFlow("")
    val reposSearchQuery: StateFlow<String> = _reposSearchQuery

    val filteredRepositories = repositories.combine(reposSearchQuery) { repos, query ->
        repos.filter {
            "${it.address} ${it.name} ${it.description}".contains(query, ignoreCase = true)
        }
    }
        .stateIn(
            ioScope,
            SharingStarted.Lazily,
            emptyList()
        )

    val installed = installedRepo.getAll().map {
        it.associateBy(Installed::packageName)
    }

    val extras = extrasRepo.getAll().stateIn(
        ioScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val intentAddress = MutableStateFlow("")
    val address = intentAddress as StateFlow<String>
    private val intentFingerprint = MutableStateFlow("")
    val fingerprint = intentFingerprint as StateFlow<String>

    fun setSearchQuery(value: String) {
        ioScope.launch { _reposSearchQuery.update { value } }
    }

    fun setIntent(address: String?, fingerprint: String?) {
        viewModelScope.launch {
            intentAddress.update { address ?: "" }
            intentFingerprint.update { fingerprint ?: "" }
        }
    }

    suspend fun addNewRepository(address: String = "", fingerprint: String = ""): Long =
        reposRepo.insertReturn(
            newRepository(
                fallbackName = "new repository",
                address = address,
                fingerprint = fingerprint
            )
        )

    fun updateRepo(newValue: Repository?) {
        newValue?.let {
            viewModelScope.launch {
                reposRepo.upsert(it)
            }
        }
    }

    fun insertExtras(vararg items: Extras) {
        viewModelScope.launch {
            extrasRepo.upsert(*items)
        }
    }

    fun insertRepos(vararg newValue: Repository) {
        newValue.let {
            viewModelScope.launch {
                reposRepo.insertOrUpdate(*newValue)
            }
        }
    }
}

@Parcelize
data class SheetNavigationData(
    val repositoryId: Long = 0L,
    val editMode: Boolean = false,
) : Parcelable