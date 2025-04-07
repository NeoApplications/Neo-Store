package com.machiav3lli.fdroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.repository.DownloadedRepository
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.navigation.AppNavHost
import com.machiav3lli.fdroid.ui.navigation.NavRoute
import com.machiav3lli.fdroid.utils.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utils.extension.text.pathCropped
import com.machiav3lli.fdroid.utils.isBiometricLockEnabled
import com.machiav3lli.fdroid.utils.isDarkTheme
import com.machiav3lli.fdroid.viewmodels.AppSheetVM
import com.machiav3lli.fdroid.viewmodels.MainVM
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import kotlin.properties.Delegates

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
class NeoActivity : AppCompatActivity() {
    companion object {
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
    private val cScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val mScope: CoroutineScope = CoroutineScope(Dispatchers.Main)

    val db: DatabaseX by inject()

    private var currentTheme by Delegates.notNull<Int>()
    private val mainViewModel: MainVM by viewModel()
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
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
                    else -> isDarkTheme
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private val Intent.packageName: String?
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
                prefsViewModel.setIntent(
                    specialIntent.address,
                    specialIntent.fingerprint,
                )
                navController.navigate(NavRoute.Prefs(2))
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
                    cScope.launch { showSearchPage(query ?: "") }
                } else {
                    val packageName = intent.packageName
                    cScope.launch {
                        if (!packageName.isNullOrEmpty()
                            && db.getProductDao().exists(packageName)
                        ) navigateProduct(packageName)
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
                    intent.packageName,
                    intent.getStringExtra(EXTRA_CACHE_FILE_NAME)
                )
            )
        }
    }


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val scan = result.data?.getStringExtra("SCAN_RESULT")
                scan?.replace("fdroidrepo", "http")
                intent.data = Uri.parse(scan)
                intent.action = Intent.ACTION_VIEW
                handleIntent(intent)
            }
        }

    fun openScanner() {
        intent.putExtra(EXTRA_INTENT_HANDLED, false)
        resultLauncher.launch(Intent(INTENT_ACTION_BINARY_EYE))
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextWrapperX.wrap(newBase))
    }

    internal fun navigateProduct(packageName: String) {
        cScope.launch {
            mainViewModel.setNavigatorRole(ListDetailPaneScaffoldRole.Detail, packageName)
        }
    }

    private fun showSearchPage(query: String? = null) {
        mScope.launch {
            mainViewModel.setNavigatorRole(ListDetailPaneScaffoldRole.List)
            navController.navigate(NavRoute.Main(Preferences.DefaultTab.Search.index))
            mainViewModel.setSearchQuery(query ?: "")
        }
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
    singleOf(::DownloadedRepository)
    viewModelOf(::MainVM)
    viewModelOf(::PrefsVM)
    viewModelOf(::AppSheetVM)
}
