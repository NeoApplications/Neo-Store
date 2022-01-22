package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Repository

class RepositoriesViewModelX(val db: DatabaseX) : ViewModel() {

    val productsList = MediatorLiveData<List<Repository>>()

    init {
        productsList.addSource(db.repositoryDao.allLive, productsList::setValue)
    }

    class Factory(val db: DatabaseX) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RepositoriesViewModelX::class.java)) {
                return RepositoriesViewModelX(db) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
