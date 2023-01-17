package com.machiav3lli.fdroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RepositorySheetVM(val db: DatabaseX, val repositoryId: Long) : ViewModel() {

    val repo = db.repositoryDao.getFlow(repositoryId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )
    val appsCount = db.productDao.countForRepositoryFlow(repositoryId).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0
    )

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
