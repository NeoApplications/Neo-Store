package com.machiav3lli.fdroid.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO migrate sheets to Compose
// TODO migrate fields to Flows
class RepositorySheetVM(val db: DatabaseX, val repositoryId: Long) : ViewModel() {

    val repo: MediatorLiveData<Repository> = MediatorLiveData()
    val appsCount: MediatorLiveData<Long> = MediatorLiveData()

    init {
        repo.addSource(db.repositoryDao.getLive(repositoryId), repo::setValue)
        appsCount.addSource(db.productDao.countForRepositoryLive(repositoryId), appsCount::setValue)
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

    class Factory(val db: DatabaseX, val repositoryId: Long) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RepositorySheetVM::class.java)) {
                return RepositorySheetVM(db, repositoryId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
