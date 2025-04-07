package com.machiav3lli.fdroid.viewmodels

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.database.entity.Repository.Companion.newRepository
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
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

class PrefsVM(private val db: DatabaseX) : ViewModel() {
    private val cc = Dispatchers.IO
    private val ioScope = viewModelScope.plus(Dispatchers.IO)

    val repositories = db.getRepositoryDao().getAllFlow()
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

    val installed = db.getInstalledDao().getAllFlow().map {
        it.associateBy(Installed::packageName)
    }

    val extras = db.getExtrasDao().getAllFlow().stateIn(
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
        withContext(cc) {
            db.getRepositoryDao().insertReturn(
                newRepository(
                    fallbackName = "new repository",
                    address = address,
                    fingerprint = fingerprint
                )
            )
        }

    fun updateRepo(newValue: Repository?) {
        newValue?.let {
            viewModelScope.launch {
                update(it)
            }
        }
    }

    private suspend fun update(newValue: Repository) {
        withContext(cc) {
            db.getRepositoryDao().put(newValue)
        }
    }

    fun insertExtras(vararg items: Extras) {
        viewModelScope.launch {
            insert(*items)
        }
    }

    private suspend fun insert(vararg items: Extras) {
        withContext(cc) {
            db.getExtrasDao().upsert(*items)
        }
    }

    fun insertRepos(vararg newValue: Repository) {
        newValue.let {
            viewModelScope.launch {
                withContext(cc) {
                    db.getRepositoryDao().insertOrUpdate(*newValue)
                }
            }
        }
    }
}

@Parcelize
data class SheetNavigationData(
    val repositoryId: Long = 0L,
    val editMode: Boolean = false,
) : Parcelable