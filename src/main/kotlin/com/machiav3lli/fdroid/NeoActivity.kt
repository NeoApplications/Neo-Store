package com.machiav3lli.fdroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.data.repository.ExtrasRepository
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.InstallsRepository
import com.machiav3lli.fdroid.data.repository.PrivacyRepository
import com.machiav3lli.fdroid.data.repository.ProductsRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.navigation.AppNavHost
import com.machiav3lli.fdroid.ui.navigation.NavRoute
import com.machiav3lli.fdroid.utils.InstallUtils
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.extension.text.pathCropped
import com.machiav3lli.fdroid.utils.isBiometricLockEnabled
import com.machiav3lli.fdroid.utils.isDarkTheme
import com.machiav3lli.fdroid.viewmodels.AppPageVM
import com.machiav3lli.fdroid.viewmodels.ExploreVM
import com.machiav3lli.fdroid.viewmodels.InstalledVM
import com.machiav3lli.fdroid.viewmodels.LatestVM
import com.machiav3lli.fdroid.viewmodels.MainVM
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import com.machiav3lli.fdroid.viewmodels.RepoPageVM
import com.machiav3lli.fdroid.viewmodels.SearchVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.properties.Delegates

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class NeoActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "NeoActivity"
        const val ACTION_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.action.UPDATES"
        const val ACTION_INSTALL = "${BuildConfig.APPLICATION_ID}.intent.action.INSTALL"
        const val EXTRA_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.extra.UPDATES"
        const val EXTRA_CACHE_FILE_NAME =
            "${BuildConfig.APPLICATION_ID}.intent.extra.CACHE_FILE_NAME"
    }

    sealed class SpecialIntent {
        data object Updates : SpecialIntent()
        class Install(val packageName: String?, val cacheFileName: String?) : SpecialIntent()
        class AddRepo(val address: String?, val fingerprint: String?) : SpecialIntent()
    }

    private lateinit var navController: NavHostController

    private var currentTheme by Delegates.notNull<Int>()
    private val mainViewModel: MainVM by viewModel()
    private val searchViewModel: SearchVM by viewModel()
    private val prefsViewModel: PrefsVM by viewModel()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as NeoApp).mActivity = this
        currentTheme = Preferences[Preferences.Key.Theme].resId
        NeoApp.mainActivity = this
        super.onCreate(savedInstanceState)

        setContent {
            DisposableEffect(Preferences[Preferences.Key.Theme]) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { isDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        Color.TRANSPARENT,
                        Color.TRANSPARENT,
                    ) { isDarkTheme },
                )
                onDispose {}
            }

            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System      -> isSystemInDarkTheme()
                    is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
                    else                             -> isDarkTheme
                }
            ) {
                navController = rememberNavController()

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    LaunchedEffect(key1 = navController) {
                        if (savedInstanceState == null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                            handleIntent(intent)
                        }
                    }

                    AppNavHost(
                        modifier = Modifier.imePadding(),
                        navController = navController,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentTheme != Preferences[Preferences.Key.Theme].resId)
            recreate()
        lifecycleScope.launch {
            if (!InstallUtils.restartOrphanedInstallTasks()) {
                Log.d(TAG, "Install task restart was throttled")
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private val Intent.packageNameFromURI: String?
        get() {
            val uri = data
            return when {
                uri?.scheme == "package" || uri?.scheme == "fdroid.app" -> {
                    uri.schemeSpecificPart?.nullIfEmpty()
                }

                uri?.scheme == "market" && uri.host == "details"        -> {
                    uri.getQueryParameter("id")?.nullIfEmpty()
                }

                uri != null && uri.scheme in setOf("http", "https")     -> {
                    val host = uri.host.orEmpty()
                    if (host == "f-droid.org" || host.endsWith(".f-droid.org")) {
                        uri.lastPathSegment?.nullIfEmpty()
                    } else if (host == "apt.izzysoft.de") {
                        uri.lastPathSegment?.nullIfEmpty()
                    } else {
                        null
                    }
                }

                else                                                    -> {
                    null
                }
            }
        }

    private fun handleSpecialIntent(specialIntent: SpecialIntent) {
        when (specialIntent) {
            is SpecialIntent.Updates -> {
                // TODO directly update the apps??
                navController.navigate(NavRoute.Main(Preferences.DefaultTab.Installed.index))
            }

            is SpecialIntent.AddRepo -> {
                navController.navigate(NavRoute.Prefs(2))
                prefsViewModel.setIntent(
                    specialIntent.address,
                    specialIntent.fingerprint,
                )
            }

            is SpecialIntent.Install -> {
                val packageName = specialIntent.packageName
                if (!packageName.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        specialIntent.cacheFileName?.let {
                            NeoApp.installer.install(packageName, it)
                        }
                    }
                }
                Unit
            }
        }::class
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data
        val host = data?.host
        val fingerprintText = if (data?.isOpaque == true) null
        else data?.getQueryParameter("fingerprint")?.uppercase()?.nullIfEmpty()
            ?: data?.getQueryParameter("FINGERPRINT")?.uppercase()?.nullIfEmpty()

        // TODO Handle Intent.ACTION_APPLICATION_PREFERENCES (android.intent.action.APPLICATION_PREFERENCES)
        when (intent?.action) {
            Intent.ACTION_VIEW          -> {
                if (
                    data != null
                    && fingerprintText != null
                    && !intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)
                ) {
                    intent.putExtra(EXTRA_INTENT_HANDLED, true)
                    val (repoAddress, repoFingerprint) = try {
                        val uri = data.buildUpon()
                            .scheme("https")
                            .path(data.path?.pathCropped)
                            .query(null).fragment(null).build().toString()
                        Pair(uri, fingerprintText)
                    } catch (e: Exception) {
                        Pair(null, null)
                    }
                    handleSpecialIntent(
                        SpecialIntent.AddRepo(
                            repoAddress,
                            repoFingerprint
                        )
                    )
                } else if (host == "search") {
                    val query = data.getQueryParameter("q")
                    showSearchPage(query ?: "")
                } else {
                    val packageName = intent.packageNameFromURI
                    runBlocking(Dispatchers.IO) {
                        if (!packageName.isNullOrEmpty() && mainViewModel.productExist(packageName))
                            navigateProduct(packageName)
                        else showSearchPage(packageName)
                    }
                }
            }

            Intent.ACTION_SHOW_APP_INFO -> {
                intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME)
                    ?.takeIf { it.isNotBlank() }
                    ?.let { navigateProduct(it) }
            }

            ACTION_UPDATES              -> { // TODO Handle EXTRA_UPDATES
                if (!intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
                    intent.putExtra(EXTRA_INTENT_HANDLED, true)
                    handleSpecialIntent(SpecialIntent.Updates)
                }
            }

            ACTION_INSTALL              -> handleSpecialIntent(
                SpecialIntent.Install(
                    intent.packageNameFromURI,
                    intent.getStringExtra(EXTRA_CACHE_FILE_NAME)
                )
            )
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextWrapperX.wrap(newBase))
    }

    internal fun navigateProduct(packageName: String) {
        mainViewModel.setNavigatorRole(ListDetailPaneScaffoldRole.Detail, packageName)
    }

    private fun showSearchPage(query: String? = null) {
        mainViewModel.setNavigatorRole(ListDetailPaneScaffoldRole.List)
        searchViewModel.setSearchQuery(query ?: "")
    }

    fun launchLockPrompt(action: () -> Unit) {
        try {
            val biometricPrompt = createBiometricPrompt(action)
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.action_lock_device))
                .setConfirmationRequired(true)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL or (
                            if (NeoApp.context.isBiometricLockEnabled()) BiometricManager.Authenticators.BIOMETRIC_WEAK
                            else 0
                            )
                )
                .build()
            biometricPrompt.authenticate(promptInfo)
        } catch (e: Throwable) {
            action()
        }
    }

    private fun createBiometricPrompt(action: () -> Unit): BiometricPrompt {
        return BiometricPrompt(
            this,
            ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    action()
                }
            })
    }
}

val viewModelsModule = module {
    singleOf(::RepositoriesRepository)
    singleOf(::ProductsRepository)
    singleOf(::InstalledRepository)
    singleOf(::InstallsRepository)
    singleOf(::DownloadedRepository)
    singleOf(::ExtrasRepository)
    singleOf(::PrivacyRepository)
    viewModelOf(::MainVM)
    viewModelOf(::LatestVM)
    viewModelOf(::ExploreVM)
    viewModelOf(::SearchVM)
    viewModelOf(::InstalledVM)
    viewModelOf(::PrefsVM)
    viewModelOf(::AppPageVM)
    viewModelOf(::RepoPageVM)
}
