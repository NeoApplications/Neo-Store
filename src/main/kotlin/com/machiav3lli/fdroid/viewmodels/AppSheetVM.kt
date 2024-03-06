package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.RealmRepo
import com.machiav3lli.fdroid.database.entity.Extras
import com.machiav3lli.fdroid.database.entity.Product
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.PrivacyData
import com.machiav3lli.fdroid.entity.toAntiFeature
import com.machiav3lli.fdroid.utility.findSuggestedProduct
import com.machiav3lli.fdroid.utility.generatePermissionGroups
import com.machiav3lli.fdroid.utility.refresh
import com.machiav3lli.fdroid.utility.toPrivacyNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(ExperimentalCoroutinesApi::class)
class AppSheetVM(val db: RealmRepo, val packageName: String) : ViewModel() {

    private val cc = Dispatchers.IO

    val products = db.productDao.getFlow(packageName).mapLatest { it.filterNotNull() }

    private val developer =
        products.mapLatest { it.firstOrNull()?.author?.name ?: "" }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            ""
        )

    val exodusInfo = db.exodusInfoDao.getFlow(packageName)

    val trackers = exodusInfo.combine(db.trackerDao.allFlow) { a, b ->
        b.filter { it.key in (a?.trackers ?: emptyList()) }
    }

    val repositories = db.repositoryDao.allFlow.mapLatest { it }

    val installedItem = db.installedDao.getFlow(packageName)
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    val productRepos = combine(products, repositories) { products, repos ->
        products.mapNotNull { app ->
            repos.firstOrNull { it.id == app.repositoryId }
                ?.let { Pair(app, it) }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        emptyList()
    )

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

    val privacyNote = privacyData.mapLatest {
        it.toPrivacyNote()
    }

    val downloadingState = db.downloadedDao.getLatestFlow(packageName)
        .mapLatest { it?.state }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )


    val extras = db.extrasDao.getFlow(packageName)
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            Extras(packageName)
        )

    val authorProducts = combineTransform(
        db.productDao.getAuthorPackagesFlow(developer.value),
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

    val mainAction: StateFlow<ActionState?> = actions.mapLatest { it.first }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

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
            val exstVal = db.extrasDao[packageName]
                ?: Extras(packageName)
            db.extrasDao
                .upsert(exstVal.apply { ignoredVersion = versionCode })
        }
    }


    fun setIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveIgnoreUpdates(packageName, setBoolean)
        }
    }

    private suspend fun saveIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val exstVal = db.extrasDao[packageName]
                ?: Extras(packageName)
            db.extrasDao
                .upsert(exstVal.apply { ignoreUpdates = setBoolean })
        }
    }

    fun setIgnoreVulns(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveIgnoreVulns(packageName, setBoolean)
        }
    }

    private suspend fun saveIgnoreVulns(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val exstVal = db.extrasDao[packageName]
                ?: Extras(packageName)
            db.extrasDao
                .upsert(exstVal.apply { ignoreVulns = setBoolean })
        }
    }

    fun setAllowUnstableUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveAllowUnstableUpdates(packageName, setBoolean)
        }
    }

    private suspend fun saveAllowUnstableUpdates(packageName: String, setBoolean: Boolean) {
        withContext(cc) {
            val exstVal = db.extrasDao[packageName]
                ?: Extras(packageName)
            db.extrasDao
                .upsert(exstVal.apply { allowUnstable = setBoolean })
            val features = MainApplication.context.packageManager.systemAvailableFeatures
                .asSequence().map { it.name }.toSet() + setOf("android.hardware.touchscreen")
            productRepos.value.forEach { (product, _) ->
                db.releaseDao.upsert(
                    *product.releases.toMutableList()
                        .apply {
                            refresh(
                                product,
                                features,
                                setBoolean || Preferences[Preferences.Key.UpdateUnstable]
                            )
                        }.toTypedArray()
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
            val exstVal = db.extrasDao[packageName]
                ?: Extras(packageName)
            db.extrasDao
                .upsert(exstVal.apply { favorite = setBoolean })
        }
    }

    class Factory(val db: RealmRepo, val packageName: String) :
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
