package com.machiav3lli.fdroid.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Product
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.PrivacyData
import com.machiav3lli.fdroid.data.entity.toAntiFeature
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class AppSheetVM(
    private val db: DatabaseX,
    downloadedRepo: DownloadedRepository,
    private val productsRepo: ProductsRepository,
    private val extrasRepo: ExtrasRepository,
    private val installedRepo: InstalledRepository,
    private val privacyRepo: PrivacyRepository,
    reposRepo: RepositoriesRepository,
) : ViewModel() {
    private val cc = Dispatchers.IO

    private val packageName: MutableStateFlow<String> = MutableStateFlow("")

    val products = packageName
        .flatMapLatest { pn ->
            productsRepo.getProduct(pn)
        }

    private val developer = products.mapLatest { it.firstOrNull()?.author?.name ?: "" }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ""
    )

    val exodusInfo = packageName
        .flatMapLatest { pn ->
            privacyRepo.getExodusInfos(pn)
        }
        .mapLatest { it.maxByOrNull(ExodusInfo::version_code) }

    val trackers = combine(exodusInfo, privacyRepo.getAllTrackers()) { info, trackers ->
        trackers.filter { it.key in (info?.trackers ?: emptyList()) }
    }

    val repositories = reposRepo.getAll().distinctUntilChanged()

    val installedItem = packageName
        .flatMapLatest { packageName ->
            installedRepo.get(packageName)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    private val _repos = MutableStateFlow<List<Pair<Product, Repository>>>(emptyList())
    val repos: StateFlow<List<Pair<Product, Repository>>> = _repos

    // TODO rebase to internal only
    fun updateProductRepos(repos: List<Pair<Product, Repository>>) {
        viewModelScope.launch {
            _repos.update { repos }
        }
    }

    val privacyData = combine(installedItem, trackers, repos) { ins, trs, prs ->
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

    val downloadingState = downloadedRepo.getLatestFlow(packageName)
        .mapLatest { it?.state }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )


    val extras = packageName
        .flatMapLatest { pn ->
            extrasRepo.get(pn)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null
        )

    val authorProducts = combineTransform(
        packageName,
        developer,
        productsRepo.getAuthorList(developer.value),
    ) { pn, dev, prods ->
        if (dev.isNotEmpty()) emit(
            prods
                .filter { it.packageName != pn && it.author.name == dev }
                .groupBy { it.packageName }
                .map { it.value.maxByOrNull(Product::added)!! }
        )
    }

    private val actions: Flow<Pair<ActionState, Set<ActionState>>> =
        combine(repos, downloadingState, installedItem) { prs, ds, ins ->
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

    fun setIgnoredVersion(packageName: String, versionCode: Long) {
        viewModelScope.launch {
            extrasRepo.setIgnoredVersion(packageName, versionCode)
        }
    }

    fun setIgnoreUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.setIgnoreUpdates(packageName, setBoolean)
        }
    }

    fun setIgnoreVulns(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.setIgnoreVulns(packageName, setBoolean)
        }
    }

    fun setAllowUnstableUpdates(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.upsertExtra(packageName) {
                if (it != null) updateAllowUnstable(packageName, setBoolean)
                else insert(Extras(packageName, allowUnstable = setBoolean))
                val features = NeoApp.context.packageManager.systemAvailableFeatures
                    .asSequence().map { feat -> feat.name }
                    .toSet() + setOf("android.hardware.touchscreen")
                productsRepo.upsertProduct(
                    *repos.value.map { prodRepo ->
                        prodRepo.first.apply {
                            refreshReleases(
                                features,
                                setBoolean || Preferences[Preferences.Key.UpdateUnstable]
                            )
                            refreshVariables()
                        }
                    }.toTypedArray()
                )
            }
        }
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.setFavorite(packageName, setBoolean)
        }
    }
}
