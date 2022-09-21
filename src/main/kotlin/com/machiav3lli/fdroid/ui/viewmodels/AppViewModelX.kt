package com.machiav3lli.fdroid.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Installed
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.DownloadState
import com.machiav3lli.fdroid.utility.findSuggestedProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModelX(val db: DatabaseX, val packageName: String) : ViewModel() {

    val products: MediatorLiveData<List<Product>> = MediatorLiveData()
    val repositories: MediatorLiveData<List<Repository>> = MediatorLiveData()
    val installedItem: MediatorLiveData<Installed?> = MediatorLiveData()
    val _productRepos: MutableLiveData<List<Pair<Product, Repository>>> = MutableLiveData()
    var productRepos: List<Pair<Product, Repository>>
        get() = _productRepos.value ?: emptyList()
        set(value) {
            _productRepos.value = value
        }
    val downloadState: MutableLiveData<DownloadState> = MutableLiveData()
    val mainAction: MutableLiveData<ActionState> = MutableLiveData()
    val actions: MediatorLiveData<Set<ActionState>> = MediatorLiveData()
    val extras: MediatorLiveData<Extras> = MediatorLiveData()

    init {
        products.addSource(db.productDao.getLive(packageName)) { products.setValue(it.filterNotNull()) }
        repositories.addSource(db.repositoryDao.allLive, repositories::setValue)
        installedItem.addSource(db.installedDao.getLive(packageName), installedItem::setValue)
        extras.addSource(db.extrasDao.getLive(packageName), extras::setValue)
        actions.addSource(extras) { updateActions() }
    }

    fun updateActions() {
        viewModelScope.launch {
            updateUI()
        }
    }

    private suspend fun updateUI() {
        withContext(Dispatchers.IO) {
            val installed = installedItem.value
            val productRepos = productRepos
            val product = findSuggestedProduct(productRepos, installed) { it.first }?.first
            val compatible = product != null && product.selectedReleases.firstOrNull()
                .let { it != null && it.incompatibilities.isEmpty() }
            val canInstall =
                product != null && installed == null && compatible && downloadState.value == null
            val canUpdate =
                product != null && compatible && product.canUpdate(installed) &&
                        !shouldIgnore(product.versionCode) && downloadState.value == null
            val canUninstall = product != null && installed != null && !installed.isSystem
            val canLaunch =
                product != null && installed != null && installed.launcherActivities.isNotEmpty()
            val canShare = product != null && productRepos[0].second.name == "F-Droid"
            val bookmarked = extras.value?.favorite ?: false

            val actions = mutableSetOf<ActionState>()
            synchronized(actions) {
                if (canUpdate) actions += ActionState.Update
                else if (canInstall) actions += ActionState.Install
                if (canLaunch) actions += ActionState.Launch
                if (installed != null) actions += ActionState.Details
                if (canUninstall) actions += ActionState.Uninstall
                if (canShare) actions += ActionState.Share
                if (bookmarked) actions += ActionState.Bookmarked
                else actions += ActionState.Bookmark
            }
            val primaryAction = when {
                canUpdate -> ActionState.Update
                canLaunch -> ActionState.Launch
                canInstall -> ActionState.Install
                canShare -> ActionState.Share
                bookmarked -> ActionState.Bookmarked
                else -> ActionState.Bookmark
            }

            withContext(Dispatchers.Main) {
                synchronized(actions) { this@AppViewModelX.actions.value = actions }

                if (downloadState.value != null && mainAction.value?.textId != downloadState.value?.textId)
                    downloadState.value?.let {
                        mainAction.value = ActionState.Cancel(it.textId)
                    }
                else if (downloadState.value == null)
                    mainAction.value = primaryAction
                true
            }
        }
    }

    private fun shouldIgnore(appVersionCode: Long): Boolean =
        extras.value?.ignoredVersion == appVersionCode || extras.value?.ignoreUpdates == true

    fun setIgnoredVersion(packageName: String, versionCode: Long) {
        viewModelScope.launch {
            saveIgnoredVersion(packageName, versionCode)
        }
    }

    private suspend fun saveIgnoredVersion(packageName: String, versionCode: Long) {
        withContext(Dispatchers.IO) {
            val oldValue = db.extrasDao[packageName]
            if (oldValue != null) db.extrasDao
                .insertReplace(oldValue.copy(ignoredVersion = versionCode))
            else db.extrasDao
                .insertReplace(Extras(packageName, ignoredVersion = versionCode))
        }
    }


    fun setIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveIgnoreUpdates(packageName, setBoolean)
        }
    }

    private suspend fun saveIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        withContext(Dispatchers.IO) {
            val oldValue = db.extrasDao[packageName]
            if (oldValue != null) db.extrasDao
                .insertReplace(oldValue.copy(ignoreUpdates = setBoolean))
            else db.extrasDao
                .insertReplace(Extras(packageName, ignoreUpdates = setBoolean))
        }
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveFavorite(packageName, setBoolean)
        }
    }

    private suspend fun saveFavorite(packageName: String, setBoolean: Boolean) {
        withContext(Dispatchers.IO) {
            val oldValue = db.extrasDao[packageName]
            if (oldValue != null) db.extrasDao
                .insertReplace(oldValue.copy(favorite = setBoolean))
            else db.extrasDao
                .insertReplace(Extras(packageName, favorite = setBoolean))
        }
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
