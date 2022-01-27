package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Repository

class RepositoriesViewModelX(val db: DatabaseX) : ViewModel() {

    val repositories = MediatorLiveData<List<Repository>>()

    init {
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
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
