package com.machiav3lli.fdroid.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.machiav3lli.fdroid.BuildConfig
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.EXTRA_INTENT_HANDLED
import com.machiav3lli.fdroid.INTENT_ACTION_BINARY_EYE
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.ui.components.TopBar
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.ui.navigation.PagerNavBar
import com.machiav3lli.fdroid.ui.navigation.PrefsNavHost
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.extension.text.pathCropped
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.setCustomTheme
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PrefsActivityX : AppCompatActivity() {
    companion object {
        const val ACTION_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.action.UPDATES"
        const val ACTION_INSTALL = "${BuildConfig.APPLICATION_ID}.intent.action.INSTALL"
        const val EXTRA_CACHE_FILE_NAME =
            "${BuildConfig.APPLICATION_ID}.intent.extra.CACHE_FILE_NAME"
    }

    sealed class SpecialIntent {
        object Updates : SpecialIntent()
        class Install(val packageName: String?, val cacheFileName: String?) : SpecialIntent()
        class AddRepo(val address: String?, val fingerprint: String?) : SpecialIntent()
    }

    private lateinit var navController: NavHostController
    private val cScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    @OptIn(ExperimentalFoundationApi::class)
    private lateinit var pagerState: PagerState

    val db
        get() = (application as MainApplication).db

    val prefsViewModel: PrefsVM by viewModels {
        PrefsVM.Factory(db)
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MainApplication).mActivity = this
        setCustomTheme()
        MainApplication.prefsActivity = this
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
                    else -> isDarkTheme
                }
            ) {
                navController = rememberNavController()
                val pages = listOf(
                    NavItem.PersonalPrefs,
                    NavItem.UpdatesPrefs,
                    NavItem.ReposPrefs,
                    NavItem.OtherPrefs,
                )
                pagerState = rememberPagerState(pageCount = { pages.size })
                val currentPage by remember(pagerState.currentPage) { mutableStateOf(pages[pagerState.currentPage]) }

                Scaffold(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    bottomBar = { PagerNavBar(pageItems = pages, pagerState = pagerState) },
                    topBar = {
                        TopBar(title = stringResource(id = currentPage.title))
                    }
                ) { paddingValues ->
                    LaunchedEffect(key1 = navController) {
                        if (savedInstanceState == null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                            handleIntent(intent)
                        }
                    }

                    PrefsNavHost(
                        modifier = Modifier.padding(paddingValues),
                        navController = navController,
                        pagerState = pagerState,
                        pages = pages,
                    )
                }
            }
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith("finishAfterTransition()"))
    override fun onBackPressed() {
        finishAfterTransition()
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

    @OptIn(ExperimentalFoundationApi::class)
    private fun handleSpecialIntent(specialIntent: SpecialIntent) {
        when (specialIntent) {
            is SpecialIntent.AddRepo -> {
                prefsViewModel.setIntent(
                    specialIntent.address,
                    specialIntent.fingerprint,
                )
                if (pagerState.currentPage != 2) cScope.launch {
                    pagerState.scrollToPage(2)
                }
                Unit
            }

            is SpecialIntent.Updates -> navController.navigate(NavItem.Installed.destination) // TODO
            is SpecialIntent.Install -> {
                val packageName = specialIntent.packageName
                if (!packageName.isNullOrEmpty()) {
                    cScope.launch {
                        specialIntent.cacheFileName?.let {
                            AppInstaller.getInstance(this@PrefsActivityX)
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
                val data = intent.data
                if (data != null && !intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
                    intent.putExtra(EXTRA_INTENT_HANDLED, true)
                    val (addressText, fingerprintText) = try {
                        val uri = data.buildUpon()
                            .scheme("https")
                            .path(data.path?.pathCropped)
                            .query(null).fragment(null).build().toString()
                        val fingerprintText =
                            data.getQueryParameter("fingerprint")?.uppercase()?.nullIfEmpty()
                                ?: data.getQueryParameter("FINGERPRINT")?.uppercase()?.nullIfEmpty()
                        Pair(uri, fingerprintText)
                    } catch (e: Exception) {
                        Pair(null, null)
                    }
                    handleSpecialIntent(
                        SpecialIntent.AddRepo(
                            addressText,
                            fingerprintText
                        )
                    )
                }
            }

            ACTION_UPDATES     -> handleSpecialIntent(SpecialIntent.Updates)
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


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
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
}
