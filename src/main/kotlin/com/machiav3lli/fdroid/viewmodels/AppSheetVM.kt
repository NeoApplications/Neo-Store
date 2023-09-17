package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.entity.ExodusInfo
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.PrivacyData
import com.machiav3lli.fdroid.entity.toAntiFeature
import com.machiav3lli.fdroid.utility.findSuggestedProduct
import com.machiav3lli.fdroid.utility.generatePermissionGroups
import com.machiav3lli.fdroid.utility.toPrivacyNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSheetVM(val db: DatabaseX, val packageName: String) : ViewModel() {

    private val cc = Dispatchers.IO

    @OptIn(ExperimentalCoroutinesApi::class)
    val products = db.getProductDao().getFlow(packageName).mapLatest { it.filterNotNull() }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val developer = products.mapLatest { it.firstOrNull()?.author?.name ?: "" }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ""
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val exodusInfo = db.getExodusInfoDao().getFlow(packageName)
        .mapLatest { it.maxByOrNull(ExodusInfo::version_code) }

    val trackers = exodusInfo.combine(db.getTrackerDao().getAllFlow()) { a, b ->
        b.filter { it.key in (a?.trackers ?: emptyList()) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val repositories = db.getRepositoryDao().getAllFlow().mapLatest { it }

    val installedItem = db.getInstalledDao().getFlow(packageName)
        .stateIn(
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

    val privacyData = combine(installedItem, trackers, productRepos) { ins, trs, prs ->
        val suggestedProduct = findSuggestedProduct(prs, ins) { it.first }
        PrivacyData(
            permissions = suggestedProduct?.first?.displayRelease
                ?.generatePermissionGroups(MainApplication.mainActivity!!) ?: emptyMap(),
            trackers = trs,
            antiFeatures = suggestedProduct?.first?.antiFeatures?.mapNotNull { it.toAntiFeature() }
                ?: emptyList()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PrivacyData(emptyMap(), emptyList(), emptyList())
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val privacyNote = privacyData.mapLatest {
        it.toPrivacyNote()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val downloadingState = db.getDownloadedDao().getLatestFlow(packageName)
        .mapLatest { it?.state }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )


    val extras = db.getExtrasDao().getFlow(packageName)
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Extras(packageName)
        )

    val authorProducts = combineTransform(
        db.getProductDao().getAuthorPackagesFlow(developer.value),
        developer
    ) { prods, dev ->
        if (dev.isNotEmpty()) emit(
            prods
                .filter { it.packageName != packageName && it.author.name == dev }
                .groupBy { it.packageName }
                .map { it.value.maxByOrNull(Product::added)!! }
        )
    }

    private val actions: Flow<Pair<ActionState?, Set<ActionState>>> =
        combine(productRepos, downloadingState, installedItem, extras) { prs, ds, ins, ex ->
            val product = findSuggestedProduct(prs, ins) { it.first }?.first
            val compatible = product != null && product.selectedReleases.firstOrNull()
                .let { it != null && it.incompatibilities.isEmpty() }
            val canInstall =
                product != null && ins == null && compatible && ds?.isActive != true
            val canUpdate =
                product != null && compatible && product.canUpdate(ins) &&
                        !shouldIgnore(product.versionCode) && ds?.isActive != true
            val canUninstall = product != null && ins != null && !ins.isSystem
            val canLaunch = product != null &&
                    ins != null && ins.launcherActivities.isNotEmpty()
            val canShare = product != null &&
                    prs[0].second.name in setOf("F-Droid", "IzzyOnDroid F-Droid Repo")
            val bookmarked = ex?.favorite ?: false

            val actions = mutableSetOf<ActionState>()
            synchronized(actions) {
                if (canUpdate) actions += ActionState.Update
                else if (canInstall) actions += ActionState.Install
                if (canLaunch) actions += ActionState.Launch
                if (ins != null) actions += ActionState.Details
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

            val mA = if (ds != null && ds.isActive)
                ActionState.Cancel(ds.description)
            else primaryAction
            Pair(mA, actions)
        }.distinctUntilChanged { old, new ->
            val omA = old.first
            val nmA = new.first
            val matchCancel =
                !(omA is ActionState.Cancel && nmA is ActionState.Cancel && nmA.textId != omA.textId)
            old.second == new.second && matchCancel
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    val mainAction: StateFlow<ActionState?> = actions.mapLatest { it.first }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val subActions: StateFlow<Set<ActionState>> = actions.mapLatest { it.second }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptySet()
        )

    private fun shouldIgnore(appVersionCode: Long): Boolean =
        extras.value?.ignoredVersion == appVersionCode || extras.value?.ignoreUpdates == true

    fun setIgnoredVersion(packageName: String, versionCode: Long) {
        viewModelScope.launch {
            saveIgnoredVersion(packageName, versionCode)
        }
    }

    private suspend fun saveIgnoredVersion(packageName: String, versionCode: Long) {
        withContext(cc) {
            val oldValue = db.getExtrasDao()[packageName]
            if (oldValue != null) db.getExtrasDao()
                .insertReplace(oldValue.copy(ignoredVersion = versionCode))
            else db.getExtrasDao()
                .insertReplace(Extras(packageName, ignoredVersion = versionCode))
        }
    }


    fun setIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveIgnoreUpdates(packageName, setBoolean)
        }
    }

    private suspend fun saveIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val oldValue = db.getExtrasDao()[packageName]
            if (oldValue != null) db.getExtrasDao()
                .insertReplace(oldValue.copy(ignoreUpdates = setBoolean))
            else db.getExtrasDao()
                .insertReplace(Extras(packageName, ignoreUpdates = setBoolean))
        }
    }

    fun setIgnoreVulns(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveIgnoreVulns(packageName, setBoolean)
        }
    }

    private suspend fun saveIgnoreVulns(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val oldValue = db.getExtrasDao()[packageName]
            if (oldValue != null) db.getExtrasDao()
                .insertReplace(oldValue.copy(ignoreVulns = setBoolean))
            else db.getExtrasDao()
                .insertReplace(Extras(packageName, ignoreVulns = setBoolean))
        }
    }

    fun setAllowUnstableUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveAllowUnstableUpdates(packageName, setBoolean)
        }
    }

    private suspend fun saveAllowUnstableUpdates(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val oldValue = db.getExtrasDao()[packageName]
            if (oldValue != null) db.getExtrasDao()
                .insertReplace(oldValue.copy(allowUnstable = setBoolean))
            else db.getExtrasDao()
                .insertReplace(Extras(packageName, allowUnstable = setBoolean))
            val features = MainApplication.context.packageManager.systemAvailableFeatures
                .asSequence().map { it.name }.toSet() + setOf("android.hardware.touchscreen")
            productRepos.value.forEach {
                db.getProductDao().insertReplace(
                    it.first.apply {
                        refreshReleases(
                            features,
                            setBoolean || Preferences[Preferences.Key.UpdateUnstable]
                        )
                        refreshVariables()
                    }
                )
            }
        }
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveFavorite(packageName, setBoolean)
        }
    }

    private suspend fun saveFavorite(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val oldValue = db.getExtrasDao()[packageName]
            if (oldValue != null) db.getExtrasDao()
                .insertReplace(oldValue.copy(favorite = setBoolean))
            else db.getExtrasDao()
                .insertReplace(Extras(packageName, favorite = setBoolean))
        }
    }

    class Factory(val db: DatabaseX, val packageName: String) :
        ViewModelProvider.Factory {
        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppSheetVM::class.java)) {
                return AppSheetVM(db, packageName) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
