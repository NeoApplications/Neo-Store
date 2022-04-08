package com.looker.droidify.ui.fragments

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.looker.droidify.R
import com.looker.droidify.content.ProductPreferences
import com.looker.droidify.database.entity.Product
import com.looker.droidify.database.entity.Release
import com.looker.droidify.database.entity.Repository
import com.looker.droidify.databinding.SheetAppXBinding
import com.looker.droidify.entity.ProductPreference
import com.looker.droidify.entity.Screenshot
import com.looker.droidify.installer.AppInstaller
import com.looker.droidify.screen.MessageDialog
import com.looker.droidify.screen.ScreenshotsFragment
import com.looker.droidify.service.Connection
import com.looker.droidify.service.DownloadService
import com.looker.droidify.ui.activities.MainActivityX
import com.looker.droidify.ui.adapters.AppDetailAdapter
import com.looker.droidify.ui.viewmodels.AppViewModelX
import com.looker.droidify.utility.Utils.rootInstallerEnabled
import com.looker.droidify.utility.Utils.startUpdate
import com.looker.droidify.utility.extension.android.Android
import com.looker.droidify.utility.findSuggestedProduct
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// TODO clean up and replace dropped functions from AppDetailFragment
class AppSheetX() : FullscreenBottomSheetDialogFragment(), AppDetailAdapter.Callbacks {
    companion object {
        private const val EXTRA_PACKAGE_NAME = "packageName"
        private const val STATE_ADAPTER = "adapter"
    }

    constructor(packageName: String) : this() {
        arguments = Bundle().apply {
            putString(EXTRA_PACKAGE_NAME, packageName)
        }
    }

    private lateinit var binding: SheetAppXBinding
    val viewModel: AppViewModelX by viewModels {
        AppViewModelX.Factory(mainActivityX.db, packageName)
    }

    val mainActivityX: MainActivityX
        get() = requireActivity() as MainActivityX
    val packageName: String
        get() = requireArguments().getString(EXTRA_PACKAGE_NAME)!!

    private var actions = Pair(emptySet<Action>(), null as Action?)
    private var productRepos = emptyList<Pair<Product, Repository>>()
    private var products = emptyList<Product>()
    private var installed: Installed? = null
    private var downloading = false

    private var productDisposable: Disposable? = null
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
        binding = SheetAppXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setupAdapters()
        return binding.root
    }

    private fun setupAdapters() {
        downloadConnection.bind(requireContext())

        binding.recyclerView.apply {
            id = android.R.id.list
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = AppDetailAdapter(this@AppSheetX)
        }
    }

    override fun setupLayout() {
        // TODO simplify observing and updating
        viewModel.installedItem.observe(requireActivity()) {
            installed = it?.let {
                val isSystem = try {
                    ((requireContext().packageManager.getApplicationInfo(packageName, 0).flags)
                            and ApplicationInfo.FLAG_SYSTEM) != 0
                } catch (e: Exception) {
                    false
                }
                val launcherActivities =
                    if (packageName != context?.packageName && context != null) {
                        val packageManager = requireContext().packageManager
                        packageManager
                            .queryIntentActivities(
                                Intent(Intent.ACTION_MAIN).addCategory(
                                    Intent.CATEGORY_LAUNCHER
                                ), 0
                            )
                            .asSequence()
                            .mapNotNull { resolveInfo -> resolveInfo.activityInfo }
                            .filter { activityInfo -> activityInfo.packageName == packageName }
                            .mapNotNull { activityInfo ->
                                val label = try {
                                    activityInfo.loadLabel(packageManager).toString()
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    null
                                }
                                label?.let { labelName ->
                                    Pair(
                                        activityInfo.name,
                                        labelName
                                    )
                                }
                            }
                            .toList()
                    } else {
                        // Don't allow to launch self
                        emptyList()
                    }
                Installed(it, isSystem, launcherActivities)
            }
            updateSheet()
        }

        viewModel.repositories.observe(viewLifecycleOwner) {
            if (it.isNotEmpty() && products.isNotEmpty()) updateSheet()
        }
        viewModel.products.observe(viewLifecycleOwner) {
            products = it.filterNotNull()
            viewModel.repositories.value?.let { repos ->
                if (repos.isNotEmpty() && products.isNotEmpty()) updateSheet()
            }
        }
    }

    override fun updateSheet() {
        products.mapNotNull { product ->
            viewModel.repositories.value
                ?.firstOrNull { it.id == product.repositoryId }
                ?.let { Pair(product, it) }
        }.apply {
            productRepos = this

            val adapter = binding.recyclerView.adapter as AppDetailAdapter
            adapter.setProducts(
                binding.recyclerView.context,
                packageName,
                this,
                installed?.data
            )
            lifecycleScope.launch {
                updateButtons()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        productDisposable?.dispose()
        productDisposable = null
        downloadConnection.unbind(requireContext())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val adapterState = (binding.recyclerView.adapter as? AppDetailAdapter)?.saveState()
        adapterState?.let { outState.putParcelable(STATE_ADAPTER, it) }
    }

    private suspend fun updateButtons() {
        updateButtons(ProductPreferences[packageName])
    }

    private suspend fun updateButtons(preference: ProductPreference) =
        withContext(Dispatchers.Default) {
            val installed = installed
            val product = findSuggestedProduct(productRepos, installed?.data) { it.first }?.first
            val compatible = product != null && product.selectedReleases.firstOrNull()
                .let { it != null && it.incompatibilities.isEmpty() }
            val canInstall = product != null && installed == null && compatible
            val canUpdate =
                product != null && compatible && product.canUpdate(installed?.data) &&
                        !preference.shouldIgnoreUpdate(product.versionCode)
            val canUninstall = product != null && installed != null && !installed.isSystem
            val canLaunch =
                product != null && installed != null && installed.launcherActivities.isNotEmpty()
            val canShare = product != null && productRepos[0].second.name == "F-Droid"

            val actions = mutableSetOf<Action>()
            launch {
                if (canInstall) {
                    actions += Action.INSTALL
                }
            }
            launch {
                if (canUpdate) {
                    actions += Action.UPDATE
                }
            }
            launch {
                if (canLaunch) {
                    actions += Action.LAUNCH
                }
            }
            launch {
                if (installed != null) {
                    actions += Action.DETAILS
                }
            }
            launch {
                if (canUninstall) {
                    actions += Action.UNINSTALL
                }
            }
            launch {
                if (canShare) {
                    actions += Action.SHARE
                }
            }
            val primaryAction = when {
                canUpdate -> Action.UPDATE
                canLaunch -> Action.LAUNCH
                canInstall -> Action.INSTALL
                canShare -> Action.SHARE
                else -> null
            }
            val secondaryAction = when {
                primaryAction != Action.SHARE && canShare -> Action.SHARE
                primaryAction != Action.LAUNCH && canLaunch -> Action.LAUNCH
                installed != null && canUninstall -> Action.UNINSTALL
                else -> null
            }

            launch(Dispatchers.Main) {
                val adapterAction =
                    if (downloading) AppDetailAdapter.Action.CANCEL else primaryAction?.adapterAction
                val adapterSecondaryAction =
                    if (downloading) null else secondaryAction?.adapterAction
                (binding.recyclerView.adapter as? AppDetailAdapter)
                    ?.setAction(adapterAction)
                (binding.recyclerView.adapter as? AppDetailAdapter)
                    ?.setSecondaryAction(adapterSecondaryAction)
            }
            launch { this@AppSheetX.actions = Pair(actions, primaryAction) }
        }

    private suspend fun updateDownloadState(state: DownloadService.State?) {
        val status = when (state) {
            is DownloadService.State.Pending -> AppDetailAdapter.Status.Pending
            is DownloadService.State.Connecting -> AppDetailAdapter.Status.Connecting
            is DownloadService.State.Downloading -> AppDetailAdapter.Status.Downloading(
                state.read,
                state.total
            )
            is DownloadService.State.Success, is DownloadService.State.Error, is DownloadService.State.Cancel, null -> null
        }
        val downloading = status != null
        if (this.downloading != downloading) {
            this.downloading = downloading
            updateButtons()
        }
        (binding.recyclerView.adapter as? AppDetailAdapter)?.setStatus(status)
        if (state is DownloadService.State.Success && isResumed && !rootInstallerEnabled) {
            withContext(Dispatchers.Default) {
                AppInstaller.getInstance(context)?.defaultInstaller?.install(state.release.cacheFileName)
            }
        }
    }

    override fun onActionClick(action: AppDetailAdapter.Action) {
        when (action) {
            AppDetailAdapter.Action.INSTALL,
            AppDetailAdapter.Action.UPDATE,
            -> {
                val installedItem = installed?.data
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
            AppDetailAdapter.Action.LAUNCH -> {
                val launcherActivities = installed?.launcherActivities.orEmpty()
                if (launcherActivities.size >= 2) {
                    LaunchDialog(launcherActivities).show(
                        childFragmentManager,
                        LaunchDialog::class.java.name
                    )
                } else {
                    launcherActivities.firstOrNull()?.let { startLauncherActivity(it.first) }
                }
                Unit
            }
            AppDetailAdapter.Action.DETAILS -> {
                startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:$packageName"))
                )
            }
            AppDetailAdapter.Action.UNINSTALL -> {
                lifecycleScope.launch {
                    AppInstaller.getInstance(context)?.defaultInstaller?.uninstall(packageName)
                }
                Unit
            }
            AppDetailAdapter.Action.CANCEL -> {
                val binder = downloadConnection.binder
                if (downloading && binder != null) {
                    binder.cancel(packageName)
                } else Unit
            }
            AppDetailAdapter.Action.SHARE -> {
                shareIntent(packageName, productRepos[0].first.label)
            }
        }::class
    }

    private fun startLauncherActivity(name: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_LAUNCHER)
                    .setComponent(ComponentName(packageName, name))
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    override fun onScreenshotClick(screenshot: Screenshot) {
        val pair = productRepos.asSequence()
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
        val installedItem = installed?.data
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
                    productRepos.asSequence()
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

    class LaunchDialog() : DialogFragment() {
        companion object {
            private const val EXTRA_NAMES = "names"
            private const val EXTRA_LABELS = "labels"
        }

        constructor(launcherActivities: List<Pair<String, String>>) : this() {
            arguments = Bundle().apply {
                putStringArrayList(EXTRA_NAMES, ArrayList(launcherActivities.map { it.first }))
                putStringArrayList(EXTRA_LABELS, ArrayList(launcherActivities.map { it.second }))
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog {
            val names = requireArguments().getStringArrayList(EXTRA_NAMES)!!
            val labels = requireArguments().getStringArrayList(EXTRA_LABELS)!!
            return MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.launch)
                .setItems(labels.toTypedArray()) { _, position ->
                    (parentFragment as AppSheetX)
                        .startLauncherActivity(names[position])
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
        }
    }

    private enum class Action(
        val id: Int,
        val adapterAction: AppDetailAdapter.Action,
    ) {
        INSTALL(1, AppDetailAdapter.Action.INSTALL),
        UPDATE(2, AppDetailAdapter.Action.UPDATE),
        LAUNCH(3, AppDetailAdapter.Action.LAUNCH),
        DETAILS(4, AppDetailAdapter.Action.DETAILS),
        UNINSTALL(5, AppDetailAdapter.Action.UNINSTALL),
        SHARE(6, AppDetailAdapter.Action.SHARE)
    }

    private class Installed(
        val data: com.looker.droidify.database.entity.Installed, val isSystem: Boolean,
        val launcherActivities: List<Pair<String, String>>,
    )
}
