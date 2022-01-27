package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.database.entity.Repository.Companion.newRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepositoriesViewModelX(val db: DatabaseX) : ViewModel() {

    val repositories = MediatorLiveData<List<Repository>>()

    init {
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
    }

    fun addRepository() {
        viewModelScope.launch {
            addNewRepository()
        }
    }

    private suspend fun addNewRepository() {
        withContext(Dispatchers.IO) { db.repositoryDao.insert(newRepository()) }
    }

    class Factory(val db: DatabaseX) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RepositoriesViewModelX::class.java)) {
                return RepositoriesViewModelX(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
