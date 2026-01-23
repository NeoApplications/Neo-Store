package com.machiav3lli.fdroid.viewmodels

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.RELEASE_STATE_INSTALLED
import com.machiav3lli.fdroid.RELEASE_STATE_NONE
import com.machiav3lli.fdroid.RELEASE_STATE_SUGGESTED
import com.machiav3lli.fdroid.STATEFLOW_SUBSCRIBE_BUFFER
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.AntiFeatureDetails
import com.machiav3lli.fdroid.data.database.entity.CategoryDetails
import com.machiav3lli.fdroid.data.database.entity.EmbeddedProduct
import com.machiav3lli.fdroid.data.database.entity.ExodusInfo
import com.machiav3lli.fdroid.data.database.entity.Extras
import com.machiav3lli.fdroid.data.database.entity.Installed
import com.machiav3lli.fdroid.data.database.entity.MonthlyPackageSum
import com.machiav3lli.fdroid.data.database.entity.RBLog
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.database.entity.Tracker
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.data.entity.DownloadState
import com.machiav3lli.fdroid.data.entity.PrivacyData
import com.machiav3lli.fdroid.data.entity.PrivacyNote
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import com.machiav3lli.fdroid.utils.Utils.startUpdate
import com.machiav3lli.fdroid.utils.extension.Quadruple
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.combine
import com.machiav3lli.fdroid.utils.extension.grantedPermissions
import com.machiav3lli.fdroid.utils.extension.text.intToIsoDate
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.findSuggestedProduct
import com.machiav3lli.fdroid.utils.generatePermissionGroups
import com.machiav3lli.fdroid.utils.shareIntent
import com.machiav3lli.fdroid.utils.startLauncherActivity
import com.machiav3lli.fdroid.utils.toPrivacyNote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AppPageVM(
    downloadedRepo: DownloadedRepository,
    private val productsRepo: ProductsRepository,
    private val extrasRepo: ExtrasRepository,
    installedRepo: InstalledRepository,
    privacyRepo: PrivacyRepository,
    reposRepo: RepositoriesRepository,
) : ViewModel() {
    private val packageName = MutableStateFlow("")

    private val products = packageName
        .flatMapLatest { pn -> productsRepo.getProduct(pn) }
        .distinctUntilChanged()

    private val developer = products
        .mapLatest {
            it.firstOrNull()?.product?.author?.let {
                it.name.nullIfEmpty() ?: it.email.nullIfEmpty() ?: it.web.nullIfEmpty()
            }.orEmpty()
        }.distinctUntilChanged()

    private val repositories = reposRepo.getAll().distinctUntilChanged()

    private val rbLogs = packageName
        .flatMapLatest { pn -> privacyRepo.getRBLogsMap(pn) }
        .distinctUntilChanged()

    private val installedItem = packageName
        .flatMapLatest { packageName -> installedRepo.get(packageName) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            null
        )

    private val productRepos = combine(
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

    private val suggestedProductRepo = combine(
        productRepos,
        installedItem,
    ) { prodRepos, installed ->
        findSuggestedProduct(prodRepos, installed) { it.first }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        null
    )

    private val releaseItems = combine(
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

                        release.incompatibilities.isEmpty()
                                && release.selected
                                && release.versionCode >= (installed?.versionCode ?: 0)
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

    val coreAppState: StateFlow<CoreAppState> = combine(
        suggestedProductRepo,
        installedItem,
        releaseItems,
        productRepos,
    ) { suggested, installed, releases, prodRepos ->
        CoreAppState(
            suggestedProductRepo = suggested,
            releaseItems = releases,
            productRepos = prodRepos,
            installed = installed,
            isInstalled = installed != null,
            canUpdate = suggested?.first?.canUpdate(installed) ?: false,
            installedVersion = installed?.version ?: "",
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        CoreAppState()
    )

    // Extra state
    private val categoryDetails = combine(
        suggestedProductRepo,
        productsRepo.getAllCategoryDetails(),
    ) { prod, cats ->
        val catsMap = cats.associateBy(CategoryDetails::name)
        prod?.let {
            it.first.product.categories.map { catsMap[it]?.label ?: it }
        }.orEmpty()
    }.distinctUntilChanged()

    private val downloadingState = downloadedRepo.getLatestFlow(packageName)
        .mapLatest { it?.state }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            null
        )

    private val extras = packageName
        .flatMapLatest { pn -> extrasRepo.get(pn) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
            null
        )

    private val authorProducts = combine(
        packageName,
        developer,
        developer.flatMapLatest {
            productsRepo.getAuthorList(it)
        },
    ) { pn, dev, prods ->
        if (dev.isNotEmpty()) prods
            .filter { it.product.packageName != pn }
            .groupBy { it.product.packageName }
            .mapNotNull { it.value.maxByOrNull { embdProd -> embdProd.product.added }?.toItem() }
        else emptyList()
    }.distinctUntilChanged()

    // Privacy state
    private val requestedPermissions = combine(
        packageName,
        installedItem,
    ) { pn, inst ->
        runCatching {
            if (inst != null) NeoApp.context.packageManager.getPackageInfo(
                pn,
                PackageManager.GET_PERMISSIONS
            ).grantedPermissions else emptyMap()
        }.fold(
            onSuccess = { it },
            onFailure = { emptyMap() }
        )
    }.distinctUntilChanged()

    private val exodusInfo = packageName
        .flatMapLatest { pn ->
            privacyRepo.getExodusInfos(pn)
        }
        .mapLatest { it.maxByOrNull(ExodusInfo::version_code) }

    private val trackers = combine(exodusInfo, privacyRepo.getAllTrackers()) { info, trackers ->
        trackers.filter { it.key in info?.trackers.orEmpty() }
    }

    private val privacyData = combine(
        suggestedProductRepo,
        trackers,
        reposRepo.getRepoAntiFeaturesMap().distinctUntilChanged(),
    ) { suggestedProduct, trs, afsMap ->
        PrivacyData(
            permissions = suggestedProduct?.first?.displayRelease
                ?.generatePermissionGroups(NeoApp.context) ?: emptyMap(),
            trackers = trs,
            antiFeatures = suggestedProduct?.let {
                it.first.product.antiFeatures.map { af ->
                    afsMap[af] ?: AntiFeatureDetails(af, "")
                }
            }.orEmpty(),
        )
    }.distinctUntilChanged()

    val privacyPanelState: StateFlow<PrivacyPanelState> = combine(
        trackers,
        installedItem,
        requestedPermissions,
        exodusInfo,
        privacyData,
        rbLogs,
    ) { trackers, installed, permissions, exodus, privacy, logs ->
        PrivacyPanelState(
            trackers = trackers,
            isInstalled = installed != null,
            requestedPermissions = permissions,
            exodusInfo = exodus,
            privacyData = privacy,
            privacyNote = privacy.toPrivacyNote(),
            rbLogs = logs,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        PrivacyPanelState()
    )

    // Download stats
    private val downloadStatsInfo = packageName
        .flatMapLatest { pn ->
            combine(
                privacyRepo.getClientSumDownloadStats(pn),
                privacyRepo.getLatestDownloadStats(pn),
                if (Android.sdk(Build.VERSION_CODES.R))
                    privacyRepo.getSumDownloadOrder(pn)
                else privacyRepo.getSumDownloadOrderLegacy(pn)
            ) { stats, latests, order ->
                Quadruple(
                    stats.find { it.client == "_total" }?.totalCount ?: 0,
                    latests.filter { it.client == "_total" }.sumOf { it.count },
                    stats.filterNot { it.client == "_total" || it.client == "_unknown" }
                        .maxByOrNull { it.totalCount }?.client ?: "",
                    order
                )
            }
        }.distinctUntilChanged()

    private val downloadStatsMonthlyMap = packageName
        .flatMapLatest { pn -> privacyRepo.getMonthlyDownloadStats(pn) }
        .map {
            it.groupBy { stats -> stats.yearMonth.intToIsoDate() }
                .mapValues { entry ->
                    entry.value.groupBy(MonthlyPackageSum::client)
                        .mapValues { stats -> stats.value.sumOf { stat -> stat.totalCount } }
                }
        }.distinctUntilChanged()

    val downloadStatsState: StateFlow<DownloadStatsState> = combine(
        downloadStatsInfo,
        downloadStatsMonthlyMap,
    ) { info, monthly ->
        DownloadStatsState(
            info = info,
            monthlyMap = monthly,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        DownloadStatsState()
    )

    // Actions
    val actionExecutionState: StateFlow<ActionExecutionState>
        field = MutableStateFlow(ActionExecutionState())

    private val canInstall: Flow<Boolean> = combine(
        suggestedProductRepo,
        installedItem,
        downloadingState,
    ) { product, installed, downloadState ->
        val compatible = product?.first?.selectedReleases?.firstOrNull()
            ?.let { it.incompatibilities.isEmpty() } ?: false

        product != null &&
                installed == null &&
                compatible &&
                downloadState?.isActive != true
    }.distinctUntilChanged()

    private val canUpdate: Flow<Boolean> = combine(
        suggestedProductRepo,
        installedItem,
        downloadingState,
        extras,
    ) { product, installed, downloadState, extrasData ->
        val compatible = product?.first?.selectedReleases?.firstOrNull()
            ?.let { it.incompatibilities.isEmpty() } ?: false

        product != null &&
                compatible &&
                product.first.canUpdate(installed) &&
                !shouldIgnore(product.first.versionCode, extrasData) &&
                downloadState?.isActive != true
    }.distinctUntilChanged()

    private val canUninstall: Flow<Boolean> = combine(
        suggestedProductRepo,
        installedItem,
    ) { product, installed ->
        product != null && installed != null && !installed.isSystem
    }.distinctUntilChanged()

    private val canLaunch: Flow<Boolean> = combine(
        suggestedProductRepo,
        installedItem,
    ) { product, installed ->
        product != null &&
                installed != null &&
                installed.launcherActivities.isNotEmpty()
    }.distinctUntilChanged()

    private val canShare: Flow<Boolean> = productRepos
        .map { prods -> prods.any { it.second.webBaseUrl.isNotBlank() } }
        .distinctUntilChanged()

    private val canShowDetails: Flow<Boolean> = installedItem
        .map { it != null }
        .distinctUntilChanged()

    private val activeDownloadAction: Flow<ActionState?> = downloadingState
        .map { it?.toActionState() }
        .distinctUntilChanged()

    private val availableActions: StateFlow<Set<ActionState>> = combine(
        canInstall,
        canUpdate,
        canUninstall,
        canLaunch,
        canShare,
        canShowDetails,
    ) { install, update, uninstall, launch, share, details ->
        buildSet {
            if (update) add(ActionState.Update)
            if (install && !Preferences[Preferences.Key.KidsMode]) add(ActionState.Install)
            if (launch) add(ActionState.Launch)
            if (details) add(ActionState.Details)
            if (uninstall) add(ActionState.Uninstall)
            if (share) add(ActionState.Share)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        emptySet()
    )

    private val primaryAction: StateFlow<ActionState> = combine(
        canUpdate,
        canLaunch,
        canInstall,
        canShare,
        activeDownloadAction,
    ) { update, launch, install, share, downloadAction ->
        if (downloadAction != null) return@combine downloadAction

        when {
            update                                            -> ActionState.Update
            launch                                            -> ActionState.Launch
            install && !Preferences[Preferences.Key.KidsMode] -> ActionState.Install
            share                                             -> ActionState.Share
            else                                              -> ActionState.NoAction
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        ActionState.NoAction
    )

    private val secondaryActions: StateFlow<Set<ActionState>> = combine(
        availableActions,
        primaryAction,
    ) { available, primary ->
        available - primary
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STATEFLOW_SUBSCRIBE_BUFFER),
        emptySet()
    )

    val extraAppState: StateFlow<ExtraAppState> = combine(
        repositories,
        authorProducts,
        categoryDetails,
        downloadingState,
        primaryAction,
        secondaryActions,
        extras,
    ) { repos, authorProds, categories, downloading, pAct, sActs, ext ->
        ExtraAppState(
            repositories = repos,
            authorProducts = authorProds,
            categoryDetails = categories,
            downloadingState = downloading,
            mainAction = pAct,
            subActions = sActs,
            extras = ext,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        ExtraAppState()
    )

    fun setApp(pn: String) = packageName.update { pn }

    private fun shouldIgnore(appVersionCode: Long, extras: Extras?): Boolean =
        extras?.ignoredVersion == appVersionCode || extras?.ignoreUpdates == true

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

    fun processActionCommand(command: AppActionCommand, context: Context) {
        viewModelScope.launch {
            when (command) {
                is AppActionCommand.Execute   -> {
                    val confirmation = checkIfNeedsConfirmation(command.action)

                    if (confirmation != null) {
                        actionExecutionState.update {
                            it.copy(pendingConfirmation = command.action to confirmation)
                        }
                    } else {
                        executeActionInternal(command.action, context)
                    }
                }

                is AppActionCommand.Confirmed -> {
                    actionExecutionState.update { it.copy(pendingConfirmation = null) }
                    executeActionInternal(command.action, context)
                }

                AppActionCommand.Cancel       -> {
                    actionExecutionState.update {
                        it.copy(pendingConfirmation = null, error = null)
                    }
                }
            }
        }
    }

    private fun checkIfNeedsConfirmation(action: ActionState): DialogKey? {
        val state = coreAppState.value
        val extras = extraAppState.value.extras

        return when (action) {
            ActionState.Install, ActionState.Update -> {
                if (Preferences[Preferences.Key.DownloadShowDialog]) {
                    DialogKey.Download(
                        state.suggestedProductRepo?.first?.product?.label ?: packageName.value
                    ) {
                        startUpdate(
                            packageName.value,
                            state.installed,
                            state.productRepos,
                        )
                    }
                } else null
            }

            ActionState.Launch                      -> {
                state.installed?.let { installed ->
                    if (installed.launcherActivities.size >= 2) {
                        DialogKey.Launch(
                            installed.packageName,
                            installed.launcherActivities,
                        )
                    } else null
                }
            }

            ActionState.Uninstall                   -> {
                if (NeoApp.installer.isRoot()) {
                    DialogKey.Uninstall(
                        state.suggestedProductRepo?.first?.product?.label ?: packageName.value
                    ) {
                        viewModelScope.launch {
                            NeoApp.installer.uninstall(packageName.value)
                        }
                    }
                } else null
            }

            else                                    -> null
        }
    }

    private suspend fun executeActionInternal(action: ActionState, context: Context) {
        actionExecutionState.update { it.copy(isExecuting = true, error = null) }

        try {
            val state = coreAppState.value
            val neoActivity = context as? NeoActivity

            when (action) {
                ActionState.Install, ActionState.Update      -> {
                    startUpdate(
                        packageName.value,
                        state.installed,
                        state.productRepos,
                    )
                }

                ActionState.Launch                           -> {
                    state.installed?.let { installed ->
                        installed.launcherActivities.firstOrNull()
                            ?.let { context.startLauncherActivity(installed.packageName, it.first) }
                    }
                }

                ActionState.Details                          -> {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData("package:${packageName.value}".toUri())
                    )
                }

                ActionState.Uninstall                        -> {
                    NeoApp.installer.uninstall(packageName.value)
                }

                is ActionState.CancelPending,
                is ActionState.CancelConnecting,
                is ActionState.CancelDownloading             -> {
                    val cancelIntent = Intent(context, ActionReceiver::class.java).apply {
                        this.action = ActionReceiver.COMMAND_CANCEL_DOWNLOAD
                        putExtra(ARG_PACKAGE_NAME, packageName.value)
                    }
                    neoActivity?.sendBroadcast(cancelIntent)
                }

                ActionState.Share                            -> {
                    val prodRepo = state.productRepos.first { it.second.webBaseUrl.isNotBlank() }
                    context.shareIntent(
                        packageName.value,
                        prodRepo.first.product.label,
                        prodRepo.second.webBaseUrl,
                    )
                }

                ActionState.Bookmark, ActionState.Bookmarked -> {
                    setFavorite(packageName.value, action is ActionState.Bookmark)
                }

                else                                         -> {
                    actionExecutionState.update {
                        it.copy(error = "Unsupported action: $action")
                    }
                }
            }

            actionExecutionState.update { it.copy(isExecuting = false) }
        } catch (e: Exception) {
            actionExecutionState.update {
                it.copy(isExecuting = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun clearActionError() {
        actionExecutionState.update { it.copy(error = null) }
    }

    fun setFavorite(packageName: String, setBoolean: Boolean) {
        viewModelScope.launch {
            extrasRepo.setFavorite(packageName, setBoolean)
        }
    }
}

sealed interface AppActionCommand {
    data class Execute(val action: ActionState) : AppActionCommand
    data class Confirmed(val action: ActionState) : AppActionCommand
    data object Cancel : AppActionCommand
}

data class ActionExecutionState(
    val isExecuting: Boolean = false,
    val pendingConfirmation: Pair<ActionState, DialogKey>? = null,
    val error: String? = null,
)

data class PrivacyPanelState(
    val trackers: List<Tracker> = emptyList(),
    val isInstalled: Boolean = false,
    val requestedPermissions: Map<String, Boolean> = emptyMap(),
    val exodusInfo: ExodusInfo? = null,
    val privacyData: PrivacyData = PrivacyData(),
    val privacyNote: PrivacyNote = PrivacyNote(),
    val rbLogs: Map<String, RBLog> = emptyMap(),
)

data class DownloadStatsState(
    val info: Quadruple<Long, Long, String, Int> = Quadruple(0L, 0L, "", 9999),
    val monthlyMap: Map<String, Map<String, Long>> = emptyMap(),
)

data class CoreAppState(
    val suggestedProductRepo: Pair<EmbeddedProduct, Repository>? = null,
    val releaseItems: List<Quadruple<Release, Repository, Int, RBLog?>> = emptyList(),
    val productRepos: List<Pair<EmbeddedProduct, Repository>> = emptyList(),
    val installed: Installed? = null,
    val isInstalled: Boolean = false,
    val canUpdate: Boolean = false,
    val installedVersion: String = "",
)

data class ExtraAppState(
    val repositories: List<Repository> = emptyList(),
    val authorProducts: List<ProductItem> = emptyList(),
    val categoryDetails: List<String> = emptyList(),
    val downloadingState: DownloadState? = null,
    val mainAction: ActionState = ActionState.Bookmark,
    val subActions: Set<ActionState> = emptySet(),
    val extras: Extras? = null,
)