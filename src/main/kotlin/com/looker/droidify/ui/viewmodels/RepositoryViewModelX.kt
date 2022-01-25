package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Repository

class RepositoryViewModelX(val db: DatabaseX, val repositoryId: Long) : ViewModel() {

    val repo = MediatorLiveData<Repository>()
    val appsCount = MediatorLiveData<Long>()

    init {
        repo.addSource(db.repositoryDao.getLive(repositoryId), repo::setValue)
        appsCount.addSource(db.productDao.countForRepositoryLive(repositoryId), appsCount::setValue)
    }

    class Factory(val db: DatabaseX, val repositoryId: Long) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RepositoryViewModelX::class.java)) {
                return RepositoryViewModelX(db, repositoryId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
