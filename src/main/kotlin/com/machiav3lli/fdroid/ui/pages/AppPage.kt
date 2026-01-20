package com.machiav3lli.fdroid.ui.pages

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Release
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.ActionState
import com.machiav3lli.fdroid.data.entity.AntiFeature
import com.machiav3lli.fdroid.data.entity.DialogKey
import com.machiav3lli.fdroid.data.entity.DonateType
import com.machiav3lli.fdroid.manager.network.createIconUri
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import com.machiav3lli.fdroid.manager.work.DownloadWorker
import com.machiav3lli.fdroid.manager.work.ExodusWorker
import com.machiav3lli.fdroid.ui.components.ExpandableItemsBlock
import com.machiav3lli.fdroid.ui.components.MonthlyLineChart
import com.machiav3lli.fdroid.ui.components.MultiLineChart
import com.machiav3lli.fdroid.ui.components.ScreenshotItem
import com.machiav3lli.fdroid.ui.components.ScreenshotList
import com.machiav3lli.fdroid.ui.components.SegmentedTabButton
import com.machiav3lli.fdroid.ui.components.SimpleLineChart
import com.machiav3lli.fdroid.ui.components.SwitchPreference
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.components.appsheet.AppInfoChips
import com.machiav3lli.fdroid.ui.components.appsheet.AppInfoHeader
import com.machiav3lli.fdroid.ui.components.appsheet.HtmlTextBlock
import com.machiav3lli.fdroid.ui.components.appsheet.LinkItem
import com.machiav3lli.fdroid.ui.components.appsheet.PrivacyPanel
import com.machiav3lli.fdroid.ui.components.appsheet.ReleaseItem
import com.machiav3lli.fdroid.ui.components.appsheet.SourceCodeButton
import com.machiav3lli.fdroid.ui.components.appsheet.TopBarHeader
import com.machiav3lli.fdroid.ui.components.appsheet.WarningCard
import com.machiav3lli.fdroid.ui.components.appsheet.appInfoChips
import com.machiav3lli.fdroid.ui.components.appsheet.downloadInfoChips
import com.machiav3lli.fdroid.ui.components.common.BottomSheet
import com.machiav3lli.fdroid.ui.components.privacy.MeterIconsBar
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Calendar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesFour
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import com.machiav3lli.fdroid.ui.compose.utils.blockBorderBottom
import com.machiav3lli.fdroid.ui.dialog.ActionSelectionDialogUI
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.utils.Utils.startUpdate
import com.machiav3lli.fdroid.utils.extension.koinNeoViewModel
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.generateLinks
import com.machiav3lli.fdroid.utils.shareIntent
import com.machiav3lli.fdroid.utils.shareReleaseIntent
import com.machiav3lli.fdroid.utils.startLauncherActivity
import com.machiav3lli.fdroid.viewmodels.AppPageVM
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoroutinesApi::class
)
@Composable
fun AppPage(
    packageName: String,
    viewModel: AppPageVM = koinNeoViewModel(),
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val neoActivity = LocalActivity.current as NeoActivity
    val scope = rememberCoroutineScope()
    val showScreenshots = rememberSaveable { mutableStateOf(false) }
    val statsTab = rememberSaveable { mutableIntStateOf(0) }
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val currentPage by remember { derivedStateOf { pagerState.currentPage } }
    var screenshotPage by rememberSaveable { mutableIntStateOf(0) }
    val screenshotsPageState = rememberModalBottomSheetState(true)
    val appState by viewModel.coreAppState.collectAsStateWithLifecycle()
    val extraState by viewModel.extraAppState.collectAsStateWithLifecycle()
    val privacyState by viewModel.privacyPanelState.collectAsStateWithLifecycle()
    val downloadStatsState by viewModel.downloadStatsState.collectAsStateWithLifecycle()

    LaunchedEffect(packageName) {
        viewModel.setApp(packageName)
    }

    BackHandler(currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(0) }
    }

    val enableScreenshots by remember(Preferences[Preferences.Key.ShowScreenshots]) {
        derivedStateOf {
            Preferences[Preferences.Key.ShowScreenshots]
        }
    }
    val snackbarHostState = remember { SnackbarHostState() }
    val nestedScrollConnection = rememberNestedScrollInteropConnection()
    val coroutineScope = rememberCoroutineScope()

    val onUriClick = { uri: Uri, shouldConfirm: Boolean ->
        if (shouldConfirm && (uri.scheme == "http" || uri.scheme == "https")) {
            dialogKey.value = DialogKey.Link(uri)
            openDialog.value = true
            true
        } else {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                true
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                false
            }
        }
    }

    val copyLinkToClipboard = { link: String ->
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, link))
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.link_copied_to_clipboard),
                actionLabel = context.getString(R.string.open),
                duration = SnackbarDuration.Short,
            ).apply {
                if (this == SnackbarResult.ActionPerformed) {
                    onUriClick(link.toUri(), false)
                }
            }
        }
    }

    val onReleaseClick = { release: Release ->
        val installedItem = appState.installed
        when {
            release.incompatibilities.isNotEmpty()                               -> {
                dialogKey.value = DialogKey.ReleaseIncompatible(
                    release.incompatibilities,
                    release.platforms, release.minSdkVersion, release.maxSdkVersion
                )
                openDialog.value = true
            }

            installedItem != null
                    && installedItem.versionCode > release.versionCode
                    && !Preferences[Preferences.Key.DisableDownloadVersionCheck] -> {
                dialogKey.value = DialogKey.ReleaseIssue(R.string.incompatible_older_DESC)
                openDialog.value = true
            }

            installedItem != null
                    && release.signature !in installedItem.signatures
                    && !Preferences[Preferences.Key.DisableSignatureCheck]       -> {
                dialogKey.value = DialogKey.ReleaseIssue(R.string.incompatible_signature_DESC)
                openDialog.value = true
            }

            else                                                                 -> {
                val productRepository = appState.productRepos.asSequence()
                    .filter { it.second.id == release.repositoryId }
                    .firstOrNull()
                if (productRepository != null) {
                    val action = {
                        DownloadWorker.enqueue(
                            packageName,
                            productRepository.first.product.label,
                            productRepository.second,
                            release
                        )
                    }
                    if (Preferences[Preferences.Key.DownloadShowDialog]) {
                        dialogKey.value =
                            DialogKey.Download(productRepository.first.product.label, action)
                        openDialog.value = true
                    } else action()
                }
            }
        }
    }

    val onActionClick: (ActionState) -> Unit = { action: ActionState ->
        Log.d("AppPage", "onClickAction: ${action::class.java}")
        when (action) {
            ActionState.Install,
            ActionState.Update,
                 -> {
                val actionJob: () -> Unit = {
                    startUpdate(
                        packageName,
                        appState.installed,
                        appState.productRepos,
                    )
                }
                if (Preferences[Preferences.Key.DownloadShowDialog]) {
                    dialogKey.value =
                        DialogKey.Download(
                            appState.suggestedProductRepo?.first?.product?.label ?: packageName,
                            actionJob
                        )
                    openDialog.value = true
                } else actionJob()
            }

            ActionState.Launch
                 -> appState.installed?.let { installed ->
                if (installed.launcherActivities.size >= 2) {
                    dialogKey.value = DialogKey.Launch(
                        installed.packageName,
                        installed.launcherActivities,
                    )
                    openDialog.value = true
                } else {
                    installed.launcherActivities.firstOrNull()
                        ?.let { context.startLauncherActivity(installed.packageName, it.first) }
                }
            }

            ActionState.Details
                 -> context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData("package:$packageName".toUri())
            )

            ActionState.Uninstall
                 -> {
                val actionJob: () -> Unit = {
                    scope.launch {
                        NeoApp.installer.uninstall(packageName)
                    }
                }
                if (NeoApp.installer.isRoot()) {
                    dialogKey.value = DialogKey.Uninstall(
                        appState.suggestedProductRepo?.first?.product?.label ?: packageName,
                        actionJob
                    )
                    openDialog.value = true
                } else actionJob()
            }

            is ActionState.CancelPending,
            is ActionState.CancelConnecting,
            is ActionState.CancelDownloading,
                 -> {
                val cancelIntent = Intent(context, ActionReceiver::class.java).apply {
                    this.action = ActionReceiver.COMMAND_CANCEL_DOWNLOAD
                    putExtra(ARG_PACKAGE_NAME, packageName)
                }
                neoActivity.sendBroadcast(cancelIntent)
            }

            ActionState.Share
                 -> {
                val prodRepo = appState.productRepos.first { it.second.webBaseUrl.isNotBlank() }
                context.shareIntent(
                    packageName,
                    prodRepo.first.product.label,
                    prodRepo.second.webBaseUrl,
                )
            }

            ActionState.Bookmark,
            ActionState.Bookmarked,
                 -> viewModel.setFavorite(packageName, action is ActionState.Bookmark)

            else -> {}
        }
    }

    appState.suggestedProductRepo?.let { (eProduct, repo) ->
        val product by derivedStateOf { eProduct.product }
        val imageData by remember(product, repo) {
            mutableStateOf(
                createIconUri(
                    product.icon,
                    repo.address,
                    repo.authentication
                ).toString()
            )
        }

        val screenshots by remember(product) {
            derivedStateOf {
                product.screenshots.map {
                    ScreenshotItem(it, repo)
                }
            }
        }

        val displayRelease by remember {
            derivedStateOf { eProduct.displayRelease }
        }

        LaunchedEffect(product) {
            async {
                ExodusWorker.fetchExodusInfo(product.packageName, eProduct.versionCode)
            }
        }

        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            topBar = {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                        .padding(bottom = 8.dp),
                ) {
                    TopBarHeader(
                        appName = product.label,
                        packageName = product.packageName,
                        icon = imageData,
                        state = extraState.downloadingState,
                        actions = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                SourceCodeButton(
                                    sourceType = privacyState.privacyNote.sourceType,
                                    onClick = {
                                        onUriClick(
                                            (product.source.nullIfEmpty() ?: product.web).toUri(),
                                            true
                                        )
                                    },
                                    onLongClick = {
                                        product.source.let { link ->
                                            if (link.isNotEmpty()) copyLinkToClipboard(link)
                                        }
                                    }
                                )
                                TopBarAction(
                                    icon = Phosphor.X,
                                    description = stringResource(id = R.string.cancel),
                                ) {
                                    onDismiss()
                                }
                            }
                        },
                    )
                    AppInfoChips(
                        eProduct.appInfoChips(
                            appState.canUpdate,
                            privacyState.isInstalled,
                            appState.installedVersion,
                            displayRelease,
                            extraState.categoryDetails
                        )
                    )
                    MeterIconsBar(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTrackers = if (privacyState.exodusInfo != null) privacyState.privacyNote.trackersRank
                        else null,
                        selectedPermissions = privacyState.privacyNote.permissionsRank,
                        currentPage = currentPage,
                    ) {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                if (currentPage == 0) 1
                                else 0
                            )
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(paddingValues)
                    .blockBorderBottom(),
                beyondViewportPageCount = 1,
            ) { pageIndex ->
                if (pageIndex == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .nestedScroll(nestedScrollConnection)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp),
                    ) {
                        if (product.antiFeatures.contains(AntiFeature.KNOWN_VULN.key)) {
                            item {
                                WarningCard(stringResource(id = R.string.has_security_vulnerabilities))
                            }
                        }
                        item {
                            AppInfoHeader(
                                mainAction = extraState.mainAction,
                                possibleActions = extraState.subActions,
                                favState = { extraState.extras?.favorite == true },
                                onAction = onActionClick
                            )
                        }
                        item {
                            AnimatedVisibility(visible = appState.canUpdate) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.ignore_this_update),
                                    initSelected = { extraState.extras?.ignoredVersion == eProduct.versionCode },
                                    onCheckedChanged = {
                                        viewModel.setIgnoredVersion(
                                            product.packageName,
                                            if (it) eProduct.versionCode else 0
                                        )
                                    }
                                )
                            }
                            AnimatedVisibility(visible = privacyState.isInstalled) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.ignore_all_updates),
                                    initSelected = { extraState.extras?.ignoreUpdates == true },
                                    onCheckedChanged = {
                                        viewModel.setIgnoreUpdates(product.packageName, it)
                                    }
                                )
                            }
                            AnimatedVisibility(visible = privacyState.isInstalled) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.ignore_vulns),
                                    initSelected = { extraState.extras?.ignoreVulns == true },
                                    onCheckedChanged = {
                                        viewModel.setIgnoreVulns(product.packageName, it)
                                    }
                                )
                            }
                            AnimatedVisibility(visible = privacyState.isInstalled) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.allow_unstable_updates),
                                    initSelected = { extraState.extras?.allowUnstable == true },
                                    onCheckedChanged = {
                                        viewModel.setAllowUnstableUpdates(product.packageName, it)
                                    }
                                )
                            }
                        }
                        if (enableScreenshots) { // TODO add optional screenshots button
                            item {
                                ScreenshotList(
                                    screenShots = screenshots,
                                    video = product.video,
                                    onUriClick = { onUriClick(it, true) },
                                ) { index ->
                                    screenshotPage = index
                                    showScreenshots.value = true
                                }
                            }
                        }
                        item { // TODO add markdown parsing or not?
                            if ((product.description + product.summary).isNotEmpty()) HtmlTextBlock(
                                shortText = product.summary,
                                longText = product.description
                            ) {
                                onUriClick(it.toUri(), true)
                            }
                        }
                        val links = product.generateLinks(context)
                        item {
                            if (links.isNotEmpty()) {
                                ExpandableItemsBlock(heading = stringResource(id = R.string.links)) {
                                    links.forEach { item ->
                                        LinkItem(
                                            linkType = item,
                                            onClick = { link ->
                                                link?.let { onUriClick(it, true) }
                                            },
                                            onLongClick = { link ->
                                                copyLinkToClipboard(link.toString())
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        if (product.donates.isNotEmpty()) item {
                            ExpandableItemsBlock(heading = stringResource(id = R.string.donate)) {
                                product.donates.forEach { item ->
                                    LinkItem(
                                        linkType = DonateType(item, context),
                                        onClick = { link ->
                                            link?.let { onUriClick(it, true) }
                                        },
                                        onLongClick = { link ->
                                            copyLinkToClipboard(link.toString())
                                        }
                                    )
                                }
                            }
                        }
                        item {
                            if (extraState.authorProducts.isNotEmpty()) {
                                ExpandableItemsBlock(
                                    heading = stringResource(
                                        id = R.string.other_apps_by,
                                        product.author.name
                                    ),
                                ) {
                                    ProductsHorizontalRecycler(
                                        productsList = extraState.authorProducts,
                                        repositories = extraState.repositories.associateBy(
                                            Repository::id
                                        ),
                                        rowsNumber = 1,
                                    ) { item ->
                                        neoActivity.navigateProduct(item.packageName)
                                    }
                                }
                            }
                        }
                        if (downloadStatsState.dailyMap.isNotEmpty()) {
                            item {
                                ExpandableItemsBlock(
                                    heading = stringResource(id = R.string.download_stats_iod),
                                    preExpanded = false,
                                ) {
                                    AppInfoChips(downloadStatsState.info.downloadInfoChips())
                                    SingleChoiceSegmentedButtonRow(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                                shape = MaterialTheme.shapes.extraLarge,
                                            )
                                            .padding(horizontal = 4.dp),
                                    ) {
                                        SegmentedTabButton(
                                            text = stringResource(id = R.string.total),
                                            icon = Phosphor.Download,
                                            selected = {
                                                statsTab.intValue == 0
                                            },
                                            onClick = {
                                                statsTab.intValue = 0
                                            },
                                        )
                                        SegmentedTabButton(
                                            text = stringResource(id = R.string.clients),
                                            icon = Phosphor.CirclesFour,
                                            selected = {
                                                statsTab.intValue == 1
                                            },
                                            onClick = {
                                                statsTab.intValue = 1
                                            },
                                        )
                                        SegmentedTabButton(
                                            text = stringResource(id = R.string.monthly),
                                            icon = Phosphor.Calendar,
                                            selected = {
                                                statsTab.intValue == 2
                                            },
                                            onClick = {
                                                statsTab.intValue = 2
                                            },
                                        )
                                    }
                                    when (statsTab.intValue) {
                                        0 -> SimpleLineChart(downloadStatsState.dailyMap)
                                        1 -> MultiLineChart(downloadStatsState.dailyMap)
                                        2 -> MonthlyLineChart(downloadStatsState.monthlyMap)
                                    }
                                }
                            }
                        }
                        item {
                            if (product.whatsNew.isNotEmpty()) {
                                ExpandableItemsBlock(
                                    heading = stringResource(id = R.string.changes),
                                    preExpanded = true,
                                ) {
                                    Text(
                                        product.whatsNew.trim { it <= ' ' },
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth()
                                    )
                                }
                            }

                        }
                        item {
                            Text(
                                text = stringResource(
                                    id = if (appState.releaseItems.isEmpty()) R.string.no_releases
                                    else R.string.releases
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                        items(
                            items = appState.releaseItems,
                            key = { it.first.identifier },
                        ) { item ->
                            ReleaseItem(
                                release = item.first,
                                repository = item.second,
                                releaseState = item.third,
                                rbLog = item.fourth,
                                onDownloadClick = { release ->
                                    onReleaseClick(release)
                                },
                                onShareClick = {
                                    context.shareReleaseIntent(
                                        "${product.label} ${item.first.version}",
                                        it.getDownloadUrl(item.second)
                                    )
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                } else {
                    PrivacyPanel(
                        modifier = Modifier
                            .nestedScroll(nestedScrollConnection)
                            .fillMaxSize(),
                        packageName,
                        viewModel,
                        copyLinkToClipboard,
                        onUriClick,
                    )
                }
            }

            if (showScreenshots.value) {
                BottomSheet(
                    sheetState = screenshotsPageState,
                    shape = RectangleShape,
                    onDismiss = {
                        scope.launch { screenshotsPageState.hide() }
                        showScreenshots.value = false
                    },
                ) {
                    ScreenshotsPage(
                        screenshots = screenshots,
                        page = screenshotPage
                    )
                }
            }

            if (openDialog.value) {
                BaseDialog(openDialogCustom = openDialog) {
                    when (dialogKey.value) {
                        is DialogKey.Launch -> ActionSelectionDialogUI(
                            titleId = R.string.launch,
                            options = (dialogKey.value as DialogKey.Launch)
                                .launcherActivities.toMap(),
                            openDialogCustom = openDialog,
                            onAction = { key ->
                                context.startLauncherActivity(
                                    (dialogKey.value as DialogKey.Launch).packageName,
                                    key
                                )
                                openDialog.value = false
                            }
                        )

                        else                -> KeyDialogUI(
                            key = dialogKey.value,
                            openDialog = openDialog,
                            primaryAction = {
                                when (dialogKey.value) {
                                    is DialogKey.Link   -> {
                                        try {
                                            context.startActivity(
                                                Intent(
                                                    Intent.ACTION_VIEW,
                                                    (dialogKey.value as DialogKey.Link).uri
                                                )
                                            )
                                        } catch (e: ActivityNotFoundException) {
                                            e.printStackTrace()
                                        }
                                    }

                                    is DialogKey.Action -> {
                                        if (Preferences[Preferences.Key.ActionLockDialog] != Preferences.ActionLock.None)
                                            neoActivity.launchLockPrompt {
                                                (dialogKey.value as DialogKey.Action).action()
                                                openDialog.value = false
                                            }
                                        else {
                                            (dialogKey.value as DialogKey.Action).action()
                                            openDialog.value = false
                                        }
                                    }

                                    else                -> {
                                        dialogKey.value = null
                                        openDialog.value = false
                                    }
                                }
                            },
                            onDismiss = {
                                dialogKey.value = null
                                openDialog.value = false
                            }
                        )
                    }
                }
            }
        }
    }
}
