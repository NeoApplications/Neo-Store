package com.looker.droidify.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.database.entity.Installed
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.entity.ActionState
import com.looker.droidify.entity.DownloadState

class AppViewModelX(val db: DatabaseX, val packageName: String) : ViewModel() {

    val products = MediatorLiveData<List<Product>>()
    val repositories = MediatorLiveData<List<Repository>>()
    val installedItem = MediatorLiveData<Installed?>()
    val _productRepos = MutableLiveData<List<Pair<Product, Repository>>>()
    var productRepos: List<Pair<Product, Repository>>
        get() = _productRepos.value ?: emptyList()
        set(value) {
            _productRepos.value = value
        }
    val downloadState = MutableLiveData<DownloadState>()
    val mainAction = MutableLiveData<ActionState>()
    val actions = MutableLiveData<Set<ActionState>>()
    val secondaryAction = MutableLiveData<ActionState>()

    init {
        products.addSource(db.productDao.getLive(packageName)) { products.setValue(it.filterNotNull()) }
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
        installedItem.addSource(db.installedDao.getLive(packageName), installedItem::setValue)
    }

    class Factory(val db: DatabaseX, val packageName: String) : ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModelX::class.java)) {
                return AppViewModelX(db, packageName) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
