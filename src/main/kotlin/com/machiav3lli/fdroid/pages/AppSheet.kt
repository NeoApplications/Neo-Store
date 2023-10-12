package com.machiav3lli.fdroid.pages

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.RELEASE_STATE_INSTALLED
import com.machiav3lli.fdroid.RELEASE_STATE_NONE
import com.machiav3lli.fdroid.RELEASE_STATE_SUGGESTED
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.entity.ActionState
import com.machiav3lli.fdroid.entity.AntiFeature
import com.machiav3lli.fdroid.entity.DialogKey
import com.machiav3lli.fdroid.entity.DonateType
import com.machiav3lli.fdroid.entity.PrivacyNote
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.network.createIconUri
import com.machiav3lli.fdroid.service.ActionReceiver
import com.machiav3lli.fdroid.service.worker.DownloadWorker
import com.machiav3lli.fdroid.ui.components.ScreenshotList
import com.machiav3lli.fdroid.ui.components.SwitchPreference
import com.machiav3lli.fdroid.ui.components.appsheet.AppInfoChips
import com.machiav3lli.fdroid.ui.components.appsheet.AppInfoHeader
import com.machiav3lli.fdroid.ui.components.appsheet.CardButton
import com.machiav3lli.fdroid.ui.components.appsheet.HtmlTextBlock
import com.machiav3lli.fdroid.ui.components.appsheet.LinkItem
import com.machiav3lli.fdroid.ui.components.appsheet.PrivacyPanel
import com.machiav3lli.fdroid.ui.components.appsheet.ReleaseItem
import com.machiav3lli.fdroid.ui.components.appsheet.TopBarHeader
import com.machiav3lli.fdroid.ui.components.appsheet.WarningCard
import com.machiav3lli.fdroid.ui.components.privacy.MeterIconsBar
import com.machiav3lli.fdroid.ui.components.privacy.PrivacyCard
import com.machiav3lli.fdroid.ui.components.privacy.PrivacyItemBlock
import com.machiav3lli.fdroid.ui.components.toScreenshotItem
import com.machiav3lli.fdroid.ui.compose.ProductsHorizontalRecycler
import com.machiav3lli.fdroid.ui.compose.icons.Icon
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.icon.Opensource
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyleft
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Copyright
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GlobeSimple
import com.machiav3lli.fdroid.ui.compose.utils.blockBorder
import com.machiav3lli.fdroid.ui.dialog.ActionSelectionDialogUI
import com.machiav3lli.fdroid.ui.dialog.BaseDialog
import com.machiav3lli.fdroid.ui.dialog.KeyDialogUI
import com.machiav3lli.fdroid.utility.Utils.startUpdate
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.findSuggestedProduct
import com.machiav3lli.fdroid.utility.generateLinks
import com.machiav3lli.fdroid.utility.shareIntent
import com.machiav3lli.fdroid.utility.shareReleaseIntent
import com.machiav3lli.fdroid.utility.startLauncherActivity
import com.machiav3lli.fdroid.viewmodels.AppSheetVM
import kotlinx.coroutines.launch
import kotlin.math.truncate

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AppSheet(
    viewModel: AppSheetVM,
    packageName: String,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val neoActivity = context as NeoActivity
    val scope = rememberCoroutineScope()
    val includeIncompatible = Preferences[Preferences.Key.IncompatibleVersions]
    val showScreenshots = remember { mutableStateOf(false) }
    val openDialog = remember { mutableStateOf(false) }
    val dialogKey: MutableState<DialogKey?> = remember { mutableStateOf(null) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    var screenshotPage by remember { mutableIntStateOf(0) }
    val screenshotsPageState = rememberModalBottomSheetState(true)
    val installed by viewModel.installedItem.collectAsState(null)
    val isInstalled by remember(installed) {
        mutableStateOf(installed != null)
    }
    val products by viewModel.products.collectAsState(null)
    val exodusInfo by viewModel.exodusInfo.collectAsState(null)
    val privacyNote by viewModel.privacyNote.collectAsState(PrivacyNote())
    val authorProducts by viewModel.authorProducts.collectAsState(null)
    val repos by viewModel.repositories.collectAsState(null)
    val downloadState by viewModel.downloadingState.collectAsState()
    val mainAction by viewModel.mainAction.collectAsState()
    val actions by viewModel.subActions.collectAsState()
    val extras by viewModel.extras.collectAsState()
    val productRepos = products?.mapNotNull { product ->
        repos?.firstOrNull { it.id == product.repositoryId }
            ?.let { Pair(product, it) }
    } ?: emptyList()
    viewModel.updateProductRepos(productRepos)
    val suggestedProductRepo = findSuggestedProduct(productRepos, installed) { it.first }
    val compatibleReleasePairs = productRepos.asSequence()
        .flatMap { (product, repository) ->
            product.releases.asSequence()
                .filter { includeIncompatible || it.incompatibilities.isEmpty() }
                .map { Pair(it, repository) }
        }
        .toList()
    val releaseItems = compatibleReleasePairs.asSequence()
        .map { (release, repository) ->
            Triple(
                release,
                repository,
                when {
                    installed?.versionCode == release.versionCode && installed?.signature == release.signature                               -> RELEASE_STATE_INSTALLED
                    release.incompatibilities.firstOrNull() == null && release.selected && repository.id == suggestedProductRepo?.second?.id -> RELEASE_STATE_SUGGESTED
                    else                                                                                                                     -> RELEASE_STATE_NONE
                }
            )
        }
        .sortedByDescending { it.first.versionCode }
        .toList()

    val imageData by remember(suggestedProductRepo) {
        mutableStateOf(
            suggestedProductRepo?.let {
                createIconUri(
                    it.first.packageName,
                    it.first.icon,
                    it.first.metadataIcon,
                    it.second.address,
                    it.second.authentication
                ).toString()
            }
        )
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
        val installedItem = viewModel.installedItem.value
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
                    && installedItem.signature != release.signature
                    && !Preferences[Preferences.Key.DisableSignatureCheck]       -> {
                dialogKey.value = DialogKey.ReleaseIssue(R.string.incompatible_signature_DESC)
                openDialog.value = true
            }

            else                                                                 -> {
                val productRepository =
                    viewModel.productRepos.value.asSequence()
                        .filter { it.first.releases.any { rel -> rel === release } }
                        .firstOrNull()
                if (productRepository != null) {
                    DownloadWorker.enqueue(
                        packageName,
                        productRepository.first.label,
                        productRepository.second,
                        release
                    )
                }
            }
        }
    }

    val onActionClick = { action: ActionState? ->
        val productRepos = viewModel.productRepos.value
        when (action) {
            ActionState.Install,
            ActionState.Update,
                                  -> {
                val installedItem = viewModel.installedItem.value
                scope.launch {
                    startUpdate(
                        packageName,
                        installedItem,
                        productRepos
                    )
                }
                Unit
            }

            ActionState.Launch    -> {
                viewModel.installedItem.value?.let { installed ->
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

                Unit
            }

            ActionState.Details   -> {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:$packageName"))
                )
            }

            ActionState.Uninstall -> {
                scope.launch {
                    AppInstaller.getInstance(MainApplication.mainActivity)?.defaultInstaller
                        ?.uninstall(packageName)
                }
                Unit
            }

            is ActionState.Cancel -> {
                val cancelIntent = Intent(context, ActionReceiver::class.java).apply {
                    this.action = ActionReceiver.COMMAND_CANCEL_DOWNLOAD
                    putExtra(ARG_PACKAGE_NAME, packageName)
                }
                neoActivity.sendBroadcast(cancelIntent)
            }

            ActionState.Share     -> {
                context.shareIntent(
                    packageName,
                    productRepos[0].first.label,
                    productRepos[0].second.name
                )
            }

            ActionState.Bookmark,
            ActionState.Bookmarked,
                                  -> {
                viewModel.setFavorite(packageName, action is ActionState.Bookmark)
            }

            else                  -> Unit
        }::class
    }

    suggestedProductRepo?.let { (product, repo) ->
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    TopBarHeader(
                        appName = product.label,
                        packageName = product.packageName,
                        icon = imageData,
                        state = downloadState,
                        actions = {
                            CardButton(
                                icon = when {
                                    privacyNote.sourceType.isFree            -> Phosphor.Copyleft
                                    privacyNote.sourceType.isOpenSource      -> Icon.Opensource
                                    privacyNote.sourceType.isSourceAvailable -> Phosphor.Copyright
                                    else                                     -> Phosphor.GlobeSimple
                                },
                                description = stringResource(id = R.string.source_code),
                                onClick = {
                                    onUriClick(
                                        Uri.parse(product.source.nullIfEmpty() ?: product.web),
                                        true
                                    )
                                },
                                onLongClick = {
                                    product.source.let { link ->
                                        if (link.isNotEmpty()) copyLinkToClipboard(link)

                                    }
                                }
                            )
                        },
                    )
                    AppInfoChips(
                        product = product,
                        latestRelease = product.displayRelease,
                        installed = installed,
                    )
                    MeterIconsBar(
                        modifier = Modifier.fillMaxWidth(),
                        selectedTrackers = if (exodusInfo != null) truncate((privacyNote.trackersNote - 1) / 20f).toInt()
                        else null,
                        selectedPermissions = truncate((privacyNote.permissionsNote - 1) / 20f).toInt(),
                        pagerState,
                    ) {
                        scope.launch {
                            pagerState.animateScrollToPage(
                                if (pagerState.currentPage == 0) 1
                                else 0
                            )
                        }
                    }
                }
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding() - 4.dp,
                        start = paddingValues.calculateStartPadding(LayoutDirection.Ltr),
                        end = paddingValues.calculateEndPadding(LayoutDirection.Ltr),
                    )
                    .blockBorder(),
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
                                mainAction = mainAction,
                                possibleActions = actions.filter { it != mainAction }.toSet(),
                                onAction = { onActionClick(it) }
                            )
                        }
                        item {
                            AnimatedVisibility(visible = product.canUpdate(installed)) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.ignore_this_update),
                                    initSelected = { extras?.ignoredVersion == product.versionCode },
                                    onCheckedChanged = {
                                        viewModel.setIgnoredVersion(
                                            product.packageName,
                                            if (it) product.versionCode else 0
                                        )
                                    }
                                )
                            }
                            AnimatedVisibility(visible = isInstalled) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.ignore_all_updates),
                                    initSelected = { extras?.ignoreUpdates == true },
                                    onCheckedChanged = {
                                        viewModel.setIgnoreUpdates(product.packageName, it)
                                    }
                                )
                            }
                            AnimatedVisibility(visible = isInstalled) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.ignore_vulns),
                                    initSelected = { extras?.ignoreVulns == true },
                                    onCheckedChanged = {
                                        viewModel.setIgnoreVulns(product.packageName, it)
                                    }
                                )
                            }
                            AnimatedVisibility(visible = isInstalled) {
                                SwitchPreference(
                                    text = stringResource(id = R.string.allow_unstable_updates),
                                    initSelected = { extras?.allowUnstable == true },
                                    onCheckedChanged = {
                                        viewModel.setAllowUnstableUpdates(product.packageName, it)
                                    }
                                )
                            }
                        }
                        if (Preferences[Preferences.Key.ShowScreenshots]) { // TODO add optional screenshots button
                            item {
                                ScreenshotList(screenShots = suggestedProductRepo.first.screenshots.map {
                                    it.toScreenshotItem(
                                        repository = repo,
                                        packageName = product.packageName
                                    )
                                }) { index ->
                                    screenshotPage = index
                                    showScreenshots.value = true
                                }
                            }
                        }
                        item { // TODO add markdown parsing
                            if ((product.description + product.summary).isNotEmpty()) HtmlTextBlock(
                                shortText = product.summary,
                                longText = product.description
                            )
                        }
                        val links = product.generateLinks(context)
                        item {
                            if ((links + product.donates + product.whatsNew + authorProducts).isNotEmpty()) PrivacyCard(
                                preExpanded = true,
                            ) {
                                if (links.isNotEmpty()) {
                                    PrivacyItemBlock(heading = stringResource(id = R.string.links)) {
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
                                if (product.donates.isNotEmpty()) {
                                    PrivacyItemBlock(heading = stringResource(id = R.string.donate)) {
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
                                if (!authorProducts.isNullOrEmpty()) {
                                    PrivacyItemBlock(
                                        heading = stringResource(
                                            id = R.string.other_apps_by,
                                            product.author.name
                                        ),
                                    ) {
                                        Log.i(
                                            "author products",
                                            authorProducts?.map { it.author }?.joinToString()
                                                .orEmpty()
                                        )
                                        ProductsHorizontalRecycler(
                                            productsList = authorProducts,
                                            repositories = repos?.associateBy { repo -> repo.id }
                                                ?: emptyMap(),
                                            rowsNumber = 1,
                                        ) { item ->
                                            neoActivity.navigateProduct(item.packageName)
                                        }
                                    }
                                }
                                if (product.whatsNew.isNotEmpty()) {
                                    PrivacyItemBlock(
                                        heading = stringResource(id = R.string.changes),
                                        preExpanded = true,
                                    ) {
                                        HtmlTextBlock(shortText = product.whatsNew)
                                    }
                                }
                            }
                        }
                        item {
                            Text(
                                text = stringResource(
                                    id = if (releaseItems.isEmpty()) R.string.no_releases
                                    else R.string.releases
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 14.dp)
                            )
                        }
                        items(items = releaseItems) { item ->
                            ReleaseItem(
                                release = item.first,
                                repository = item.second,
                                releaseState = item.third,
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
                        product,
                        copyLinkToClipboard,
                        onUriClick,
                    )
                }
            }

            if (showScreenshots.value) {
                ModalBottomSheet(
                    sheetState = screenshotsPageState,
                    containerColor = Color.Transparent,
                    dragHandle = null,
                    onDismissRequest = {
                        scope.launch { screenshotsPageState.hide() }
                        showScreenshots.value = false
                    },
                    shape = RectangleShape,
                ) {
                    ScreenshotsPage(
                        screenshots = suggestedProductRepo.first.screenshots.map {
                            it.toScreenshotItem(
                                repository = repo,
                                packageName = product.packageName
                            )
                        },
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
                                    is DialogKey.Link -> {
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

                                    else              -> {
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
