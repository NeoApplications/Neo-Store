package com.looker.droidify.ui.fragments

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.looker.droidify.R
import com.looker.droidify.RELEASE_STATE_INSTALLED
import com.looker.droidify.RELEASE_STATE_NONE
import com.looker.droidify.RELEASE_STATE_SUGGESTED
import com.looker.droidify.content.Preferences
import com.looker.droidify.content.ProductPreferences
import com.looker.droidify.database.entity.Release
import com.looker.droidify.entity.ActionState
import com.looker.droidify.entity.AntiFeature
import com.looker.droidify.entity.DonateType
import com.looker.droidify.entity.DownloadState
import com.looker.droidify.entity.ProductPreference
import com.looker.droidify.entity.Screenshot
import com.looker.droidify.installer.AppInstaller
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.screen.MessageDialog
import com.looker.droidify.screen.ScreenshotsFragment
import com.looker.droidify.service.Connection
import com.looker.droidify.service.DownloadService
import com.looker.droidify.ui.activities.MainActivityX
import com.looker.droidify.ui.compose.components.ExpandableBlock
import com.looker.droidify.ui.compose.components.ScreenshotItem
import com.looker.droidify.ui.compose.components.ScreenshotList
import com.looker.droidify.ui.compose.components.SwitchPreference
import com.looker.droidify.ui.compose.pages.app_detail.components.AppInfoHeader
import com.looker.droidify.ui.compose.pages.app_detail.components.HtmlTextBlock
import com.looker.droidify.ui.compose.pages.app_detail.components.LinkItem
import com.looker.droidify.ui.compose.pages.app_detail.components.PermissionsItem
import com.looker.droidify.ui.compose.pages.app_detail.components.ReleaseItem
import com.looker.droidify.ui.compose.pages.app_detail.components.TopBarHeader
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.ui.compose.utils.Callbacks
import com.looker.droidify.ui.viewmodels.AppViewModelX
import com.looker.droidify.utility.Utils.rootInstallerEnabled
import com.looker.droidify.utility.Utils.startUpdate
import com.looker.droidify.utility.extension.android.Android
import com.looker.droidify.utility.extension.text.formatSize
import com.looker.droidify.utility.findSuggestedProduct
import com.looker.droidify.utility.generateLinks
import com.looker.droidify.utility.generatePermissionGroups
import com.looker.droidify.utility.isDarkTheme
import com.looker.droidify.utility.onLaunchClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO clean up and replace dropped functions from AppDetailFragment
class AppSheetX() : FullscreenBottomSheetDialogFragment(), Callbacks {
    companion object {
        private const val EXTRA_PACKAGE_NAME = "packageName"
    }

    constructor(packageName: String) : this() {
        arguments = Bundle().apply {
            putString(EXTRA_PACKAGE_NAME, packageName)
        }
    }

    val viewModel: AppViewModelX by viewModels {
        AppViewModelX.Factory(mainActivityX.db, packageName)
    }

    val mainActivityX: MainActivityX
        get() = requireActivity() as MainActivityX
    val packageName: String
        get() = requireArguments().getString(EXTRA_PACKAGE_NAME)!!

    private val downloadConnection = Connection(DownloadService::class.java, onBind = { _, binder ->
        binder.stateSubject
            .filter { it.packageName == packageName }
            .flowOn(Dispatchers.Default)
            .onEach { updateDownloadState(it) }
            .flowOn(Dispatchers.Main)
            .launchIn(lifecycleScope)
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        setupAdapters()
        return ComposeView(requireContext()).apply {
            setContent {
                AppTheme(
                    darkTheme = when (Preferences[Preferences.Key.Theme]) {
                        is Preferences.Theme.System -> isSystemInDarkTheme()
                        is Preferences.Theme.AmoledSystem -> isSystemInDarkTheme()
                        else -> isDarkTheme
                    }
                ) {
                    AppSheet()
                }
            }
        }
    }

    private fun setupAdapters() {
        downloadConnection.bind(requireContext())
    }

    override fun setupLayout() {
        viewModel._productRepos.observe(this) {
            lifecycleScope.launch {
                updateButtons()
            }
        }
    }

    override fun updateSheet() {
    }

    override fun onDestroyView() {
        super.onDestroyView()

        downloadConnection.unbind(requireContext())
    }

    private suspend fun updateButtons() {
        updateButtons(ProductPreferences[packageName])
    }

    // TODO rename to updateActions
    private suspend fun updateButtons(preference: ProductPreference) =
        withContext(Dispatchers.Default) {
            val installed = viewModel.installedItem.value
            val productRepos = viewModel.productRepos
            val product = findSuggestedProduct(productRepos, installed) { it.first }?.first
            val compatible = product != null && product.selectedReleases.firstOrNull()
                .let { it != null && it.incompatibilities.isEmpty() }
            val canInstall = product != null && installed == null && compatible
            val canUpdate =
                product != null && compatible && product.canUpdate(installed) &&
                        !preference.shouldIgnoreUpdate(product.versionCode)
            val canUninstall = product != null && installed != null && !installed.isSystem
            val canLaunch =
                product != null && installed != null && installed.launcherActivities.isNotEmpty()
            val canShare = product != null && productRepos[0].second.name == "F-Droid"

            val actions = mutableSetOf<ActionState>()
            launch {
                if (canInstall) {
                    actions += ActionState.Install
                }
            }
            launch {
                if (canUpdate) {
                    actions += ActionState.Update
                }
            }
            launch {
                if (canLaunch) {
                    actions += ActionState.Launch
                }
            }
            launch {
                if (installed != null) {
                    actions += ActionState.Details
                }
            }
            launch {
                if (canUninstall) {
                    actions += ActionState.Uninstall
                }
            }
            launch {
                if (canShare) {
                    actions += ActionState.Share
                }
            }
            // TODO prioritize actions set and choose the first for main others for extra actions
            val primaryAction = when {
                canUpdate -> ActionState.Update
                canLaunch -> ActionState.Launch
                canInstall -> ActionState.Install
                canShare -> ActionState.Share
                else -> null
            }
            val secondaryAction = when {
                primaryAction != ActionState.Share && canShare -> ActionState.Share
                primaryAction != ActionState.Launch && canLaunch -> ActionState.Launch
                installed != null && canUninstall -> ActionState.Uninstall
                else -> null
            }

            withContext(Dispatchers.Main) {
                viewModel.actions.value = actions

                if (viewModel.downloadState.value != null && viewModel.mainAction.value?.textId != viewModel.downloadState.value?.textId)
                    viewModel.downloadState.value?.let {
                        viewModel.mainAction.value = ActionState.Cancel(it.textId)
                    }
                else if (viewModel.downloadState.value == null) // && viewModel.mainAction.value != primaryAction)
                    viewModel.mainAction.value = primaryAction
                viewModel.secondaryAction.value = secondaryAction
            }
        }

    private suspend fun updateDownloadState(downloadState: DownloadService.State?) {
        val state = when (downloadState) {
            is DownloadService.State.Pending -> DownloadState.Pending
            is DownloadService.State.Connecting -> DownloadState.Connecting
            is DownloadService.State.Downloading -> DownloadState.Downloading(
                downloadState.read,
                downloadState.total
            )
            else -> null
        }
        viewModel.downloadState.value = state
        updateButtons()
        if (downloadState is DownloadService.State.Success && isResumed && !rootInstallerEnabled) {
            withContext(Dispatchers.Default) {
                AppInstaller.getInstance(context)?.defaultInstaller?.install(downloadState.release.cacheFileName)
            }
        }
    }

    override fun onActionClick(action: ActionState?) {
        val productRepos = viewModel.productRepos
        when (action) {
            ActionState.Install,
            ActionState.Update,
            -> {
                val installedItem = viewModel.installedItem.value
                lifecycleScope.launch {
                    startUpdate(
                        packageName,
                        installedItem,
                        productRepos,
                        downloadConnection
                    )
                }
                Unit
            }
            ActionState.Launch -> {
                viewModel.installedItem.value?.let {
                    requireContext().onLaunchClick(
                        it,
                        childFragmentManager
                    )
                }
                Unit
            }
            ActionState.Details -> {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:$packageName"))
                )
            }
            ActionState.Uninstall -> {
                lifecycleScope.launch {
                    AppInstaller.getInstance(context)?.defaultInstaller?.uninstall(packageName)
                }
                Unit
            }
            is ActionState.Cancel -> {
                // TODO fix cancel, send a cancel intent maybe?
                val binder = downloadConnection.binder
                if (viewModel.downloadState.value != null && binder != null) {
                    binder.cancel(packageName)
                } else Unit
            }
            ActionState.Share -> {
                shareIntent(packageName, productRepos[0].first.label)
            }
            else -> Unit
        }::class
    }

    private fun shareIntent(packageName: String, appName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        val extraText = if (Android.sdk(24)) {
            "https://www.f-droid.org/${resources.configuration.locales[0].language}/packages/${packageName}/"
        } else "https://www.f-droid.org/${resources.configuration.locale.language}/packages/${packageName}/"


        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TITLE, appName)
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, appName)
        shareIntent.putExtra(Intent.EXTRA_TEXT, extraText)

        startActivity(Intent.createChooser(shareIntent, "Where to Send?"))
    }

    override fun onPreferenceChanged(preference: ProductPreference) {
        lifecycleScope.launch { updateButtons(preference) }
    }

    override fun onPermissionsClick(group: String?, permissions: List<String>) {
        MessageDialog(MessageDialog.Message.Permissions(group, permissions)).show(
            childFragmentManager
        )
    }

    // TODO fix in compose implementation
    override fun onScreenshotClick(screenshot: Screenshot) {
        val pair = viewModel.productRepos.asSequence()
            .map { it ->
                Pair(
                    it.second,
                    it.first.screenshots.find { it === screenshot }?.identifier
                )
            }
            .filter { it.second != null }.firstOrNull()
        if (pair != null) {
            val (repository, identifier) = pair
            if (identifier != null) {
                ScreenshotsFragment(packageName, repository.id, identifier).show(
                    childFragmentManager
                )
            }
        }
    }

    override fun onReleaseClick(release: Release) {
        val installedItem = viewModel.installedItem.value
        when {
            release.incompatibilities.isNotEmpty() -> {
                MessageDialog(
                    MessageDialog.Message.ReleaseIncompatible(
                        release.incompatibilities,
                        release.platforms, release.minSdkVersion, release.maxSdkVersion
                    )
                ).show(childFragmentManager)
            }
            installedItem != null && installedItem.versionCode > release.versionCode -> {
                MessageDialog(MessageDialog.Message.ReleaseOlder).show(childFragmentManager)
            }
            installedItem != null && installedItem.signature != release.signature -> {
                MessageDialog(MessageDialog.Message.ReleaseSignatureMismatch).show(
                    childFragmentManager
                )
            }
            else -> {
                val productRepository =
                    viewModel.productRepos.asSequence()
                        .filter { it -> it.first.releases.any { it === release } }
                        .firstOrNull()
                if (productRepository != null) {
                    downloadConnection.binder?.enqueue(
                        packageName, productRepository.first.label,
                        productRepository.second, release
                    )
                }
            }
        }
    }

    override fun onUriClick(uri: Uri, shouldConfirm: Boolean): Boolean {
        return if (shouldConfirm && (uri.scheme == "http" || uri.scheme == "https")) {
            MessageDialog(MessageDialog.Message.Link(uri)).show(childFragmentManager)
            true
        } else {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, uri))
                true
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun copyLinkToClipboard(view: View, link: String) {
        val clipboardManager =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboardManager.setPrimaryClip(ClipData.newPlainText(null, link))
        Snackbar.make(view, R.string.link_copied_to_clipboard, Snackbar.LENGTH_SHORT).show()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppSheet() {
        val incompatible = Preferences[Preferences.Key.IncompatibleVersions]
        val installed by viewModel.installedItem.observeAsState()
        val products by viewModel.products.observeAsState()
        val repos by viewModel.repositories.observeAsState()
        val downloadState by viewModel.downloadState.observeAsState(null)
        val mainAction by viewModel.mainAction.observeAsState(if (installed == null) ActionState.Install else ActionState.Launch)
        val actions by viewModel.actions.observeAsState() // TODO add rest actions to UI
        val secondaryAction by viewModel.secondaryAction.observeAsState()
        val productRepos = products?.mapNotNull { product ->
            repos?.firstOrNull { it.id == product.repositoryId }
                ?.let { Pair(product, it) }
        } ?: emptyList()
        viewModel.productRepos = productRepos
        val suggestedProductRepo = findSuggestedProduct(productRepos, installed) { it.first }
        val compatibleReleasePairs = productRepos.asSequence()
            .flatMap { (product, repository) ->
                product.releases.asSequence()
                    .filter { incompatible || it.incompatibilities.isEmpty() }
                    .map { Pair(it, repository) }
            }
            .toList()
        val releaseItems = compatibleReleasePairs.asSequence()
            .map { (release, repository) ->
                Triple(
                    release,
                    repository,
                    when {
                        installed?.versionCode == release.versionCode && installed?.signature == release.signature -> RELEASE_STATE_INSTALLED
                        release.incompatibilities.firstOrNull() == null && release.selected && repository.id == suggestedProductRepo?.second?.id -> RELEASE_STATE_SUGGESTED
                        else -> RELEASE_STATE_NONE
                    }
                )
            }
            .sortedByDescending { it.first.versionCode }
            .toList()

        val imageData by remember(suggestedProductRepo) {
            mutableStateOf(
                suggestedProductRepo?.let {
                    CoilDownloader.createIconUri(
                        it.first.packageName,
                        it.first.icon,
                        it.first.metadataIcon,
                        it.second.address,
                        it.second.authentication
                    ).toString()
                }
            )
        }
        suggestedProductRepo?.let { (product, repo) ->
            Scaffold(
                // TODO add the topBar to the activity instead of the fragments
                topBar = {
                    TopBarHeader(
                        appName = product.label,
                        packageName = product.packageName,
                        icon = imageData,
                        state = downloadState
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    item {
                        AppInfoHeader(
                            versionCode = product.versionCode.toString(),
                            appSize = product.displayRelease?.size?.formatSize().orEmpty(),
                            appDev = product.author.name.replaceFirstChar { it.titlecase() },
                            mainAction = mainAction,
                            secondaryAction = secondaryAction,
                            possibleActions = actions?.filter { it != mainAction }?.toSet()
                                ?: emptySet(),
                            onSource = {
                                product.source.let { link ->
                                    if (link.isNotEmpty()) {
                                        requireContext().startActivity(
                                            Intent(Intent.ACTION_VIEW, link.toUri())
                                        )
                                    }
                                }
                            },
                            onSourceLong = {
                                product.source.let { link ->
                                    if (link.isNotEmpty()) {
                                        copyLinkToClipboard(
                                            requireActivity().window.decorView.rootView,
                                            link
                                        )
                                    }
                                }
                            },
                            onAction = { onActionClick(it) }
                        )
                    }
                    item {
                        AnimatedVisibility(visible = product.canUpdate(installed)) {
                            SwitchPreference(text = stringResource(id = R.string.ignore_this_update),
                                initSelected = ProductPreferences[product.packageName].ignoreVersionCode == product.versionCode,
                                onCheckedChanged = {
                                    ProductPreferences[product.packageName].let {
                                        it.copy(
                                            ignoreVersionCode =
                                            if (it.ignoreVersionCode == product.versionCode) 0 else product.versionCode
                                        )
                                    }
                                })
                        }
                    }
                    item {
                        AnimatedVisibility(visible = installed != null) {
                            SwitchPreference(text = stringResource(id = R.string.ignore_all_updates),
                                initSelected = ProductPreferences[product.packageName].ignoreVersionCode == product.versionCode,
                                onCheckedChanged = {
                                    ProductPreferences[product.packageName].let {
                                        it.copy(
                                            ignoreUpdates = !it.ignoreUpdates
                                        )
                                    }
                                })
                        }
                    }
                    if (Preferences[Preferences.Key.ShowScreenshots]) {
                        item {
                            ScreenshotList(screenShots = suggestedProductRepo.first.screenshots.map {
                                ScreenshotItem(
                                    screenShot = it,
                                    repository = repo,
                                    packageName = product.packageName
                                )
                            }) {
                                onScreenshotClick(it)
                            }
                        }
                    }
                    item {
                        // TODO add markdown parsing
                        if (product.description.isNotEmpty()) HtmlTextBlock(description = product.description)
                    }
                    val links = product.generateLinks(requireContext())
                    if (links.isNotEmpty()) {
                        item {
                            ExpandableBlock(
                                heading = stringResource(id = R.string.links),
                                positive = true,
                                preExpanded = true
                            ) {
                                links.forEach { link ->
                                    LinkItem(
                                        linkType = link,
                                        onClick = { it?.let { onUriClick(it, true) } },
                                        onLongClick = { link ->
                                            copyLinkToClipboard(
                                                requireActivity().window.decorView.rootView,
                                                link.toString()
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    if (product.donates.isNotEmpty()) {
                        item {
                            ExpandableBlock(
                                heading = stringResource(id = R.string.donate),
                                positive = true,
                                preExpanded = false
                            ) {
                                product.donates.forEach {
                                    LinkItem(linkType = DonateType(it, requireContext()),
                                        onClick = { link ->
                                            link?.let { onUriClick(it, true) }
                                        },
                                        onLongClick = { link ->
                                            copyLinkToClipboard(
                                                requireActivity().window.decorView.rootView,
                                                link.toString()
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        product.displayRelease?.generatePermissionGroups(requireContext())
                            ?.let { list ->
                                ExpandableBlock(
                                    heading = stringResource(id = R.string.permissions),
                                    positive = true,
                                    preExpanded = false
                                ) {
                                    if (list.isNotEmpty()) {
                                        list.forEach { p ->
                                            PermissionsItem(permissionsType = p) { group, permissions ->
                                                onPermissionsClick(group, permissions)
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(text = stringResource(id = R.string.no_permissions_identified))
                                        }
                                    }
                                }
                            }
                    }
                    if (product.antiFeatures.isNotEmpty()) {
                        item {
                            ExpandableBlock(
                                heading = stringResource(id = R.string.anti_features),
                                positive = false,
                                preExpanded = false
                            ) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = product.antiFeatures.map { af ->
                                        val titleId =
                                            AntiFeature.values().find { it.key == af }?.titleResId
                                        if (titleId != null) stringResource(id = titleId)
                                        else stringResource(id = R.string.unknown_FORMAT, af)
                                    }
                                        .joinToString(separator = "\n") { "\u2022 $it" }
                                )
                            }
                        }
                    }
                    if (product.whatsNew.isNotEmpty()) {
                        item {
                            ExpandableBlock(
                                heading = stringResource(id = R.string.changes),
                                positive = true,
                                preExpanded = true
                            ) {
                                HtmlTextBlock(
                                    description = product.whatsNew,
                                    isExpandable = false
                                )
                            }
                        }
                    }

                    items(items = releaseItems) {
                        ReleaseItem(
                            release = it.first,
                            repository = it.second,
                            releaseState = it.third
                        ) { release ->
                            onReleaseClick(release)
                        }
                    }
                }
            }
        }
    }
}
