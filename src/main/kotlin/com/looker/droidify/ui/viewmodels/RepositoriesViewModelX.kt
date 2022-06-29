package com.looker.droidify.ui.viewmodels

import android.content.Context
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.looker.droidify.database.dao.RepositoryDao
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.database.entity.Repository.Companion.newRepository
import com.looker.droidify.service.Connection
import com.looker.droidify.service.SyncService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepositoriesViewModelX(val repositoryDao: RepositoryDao) : ViewModel() {

    val toLaunch: MediatorLiveData<Pair<Boolean, Long>?> = MediatorLiveData()

    val syncConnection = Connection(SyncService::class.java)

    private val _showSheet = MutableSharedFlow<Long>()
    val showSheet: SharedFlow<Long> = _showSheet

    private val _repositories = MutableStateFlow<List<Repository>>(emptyList())
    val repositories = _repositories.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            repositoryDao.getAllRepositories().collectLatest {
                _repositories.emit(it)
            }
        }
        toLaunch.value = null
    }

    fun bindConnection(context: Context) {
        viewModelScope.launch { syncConnection.bind(context) }
    }

    fun showRepositorySheet(repositoryId: Long) {
        viewModelScope.launch { _showSheet.emit(repositoryId) }
    }

    fun toggleRepository(repository: Repository, isEnabled: Boolean) {
        viewModelScope.launch {
            syncConnection.binder?.setEnabled(repository, isEnabled)
        }
    }

    fun addRepository() {
        viewModelScope.launch {
            toLaunch.value = Pair(true, addNewRepository())
        }
    }

    private suspend fun addNewRepository(): Long = withContext(Dispatchers.IO) {
        repositoryDao.insert(newRepository())
        repositoryDao.latestAddedId()
    }

    fun emptyToLaunch() {
        toLaunch.value = null
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
