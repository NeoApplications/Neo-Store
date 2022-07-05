package com.machiav3lli.fdroid.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.dao.RepositoryDao
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.database.entity.Repository.Companion.newRepository
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.SyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepositoriesViewModelX(val repositoryDao: RepositoryDao) : ViewModel() {

    val syncConnection = Connection(SyncService::class.java)

    private val _showSheet = MutableSharedFlow<SheetNavigationData>()
    val showSheet: SharedFlow<SheetNavigationData> = _showSheet

    private val _repositories = MutableStateFlow<List<Repository>>(emptyList())
    val repositories = _repositories.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryDao.getAllRepositories().collectLatest {
                _repositories.emit(it)
            }
        }
    }

    fun bindConnection(context: Context) {
        viewModelScope.launch { syncConnection.bind(context) }
    }

    fun showRepositorySheet(repositoryId: Long = 0L, editMode: Boolean = false) {
        viewModelScope.launch {
            _showSheet.emit(
                if (editMode) {
                    SheetNavigationData(repositoryId, editMode)
                } else {
                    SheetNavigationData(addNewRepository(), editMode)
                }
            )
        }
    }

    fun toggleRepository(repository: Repository, isEnabled: Boolean) {
        viewModelScope.launch {
            syncConnection.binder?.setEnabled(repository, isEnabled)
        }
    }

    private suspend fun addNewRepository(): Long = withContext(Dispatchers.IO) {
        repositoryDao.insert(newRepository())
        repositoryDao.latestAddedId()
    }

    class Factory(private val repoDao: RepositoryDao) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RepositoriesViewModelX::class.java)) {
                return RepositoriesViewModelX(repoDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class SheetNavigationData(
    val repositoryId: Long = 0L,
    val editMode: Boolean = false
)