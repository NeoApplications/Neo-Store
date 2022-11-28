package com.machiav3lli.fdroid.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.ExodusInfo
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.DownloadState
import com.machiav3lli.fdroid.utility.findSuggestedProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppViewModelX(val db: DatabaseX, val packageName: String, developer: String) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val products = db.productDao.getFlow(packageName).mapLatest { it.filterNotNull() }

    @OptIn(ExperimentalCoroutinesApi::class)
    val exodusInfo = db.exodusInfoDao.getFlow(packageName)
        .mapLatest { it.maxByOrNull(ExodusInfo::version_code) ?: ExodusInfo() }

    val trackers = exodusInfo.combine(db.trackerDao.allFlow) { a, b ->
        b.filter { it.key in a.trackers }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val repositories = db.repositoryDao.allFlow.mapLatest { it }

    @OptIn(ExperimentalCoroutinesApi::class)
    val installedItem = db.installedDao.getFlow(packageName).transformLatest {
        emit(it)
        updateActions()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        null
    )

    private val _productRepos = MutableStateFlow<List<Pair<Product, Repository>>>(emptyList())
    val productRepos: StateFlow<List<Pair<Product, Repository>>> = _productRepos
    fun updateProductRepos(repos: List<Pair<Product, Repository>>) {
        viewModelScope.launch {
            _productRepos.emit(repos)
        }
    }

    private val _downloadState = MutableStateFlow<DownloadState?>(null)
    val downloadState: StateFlow<DownloadState?> = _downloadState
    fun updateDownloadState(ds: DownloadState?) {
        viewModelScope.launch {
            _downloadState.emit(ds)
        }
    }

    private val _mainAction = MutableStateFlow<ActionState?>(null)
    val mainAction: StateFlow<ActionState?> = _mainAction

    private val _subActions = MutableStateFlow<Set<ActionState>>(emptySet())
    val subActions: StateFlow<Set<ActionState>> = _subActions

    @OptIn(ExperimentalCoroutinesApi::class)
    val extras = db.extrasDao.getFlow(packageName).transformLatest {
        emit(it)
        updateActions()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        Extras(packageName)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val authorProducts = db.productDao.getAuthorPackagesFlow(developer).transformLatest { prods ->
        if (developer.isNotEmpty()) emit(
            prods
                .filter { it.packageName != packageName }
                .groupBy { it.packageName }
                .map { it.value.maxByOrNull(Product::added)!! }
        )
    }

    fun updateActions() {
        viewModelScope.launch {
            updateUI()
        }
    }

    private suspend fun updateUI() {
        withContext(Dispatchers.IO) {
            val installed = installedItem.value
            val productRepos = productRepos.value
            val product = findSuggestedProduct(productRepos, installed) { it.first }?.first
            val compatible = product != null && product.selectedReleases.firstOrNull()
                .let { it != null && it.incompatibilities.isEmpty() }
            val canInstall =
                product != null && installed == null && compatible && _downloadState.value == null
            val canUpdate =
                product != null && compatible && product.canUpdate(installed) &&
                        !shouldIgnore(product.versionCode) && _downloadState.value == null
            val canUninstall = product != null && installed != null && !installed.isSystem
            val canLaunch = product != null &&
                    installed != null && installed.launcherActivities.isNotEmpty()
            val canShare = product != null &&
                    productRepos[0].second.name in setOf("F-Droid", "IzzyOnDroid F-Droid Repo")
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
                synchronized(actions) { _subActions.value = actions }

                if (_downloadState.value != null && _mainAction.value?.textId != _downloadState.value?.textId)
                    _downloadState.value?.let {
                        _mainAction.value = ActionState.Cancel(it.textId)
                    }
                else if (_downloadState.value == null)
                    _mainAction.value = primaryAction
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

    class Factory(val db: DatabaseX, val packageName: String, val developer: String) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppViewModelX::class.java)) {
                return AppViewModelX(db, packageName, developer) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
