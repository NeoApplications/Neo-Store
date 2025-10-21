package com.machiav3lli.fdroid.viewmodels

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.RELEASE_STATE_INSTALLED
import com.machiav3lli.fdroid.RELEASE_STATE_NONE
import com.machiav3lli.fdroid.RELEASE_STATE_SUGGESTED
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.DownloadStats
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.MonthlyPackageSum
import com.machiav3lli.fdroid.data.database.entity.RBLog
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.PrivacyData
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.utils.extension.Quadruple
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.text.intToIsoDate
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.findSuggestedProduct
import com.machiav3lli.fdroid.utils.generatePermissionGroups
import com.machiav3lli.fdroid.utils.toPrivacyNote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppSheetVM(
    downloadedRepo: DownloadedRepository,
    private val productsRepo: ProductsRepository,
    private val extrasRepo: ExtrasRepository,
    private val installedRepo: InstalledRepository,
    private val privacyRepo: PrivacyRepository,
    reposRepo: RepositoriesRepository,
) : ViewModel() {
    private val packageName: MutableStateFlow<String> = MutableStateFlow("")

    val products = packageName
        .flatMapLatest { pn ->
            productsRepo.getProduct(pn)
        }

    private val developer = products.mapLatest {
        it.firstOrNull()?.product?.author?.let {
            it.name.nullIfEmpty() ?: it.email.nullIfEmpty() ?: it.web.nullIfEmpty()
        }.orEmpty()
    }.distinctUntilChanged()

    private val developerProds = developer.flatMapConcat {
        productsRepo.getAuthorList(it)
    }.distinctUntilChanged()

    val exodusInfo = packageName
        .flatMapLatest { pn ->
            privacyRepo.getExodusInfos(pn)
        }
        .mapLatest { it.maxByOrNull(ExodusInfo::version_code) }

    val trackers = combine(exodusInfo, privacyRepo.getAllTrackers()) { info, trackers ->
        trackers.filter { it.key in info?.trackers.orEmpty() }
    }

    val rbLogs = packageName
        .flatMapLatest { pn ->
            privacyRepo.getRBLogs(pn)
        }
        .mapLatest {
            it
                .groupBy(RBLog::hash)
                .mapValues { (_, rbDataList) -> rbDataList.maxByOrNull(RBLog::timestamp)!! }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptyMap()
        )

    val downloadStats = packageName
        .flatMapLatest { pn ->
            privacyRepo.getLatestDownloadStats(pn)
        }

    val downloadStatsInfo = packageName
        .flatMapLatest { pn ->
            combine(
                privacyRepo.getClientSumDownloadStats(pn),
                if (Android.sdk(Build.VERSION_CODES.R))
                    privacyRepo.getSumDownloadOrder(pn)
                else privacyRepo.getSumDownloadOrderLegacy(pn)
            ) { stats, order ->
                Triple(
                    stats.find { it.client == "_total" }?.totalCount ?: 0,
                    stats.filterNot { it.client == "_total" || it.client == "_unknown" }
                        .maxByOrNull { it.totalCount }?.client ?: "",
                    order
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            Triple(0L, "", 9999)
        )

    val downloadStatsMonthly = packageName
        .flatMapLatest { pn ->
            privacyRepo.getMonthlyDownloadStats(pn)
        }

    val downloadStatsMap = downloadStats.map {
        it.groupBy { stats -> stats.date.intToIsoDate() }
            .mapValues { entry ->
                entry.value.groupBy(DownloadStats::client)
                    .mapValues { stats -> stats.value.sumOf { stat -> stat.count } }
            }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptyMap()
        )

    val downloadStatsMonthlyMap = downloadStatsMonthly.map {
        it.groupBy { stats -> stats.yearMonth.intToIsoDate() }
            .mapValues { entry ->
                entry.value.groupBy(MonthlyPackageSum::client)
                    .mapValues { stats -> stats.value.sumOf { stat -> stat.totalCount } }
            }
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptyMap()
        )

    val repositories = reposRepo.getAll().distinctUntilChanged()

    val installedItem = packageName
        .flatMapLatest { packageName ->
            installedRepo.get(packageName)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            null
        )

    val productRepos = combine(
        products,
        repositories,
    ) { prods, repos ->
        val reposMap = repos.associateBy(Repository::id)
        prods.mapNotNull { product ->
            reposMap[product.product.repositoryId]?.let {
                Pair(product, it)
            }
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        emptyList()
    )

    val suggestedProductRepo = combine(productRepos, installedItem) { prodRepos, installed ->
        findSuggestedProduct(prodRepos, installed) { it.first }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        null
    )

    val releaseItems = combine(
        suggestedProductRepo,
        repositories,
        installedItem,
        rbLogs,
    ) { suggestedProductRepo, repos, installed, logs ->
        val includeIncompatible = Preferences[Preferences.Key.IncompatibleVersions]
        val reposMap = repos.associateBy(Repository::id)

        suggestedProductRepo?.first?.releases.orEmpty()
            .filter { includeIncompatible || it.incompatibilities.isEmpty() }
            .mapNotNull { rel -> reposMap[rel.repositoryId]?.let { Pair(rel, it) } }
            .map { (release, repository) ->
                Quadruple(
                    release,
                    repository,
                    when {
                        installed?.versionCode == release.versionCode && release.signature in installed.signatures
                             -> RELEASE_STATE_INSTALLED

                        release.incompatibilities.isEmpty() && release.selected && release.versionCode >= installed?.versionCode ?: 0
                                && (installed?.signatures?.contains(release.signature) ?: true || Preferences[Preferences.Key.DisableSignatureCheck])
                             -> RELEASE_STATE_SUGGESTED

                        else -> RELEASE_STATE_NONE
                    },
                    logs[release.hash],
                )
            }
            .sortedByDescending { it.first.versionCode }
            .toList()
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            emptyList(),
        )

    private val antifeatureDetails = combine(
        suggestedProductRepo,
        reposRepo.getRepoAntiFeatures(),
    ) { prod, afs ->
        val catsMap = afs.associateBy(AntiFeatureDetails::name)
        prod?.let {
            it.first.product.antiFeatures.map { catsMap[it] ?: AntiFeatureDetails(it, "") }
        }.orEmpty()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        emptyList(),
    )

    val privacyData = combine(
        installedItem,
        trackers,
        productRepos,
        antifeatureDetails,
    ) { ins, trs, prs, afs ->
        val suggestedProduct = findSuggestedProduct(prs, ins) { it.first }
        PrivacyData(
            permissions = suggestedProduct?.first?.displayRelease
                ?.generatePermissionGroups(NeoApp.context) ?: emptyMap(),
            trackers = trs,
            antiFeatures = afs,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        PrivacyData()
    )

    val privacyNote = privacyData.mapLatest {
        it.toPrivacyNote()
    }

    val categoryDetails = combine(
        suggestedProductRepo,
        productsRepo.getAllCategoryDetails(),
    ) { prod, cats ->
        val catsMap = cats.associateBy(CategoryDetails::name)
        prod?.let {
            it.first.product.categories.map { catsMap[it]?.label ?: it }
        }.orEmpty()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        emptyList(),
    )

    val downloadingState = downloadedRepo.getLatestFlow(packageName)
        .mapLatest { it?.state }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            null
        )

    val extras = packageName
        .flatMapLatest { pn ->
            extrasRepo.get(pn)
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            null
        )

    val authorProducts = combine(
        packageName,
        developer,
        developerProds,
    ) { pn, dev, prods ->
        if (dev.isNotEmpty()) prods
            .filter { it.product.packageName != pn }
            .groupBy { it.product.packageName }
            .mapNotNull { it.value.maxByOrNull { it.product.added } }
        else emptyList()
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
            val canShare = product != null && prs[0].second.webBaseUrl.isNotBlank()

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
                canUpdate                                            -> ActionState.Update
                canLaunch                                            -> ActionState.Launch
                canInstall && !Preferences[Preferences.Key.KidsMode] -> ActionState.Install
                canShare                                             -> ActionState.Share
                else                                                 -> ActionState.NoAction
            }

            val mA = if (ds != null && ds.isActive)
                ds.toActionState() ?: primaryAction
            else primaryAction
            Pair(mA, actions.minus(mA))
        }.distinctUntilChanged()

    val mainAction: StateFlow<ActionState> = actions.map { it.first }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            ActionState.Bookmark
        )

    val subActions: StateFlow<Set<ActionState>> = actions.map { it.second }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
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
                /* TODO productsRepo.updateReleases(
                    features,
                    setBoolean || Preferences[Preferences.Key.UpdateUnstable],
                    *repos.value.map { EmbeddedProduct(it.first.toV2(),it.first.releases) }.toList().toTypedArray(),
                )*/
                productsRepo.upsertProduct(
                    *productRepos.value.map { prodRepo ->
                        prodRepo.first.apply {
                            refreshReleases(
                                features,
                                setBoolean || Preferences[Preferences.Key.UpdateUnstable]
                            )
                        }.product
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
