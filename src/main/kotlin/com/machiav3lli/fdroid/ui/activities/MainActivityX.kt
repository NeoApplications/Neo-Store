package com.machiav3lli.fdroid.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.EXTRA_INTENT_HANDLED
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.pages.AppSheet
import com.machiav3lli.fdroid.service.worker.SyncRequest
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.ui.components.ExpandableSearchAction
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.components.TopBarAction
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowsClockwise
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GearSix
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.navigation.MainNavHost
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.PagerNavBar
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.setCustomTheme
import com.machiav3lli.fdroid.viewmodels.AppSheetVM
import com.machiav3lli.fdroid.viewmodels.ExploreVM
import com.machiav3lli.fdroid.viewmodels.InstalledVM
import com.machiav3lli.fdroid.viewmodels.LatestVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    private lateinit var navController: NavHostController
    private val cScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private lateinit var expanded: MutableState<Boolean>
    private lateinit var appSheetPackage: MutableState<String>

    val db
        get() = (application as MainApplication).db

    private var currentTheme by Delegates.notNull<Int>()

    val exploreViewModel: ExploreVM by viewModels {
        ExploreVM.Factory(db)
    }
    val latestViewModel: LatestVM by viewModels {
        LatestVM.Factory(db)
    }
    val installedViewModel: InstalledVM by viewModels {
        InstalledVM.Factory(db)
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MainApplication).mActivity = this
        currentTheme = Preferences[Preferences.Key.Theme].resId
        setCustomTheme()
        MainApplication.mainActivity = this
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
                    else -> isDarkTheme
                }
            ) {
                val query by searchQuery.collectAsState(initial = "")
                expanded = remember {
                    mutableStateOf(false)
                }
                val mScope = rememberCoroutineScope()
                navController = rememberNavController()
                var barVisible by remember { mutableStateOf(false) }

                navController.addOnDestinationChangedListener { _, destination, _ ->
                    barVisible = destination.route != NavItem.Permissions.destination
                }
                val pages = listOf(
                    NavItem.Latest,
                    NavItem.Explore,
                    NavItem.Installed,
                )
                val pagerState = rememberPagerState(
                    pageCount = { pages.size },
                    initialPage = Preferences[Preferences.Key.DefaultTab].valueString.toInt(),
                )
                appSheetPackage = remember { mutableStateOf("") }
                val appSheetState = rememberModalBottomSheetState(true)
                val appSheetVM = remember(appSheetPackage.value) {
                    AppSheetVM(
                        MainApplication.db,
                        appSheetPackage.value,
                    )
                }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    bottomBar = {
                        AnimatedVisibility(
                            barVisible,
                            enter = slideInVertically { height -> height },
                            exit = slideOutVertically { height -> height },
                        ) {
                            PagerNavBar(pageItems = pages, pagerState = pagerState)
                        }
                    },
                    topBar = {
                        TopBar(title = stringResource(id = R.string.application_name)) {
                            AnimatedVisibility(barVisible) {
                                ExpandableSearchAction(
                                    query = query,
                                    expanded = expanded,
                                    onClose = {
                                        mScope.launch { _searchQuery.emit("") }
                                    },
                                    onQueryChanged = { newQuery ->
                                        if (newQuery != query) {
                                            mScope.launch { _searchQuery.emit(newQuery) }
                                        }
                                    }
                                )
                            }
                            AnimatedVisibility(barVisible && !expanded.value) {
                                TopBarAction(
                                    icon = Phosphor.ArrowsClockwise,
                                    description = stringResource(id = R.string.sync_repositories)
                                ) {
                                    SyncWorker.enqueueAll(SyncRequest.MANUAL)
                                }
                            }
                            AnimatedVisibility(barVisible && !expanded.value) {
                                TopBarAction(
                                    icon = Phosphor.GearSix,
                                    description = stringResource(id = R.string.settings)
                                ) {
                                    navController.navigate(NavItem.Prefs.destination)
                                }
                            }
                        }
                    }
                ) { paddingValues ->
                    LaunchedEffect(key1 = navController) {
                        if (savedInstanceState == null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                            handleIntent(intent)
                        }
                    }

                    MainNavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        pagerState = pagerState,
                        pages = pages,
                    )

                    if (appSheetPackage.value.isNotEmpty()) {
                        ModalBottomSheet(
                            sheetState = appSheetState,
                            containerColor = MaterialTheme.colorScheme.background,
                            scrimColor = Color.Transparent,
                            onDismissRequest = {
                                mScope.launch { appSheetState.hide() }
                                appSheetPackage.value = ""
                            }
                        ) {

                            AppSheet(
                                appSheetVM,
                                appSheetPackage.value,
                            ) {
                                mScope.launch { appSheetState.hide() }
                                appSheetPackage.value = ""
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentTheme != Preferences[Preferences.Key.Theme].resId)
            recreate()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when {
            keyCode == KeyEvent.KEYCODE_BACK && expanded.value -> {
                cScope.launch { _searchQuery.emit("") }
                expanded.value = false
                true
            }

            keyCode == KeyEvent.KEYCODE_BACK                   -> moveTaskToBack(true)
            else                                               -> super.onKeyDown(keyCode, event)
        }
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

                uri?.scheme == "market" && uri.host == "details"        -> {
                    uri.getQueryParameter("id")?.nullIfEmpty()
                }

                uri != null && uri.scheme in setOf("http", "https")     -> {
                    val host = uri.host.orEmpty()
                    if (host == "f-droid.org" || host.endsWith(".f-droid.org")) {
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
                navController.navigate(NavItem.Installed.destination)
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
        val data = intent?.data
        val host = data?.host

        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                if (host == "search") {
                    val query = data.getQueryParameter("q")
                    cScope.launch { _searchQuery.emit(query ?: "") }
                } else {
                    val packageName = intent.packageName
                    if (!packageName.isNullOrEmpty()) {
                        navigateProduct(packageName)
                    }
                }
            }

            ACTION_UPDATES     -> { // TODO Handle EXTRA_UPDATES
                if (!intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
                    intent.putExtra(EXTRA_INTENT_HANDLED, true)
                    handleSpecialIntent(SpecialIntent.Updates)
                }
            }

            ACTION_INSTALL     -> handleSpecialIntent(
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
        appSheetPackage.value = packageName
    }
}
