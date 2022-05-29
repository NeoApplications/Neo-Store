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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.content.ProductPreferences
import com.looker.droidify.database.entity.Release
import com.looker.droidify.entity.Cancelable
import com.looker.droidify.entity.Connecting
import com.looker.droidify.entity.Details
import com.looker.droidify.entity.Install
import com.looker.droidify.entity.Launch
import com.looker.droidify.entity.PackageState
import com.looker.droidify.entity.Pending
import com.looker.droidify.entity.ProductPreference
import com.looker.droidify.entity.Screenshot
import com.looker.droidify.entity.Share
import com.looker.droidify.entity.Uninstall
import com.looker.droidify.entity.Update
import com.looker.droidify.installer.AppInstaller
import com.looker.droidify.screen.MessageDialog
import com.looker.droidify.screen.ScreenshotsFragment
import com.looker.droidify.service.Connection
import com.looker.droidify.service.DownloadService
import com.looker.droidify.ui.activities.MainActivityX
import com.looker.droidify.ui.compose.theme.AppTheme
import com.looker.droidify.ui.compose.utils.Callbacks
import com.looker.droidify.ui.viewmodels.AppViewModelX
import com.looker.droidify.utility.Utils.rootInstallerEnabled
import com.looker.droidify.utility.Utils.startUpdate
import com.looker.droidify.utility.extension.android.Android
import com.looker.droidify.utility.findSuggestedProduct
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

    private var downloading = false
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

            val actions = mutableSetOf<PackageState>()
            launch {
                if (canInstall) {
                    actions += Install
                }
            }
            launch {
                if (canUpdate) {
                    actions += Update
                }
            }
            launch {
                if (canLaunch) {
                    actions += Launch
                }
            }
            launch {
                if (installed != null) {
                    actions += Details
                }
            }
            launch {
                if (canUninstall) {
                    actions += Uninstall
                }
            }
            launch {
                if (canShare) {
                    actions += Share
                }
            }
            val primaryAction = when {
                canUpdate -> Update
                canLaunch -> Launch
                canInstall -> Install
                canShare -> Share
                else -> null
            }
            val secondaryAction = when {
                primaryAction != Share && canShare -> Share
                primaryAction != Launch && canLaunch -> Launch
                installed != null && canUninstall -> Uninstall
                else -> null
            }

            withContext(Dispatchers.Main) {
                viewModel.actions.value = actions
                if (!downloading) {
                    viewModel.state.value = primaryAction
                    viewModel.secondaryAction.value = secondaryAction
                } else {
                    viewModel.secondaryAction.value = null
                }
            }
        }

    private suspend fun updateDownloadState(state: DownloadService.State?) {
        val status = when (state) {
            is DownloadService.State.Pending -> Pending
            is DownloadService.State.Connecting -> Connecting
            is DownloadService.State.Downloading -> com.looker.droidify.entity.Downloading(
                state.read,
                state.total
            )
            else -> null
        }
        val downloading = status is Cancelable
        this.downloading = downloading
        updateButtons()
        viewModel.state.value = status
        if (state is DownloadService.State.Success && isResumed && !rootInstallerEnabled) {
            withContext(Dispatchers.Default) {
                AppInstaller.getInstance(context)?.defaultInstaller?.install(state.release.cacheFileName)
            }
        }
    }

    override fun onActionClick(action: PackageState?) {
        val productRepos = viewModel.productRepos
        when (action) {
            Install,
            Update,
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
            Launch -> {
                viewModel.installedItem.value?.let {
                    requireContext().onLaunchClick(
                        it,
                        childFragmentManager
                    )
                }
                Unit
            }
            Details -> {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:$packageName"))
                )
            }
            Uninstall -> {
                lifecycleScope.launch {
                    AppInstaller.getInstance(context)?.defaultInstaller?.uninstall(packageName)
                }
                Unit
            }
            is Cancelable -> {
                val binder = downloadConnection.binder
                if (downloading && binder != null) {
                    binder.cancel(packageName)
                } else Unit
            }
            Share -> {
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

    }
}
