package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.dao.ExtrasDao
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.PrivacyData
import com.machiav3lli.fdroid.data.entity.toAntiFeature
import com.machiav3lli.fdroid.utils.findSuggestedProduct
import com.machiav3lli.fdroid.utils.generatePermissionGroups
import com.machiav3lli.fdroid.utils.toPrivacyNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class AppSheetVM(val db: DatabaseX) : ViewModel() {
    private val cc = Dispatchers.IO

    private val packageName: MutableStateFlow<String> = MutableStateFlow("")

    val products = packageName
        .flatMapLatest { pn ->
            db.getProductDao().getFlow(pn)
        }

    private val developer = products.mapLatest { it.firstOrNull()?.author?.name ?: "" }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ""
    )

    val exodusInfo = packageName
        .flatMapLatest { pn ->
            db.getExodusInfoDao().getFlow(pn)
        }
        .mapLatest { it.maxByOrNull(ExodusInfo::version_code) }

    val trackers = exodusInfo.combine(db.getTrackerDao().getAllFlow()) { a, b ->
        b.filter { it.key in (a?.trackers ?: emptyList()) }
    }

    val repositories = db.getRepositoryDao().getAllFlow().mapLatest { it }

    val installedItem = packageName
        .flatMapLatest { packageName ->
            db.getInstalledDao().getFlow(packageName)
        }
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
                ?.generatePermissionGroups(NeoApp.context) ?: emptyMap(),
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

    val downloadingState = packageName
        .flatMapLatest { pn ->
            db.getDownloadedDao().getLatestFlow(pn)
        }
        .mapLatest { it?.state }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )


    val extras = packageName
        .flatMapLatest { pn ->
            db.getExtrasDao().getFlow(pn)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    val authorProducts = combineTransform(
        packageName,
        developer,
        db.getProductDao().getAuthorPackagesFlow(developer.value),
    ) { pn, dev, prods ->
        if (dev.isNotEmpty()) emit(
            prods
                .filter { it.packageName != pn && it.author.name == dev }
                .groupBy { it.packageName }
                .map { it.value.maxByOrNull(Product::added)!! }
        )
    }

    private val actions: Flow<Pair<ActionState, Set<ActionState>>> =
        combine(productRepos, downloadingState, installedItem) { prs, ds, ins ->
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

            val actions = mutableSetOf<ActionState>()
            synchronized(actions) {
                if (canUpdate) actions += ActionState.Update
                else if (canInstall && !Preferences[Preferences.Key.KidsMode]) actions += ActionState.Install
                if (canLaunch) actions += ActionState.Launch
                if (ins != null) actions += ActionState.Details
                if (canUninstall) actions += ActionState.Uninstall
                if (canShare) actions += ActionState.Share
            }
            val primaryAction = when {
                canUpdate -> ActionState.Update
                canLaunch -> ActionState.Launch
                canInstall && !Preferences[Preferences.Key.KidsMode] -> ActionState.Install
                canShare -> ActionState.Share
                else -> ActionState.NoAction
            }

            val mA = if (ds != null && ds.isActive)
                ds.toActionState() ?: primaryAction
            else primaryAction
            Pair(mA, actions.minus(mA))
        }.distinctUntilChanged()

    val mainAction: StateFlow<ActionState> = actions.map { it.first }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            ActionState.Bookmark
        )

    val subActions: StateFlow<Set<ActionState>> = actions.map { it.second }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            emptySet()
        )

    fun setApp(pn: String) {
        viewModelScope.launch { packageName.update { pn } }
    }

    private fun shouldIgnore(appVersionCode: Long): Boolean =
        extras.value?.ignoredVersion == appVersionCode || extras.value?.ignoreUpdates == true

    private suspend fun saveExtraField(
        packageName: String,
        updateFunc: ExtrasDao.(Extras?) -> Unit
    ) {
        withContext(cc) {
            db.getExtrasDao().upsertExtra(packageName, updateFunc)
        }
    }

    fun setIgnoredVersion(packageName: String, versionCode: Long) {
        viewModelScope.launch {
            saveExtraField(packageName) {
                if (it != null) updateIgnoredVersion(packageName, versionCode)
                else insert(Extras(packageName, ignoredVersion = versionCode))
            }
        }
    }

    fun setIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveExtraField(packageName) {
                if (it != null) updateIgnoreUpdates(packageName, setBoolean)
                else insert(Extras(packageName, ignoreUpdates = setBoolean))
            }
        }
    }

    fun setIgnoreVulns(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveExtraField(packageName) {
                if (it != null) updateIgnoreVulns(packageName, setBoolean)
                else insert(Extras(packageName, ignoreVulns = setBoolean))
            }
        }
    }

    fun setAllowUnstableUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveExtraField(packageName) {
                if (it != null) updateAllowUnstable(packageName, setBoolean)
                else insert(Extras(packageName, allowUnstable = setBoolean))
                val features = NeoApp.context.packageManager.systemAvailableFeatures
                    .asSequence().map { feat -> feat.name }
                    .toSet() + setOf("android.hardware.touchscreen")
                productRepos.value.forEach { prodRepo ->
                    db.getProductDao().upsert(
                        prodRepo.first.apply {
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
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            saveExtraField(packageName) {
                if (it != null) updateFavorite(packageName, setBoolean)
                else insert(Extras(packageName, favorite = setBoolean))
            }
        }
    }
}
