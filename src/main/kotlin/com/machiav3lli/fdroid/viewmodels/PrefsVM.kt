package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.database.entity.Repository.Companion.newRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrefsVM(val db: DatabaseX) : ViewModel() {

    private val _showSheet = MutableSharedFlow<SheetNavigationData?>()
    val showSheet: SharedFlow<SheetNavigationData?> = _showSheet

    private val _repositories = MutableStateFlow<List<Repository>>(emptyList())
    val repositories = _repositories.asStateFlow()

    val extras = db.extrasDao.allFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        emptyList()
    )

    private val intentAddress = MutableStateFlow("")
    val address = intentAddress as StateFlow<String>
    private val intentFingerprint = MutableStateFlow("")
    val fingerprint = intentFingerprint as StateFlow<String>

    init {
        viewModelScope.launch(Dispatchers.IO) {
            db.repositoryDao.getAllRepositories().collectLatest {
                _repositories.emit(it)
            }
        }
    }

    fun showRepositorySheet(
        repositoryId: Long = 0L,
        editMode: Boolean = false,
        address: String = "",
        fingerprint: String = "",
        addNew: Boolean = false,
    ) {
        viewModelScope.launch {
            _showSheet.emit(
                when {
                    addNew && (address.isEmpty() || repositories.value.none { it.address == address }) -> {
                        SheetNavigationData(addNewRepository(address, fingerprint), editMode)
                    }

                    !addNew                                                                            -> {
                        SheetNavigationData(repositoryId, editMode)
                    }

                    else                                                                               -> {
                        null
                    }
                }
            )
        }
    }

    fun closeRepositorySheet() {
        viewModelScope.launch {
            _showSheet.emit(null)
        }
    }

    fun setIntent(address: String?, fingerprint: String?) {
        viewModelScope.launch {
            intentAddress.emit(address ?: "")
            intentFingerprint.emit(fingerprint ?: "")
        }
    }

    private suspend fun addNewRepository(address: String = "", fingerprint: String = ""): Long =
        withContext(Dispatchers.IO) {
            db.repositoryDao.insert(
                newRepository(
                    fallbackName = "new repository",
                    address = address,
                    fingerprint = fingerprint
                )
            )
            db.repositoryDao.latestAddedId()
        }

    fun updateRepo(newValue: Repository?) {
        newValue?.let {
            viewModelScope.launch {
                update(it)
            }
        }
    }

    private suspend fun update(newValue: Repository) {
        withContext(Dispatchers.IO) {
            db.repositoryDao.put(newValue)
        }
    }

    fun insertExtras(vararg items: Extras) {
        viewModelScope.launch {
            insert(*items)
        }
    }

    private suspend fun insert(vararg items: Extras) {
        withContext(Dispatchers.IO) {
            db.extrasDao.insertReplace(*items)
        }
    }

    fun insertRepos(vararg newValue: Repository) {
        newValue.let {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    db.repositoryDao.insertOrUpdate(*newValue)
                }
            }
        }
    }

    class Factory(private val db: DatabaseX) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PrefsVM::class.java)) {
                return PrefsVM(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class SheetNavigationData(
    val repositoryId: Long = 0L,
    val editMode: Boolean = false,
)