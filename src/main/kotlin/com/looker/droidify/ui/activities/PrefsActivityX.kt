package com.looker.droidify.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.looker.droidify.BuildConfig
import com.looker.droidify.ContextWrapperX
import com.looker.droidify.MainApplication
import com.looker.droidify.R
import com.looker.droidify.content.Preferences
import com.looker.droidify.databinding.ActivityPrefsXBinding
import com.looker.droidify.installer.AppInstaller
import com.looker.droidify.service.Connection
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.fragments.MainNavFragmentX
import com.looker.droidify.ui.fragments.Source
import com.looker.droidify.utility.extension.text.nullIfEmpty
import kotlinx.coroutines.launch

// TODO clean up the bloat
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
    }

    lateinit var binding: ActivityPrefsXBinding
    lateinit var toolbar: MaterialToolbar
    lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    private val syncConnection = Connection(SyncService::class.java, onBind = { _, _ ->
        navController.currentDestination?.let {
            val source = Source.values()[when (it.id) {
                R.id.updateTab -> 1
                R.id.otherTab -> 2
                else -> 0 // R.id.userTab
            }]
            updateUpdateNotificationBlocker(source)
        }
    })

    val db
        get() = (application as MainApplication).db

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Preferences[Preferences.Key.Theme].getResId(resources.configuration))
        super.onCreate(savedInstanceState)

        binding = ActivityPrefsXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        toolbar = binding.toolbar

        if (savedInstanceState == null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            handleIntent(intent)
        }
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        setSupportActionBar(toolbar)
        toolbar.isFocusableInTouchMode = true
        binding.collapsingToolbar.title = getString(R.string.settings)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_content) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.userTab, R.id.updateTab, R.id.otherTab)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        supportFragmentManager.addFragmentOnAttachListener { _, _ ->
            hideKeyboard()
        }
        syncConnection.bind(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow((currentFocus ?: window.decorView).windowToken, 0)
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
            is SpecialIntent.Updates -> navController.navigate(R.id.installedTab)
            is SpecialIntent.Install -> {
                val packageName = specialIntent.packageName
                if (!packageName.isNullOrEmpty()) {
                    lifecycleScope.launch {
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

    private fun updateUpdateNotificationBlocker(activeSource: Source) {
        val blockerFragment = if (activeSource == Source.UPDATES) {
            supportFragmentManager.fragments.asSequence().mapNotNull { it as? MainNavFragmentX }
                .find { it.source == activeSource }
        } else {
            null
        }
        syncConnection.binder?.setUpdateNotificationBlocker(blockerFragment)
    }
}
