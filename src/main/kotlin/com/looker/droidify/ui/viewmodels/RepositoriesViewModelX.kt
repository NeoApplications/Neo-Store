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
    val toLaunch = MediatorLiveData<Pair<Boolean, Long>?>()

    init {
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
        toLaunch.value = null
    }

    fun addRepository() {
        viewModelScope.launch {
            toLaunch.value = Pair(true, addNewRepository())
        }
    }

    private suspend fun addNewRepository(): Long = withContext(Dispatchers.IO) {
        db.repositoryDao.insert(newRepository(address = "new.Repository"))
        db.repositoryDao.latestAddedId()
    }

    fun emptyToLaunch() {
        toLaunch.value = null
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
