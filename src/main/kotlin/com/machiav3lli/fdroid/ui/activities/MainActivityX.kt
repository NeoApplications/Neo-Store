package com.machiav3lli.fdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NAV_MAIN
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.SyncService
import com.machiav3lli.fdroid.ui.compose.components.ExpandableSearchAction
import com.machiav3lli.fdroid.ui.compose.components.TopBar
import com.machiav3lli.fdroid.ui.compose.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.fragments.AppSheetX
import com.machiav3lli.fdroid.ui.navigation.BottomNavBar
import com.machiav3lli.fdroid.ui.navigation.MainNavHost
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.setCustomTheme
import com.machiav3lli.fdroid.utility.showBatteryOptimizationDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class MainActivityX : AppCompatActivity() {
    companion object {
        const val ACTION_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.action.UPDATES"
        const val ACTION_INSTALL = "${BuildConfig.APPLICATION_ID}.intent.action.INSTALL"
        const val EXTRA_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.extra.UPDATES"
        const val EXTRA_CACHE_FILE_NAME =
            "${BuildConfig.APPLICATION_ID}.intent.extra.CACHE_FILE_NAME"
    }

    sealed class SpecialIntent {
        object Updates : SpecialIntent()
        class Install(val packageName: String?, val cacheFileName: String?) : SpecialIntent()
    }

    private lateinit var powerManager: PowerManager
    private val cScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    val syncConnection = Connection(SyncService::class.java)

    private val _searchQuery = MutableSharedFlow<String>()
    val searchQuery = _searchQuery.asSharedFlow()

    val db
        get() = (application as MainApplication).db

    private var currentTheme by Delegates.notNull<Int>()
    private var currentTab by Delegates.notNull<Int>()

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MainApplication).mActivity = this
        currentTheme = Preferences[Preferences.Key.Theme].getResId(resources.configuration)
        currentTab = Preferences[Preferences.Key.DefaultTab].getResId(resources.configuration)
        setCustomTheme()
        super.onCreate(savedInstanceState)

        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        if (savedInstanceState == null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            handleIntent(intent)
        }

        setContent {
            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
                    else -> isDarkTheme
                }
            ) {
                val query by searchQuery.collectAsState(initial = "")
                val navController = rememberAnimatedNavController()

                SideEffect {
                    cScope.launch { _searchQuery.emit("") }
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    bottomBar = { BottomNavBar(page = NAV_MAIN, navController = navController) },
                    topBar = {
                        TopBar(
                            title = stringResource(id = R.string.application_name),
                        ) {
                            ExpandableSearchAction(
                                query = query,
                                onClose = {
                                    cScope.launch { _searchQuery.emit("") }
                                },
                                onQueryChanged = { newQuery ->
                                    if (newQuery != query) {
                                        cScope.launch { _searchQuery.emit(newQuery) }
                                    }
                                }
                            )
                            TopBarAction(icon = Icons.Rounded.Sync) {
                                syncConnection.binder?.sync(SyncService.SyncRequest.MANUAL)
                            }
                            TopBarAction(icon = Icons.Rounded.Settings) {
                                navController.navigate(NavItem.Prefs.destination)
                            }
                        }
                    }
                ) { paddingValues ->
                    MainNavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        syncConnection.bind(this)
    }

    override fun onResume() {
        super.onResume()
        if (currentTheme != Preferences[Preferences.Key.Theme].getResId(resources.configuration))
            recreate()
        if (!powerManager.isIgnoringBatteryOptimizations(this.packageName) && !Preferences[Preferences.Key.IgnoreIgnoreBatteryOptimization])
            showBatteryOptimizationDialog()
    }

    override fun onNewIntent(intent: Intent?) {
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
                uri?.scheme == "market" && uri.host == "details" -> {
                    uri.getQueryParameter("id")?.nullIfEmpty()
                }
                uri != null && uri.scheme in setOf("http", "https") -> {
                    val host = uri.host.orEmpty()
                    if (host == "f-droid.org" || host.endsWith(".f-droid.org")) {
                        uri.lastPathSegment?.nullIfEmpty()
                    } else {
                        null
                    }
                }
                else -> {
                    null
                }
            }
        }

    private fun handleSpecialIntent(specialIntent: SpecialIntent) {
        when (specialIntent) {
            is SpecialIntent.Updates -> {
                // TODO directly update the apps??
                // binding.bottomNavigation.selectedItemId = R.id.installedTab TODO Fix
            }
            is SpecialIntent.Install -> {
                val packageName = specialIntent.packageName
                if (!packageName.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        specialIntent.cacheFileName?.let {
                            AppInstaller.getInstance(this@MainActivityX)
                                ?.defaultInstaller?.install(packageName, it)
                        }
                    }
                }
                Unit
            }
        }::class
    }

    private fun handleIntent(intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                val packageName = intent.packageName
                if (!packageName.isNullOrEmpty()) {
                    navigateProduct(packageName)
                }
            }
            ACTION_UPDATES -> handleSpecialIntent(SpecialIntent.Updates)
            ACTION_INSTALL -> handleSpecialIntent(
                SpecialIntent.Install(
                    intent.packageName,
                    intent.getStringExtra(EXTRA_CACHE_FILE_NAME)
                )
            )
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextWrapperX.wrap(newBase))
    }

    internal fun navigateProduct(packageName: String) {
        AppSheetX(packageName)
            .showNow(supportFragmentManager, "Product $packageName")
    }
}
