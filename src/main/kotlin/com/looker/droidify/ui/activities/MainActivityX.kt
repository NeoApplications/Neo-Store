package com.looker.droidify.ui.activities

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
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
import com.looker.droidify.database.CursorOwner
import com.looker.droidify.database.QueryLoader
import com.looker.droidify.databinding.ActivityMainXBinding
import com.looker.droidify.installer.AppInstaller
import com.looker.droidify.screen.*
import com.looker.droidify.service.Connection
import com.looker.droidify.service.SyncService
import com.looker.droidify.ui.fragments.MainNavFragmentX
import com.looker.droidify.ui.fragments.Source
import com.looker.droidify.ui.viewmodels.MainActivityViewModelX
import com.looker.droidify.utility.extension.android.Android
import com.looker.droidify.utility.extension.text.nullIfEmpty
import kotlinx.coroutines.launch

class MainActivityX : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {
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

    lateinit var binding: ActivityMainXBinding
    lateinit var toolbar: MaterialToolbar
    lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val viewModel: MainActivityViewModelX by viewModels()

    private val syncConnection = Connection(SyncService::class.java, onBind = { _, _ ->
        navController.currentDestination?.let {
            val source = Source.values()[when (it.id) {
                R.id.latestTab -> 1
                R.id.installedTab -> 2
                else -> 0 // R.id.exploreTab
            }]
            updateUpdateNotificationBlocker(source)
        }
    })

    val db
        get() = (application as MainApplication).db

    lateinit var cursorOwner: CursorOwner
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(Preferences[Preferences.Key.Theme].getResId(resources.configuration))
        super.onCreate(savedInstanceState)

        binding = ActivityMainXBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        toolbar = binding.toolbar

        if (savedInstanceState == null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
            handleIntent(intent)
        }
        setContentView(binding.root)

        if (savedInstanceState == null) {
            cursorOwner = CursorOwner()
            supportFragmentManager.beginTransaction()
                .add(cursorOwner, CursorOwner::class.java.name)
                .commit()
        } else {
            cursorOwner = supportFragmentManager
                .findFragmentByTag(CursorOwner::class.java.name) as CursorOwner
        }

        setSupportActionBar(toolbar)

        if (Android.sdk(28) && !Android.Device.isHuaweiEmui) {
            toolbar.menu.setGroupDividerEnabled(true)
        }

        toolbar.isFocusableInTouchMode = true
        binding.collapsingToolbar.title = getString(R.string.application_name)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_content) as NavHostFragment
        navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.exploreTab, R.id.latestTab, R.id.installedTab)
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        supportFragmentManager.addFragmentOnAttachListener { _, _ ->
            hideKeyboard()
        }
    }

    override fun onStart() {
        super.onStart()
        syncConnection.bind(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun hideKeyboard() {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow((currentFocus ?: window.decorView).windowToken, 0)
    }

    fun syncManual(item: MenuItem) {
        syncConnection.binder?.sync(SyncService.SyncRequest.MANUAL)
    }

    fun navigateSettings(item: MenuItem) {
        navigateSettings()
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
        // TODO
    }

    private fun navigateSettings() {
        // TODO
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

    fun attachCursorOwner(callback: CursorOwner.Callback, request: CursorOwner.Request) {
        val oldActiveRequest = viewModel.activeRequests[request.id]
        if (oldActiveRequest?.callback != null &&
            oldActiveRequest.callback != callback && oldActiveRequest.cursor != null
        ) {
            oldActiveRequest.callback.onCursorData(oldActiveRequest.request, null)
        }
        val cursor = if (oldActiveRequest?.request == request && oldActiveRequest.cursor != null) {
            callback.onCursorData(request, oldActiveRequest.cursor)
            oldActiveRequest.cursor
        } else {
            null
        }
        viewModel.activeRequests[request.id] = CursorOwner.ActiveRequest(request, callback, cursor)
        if (cursor == null) {
            LoaderManager.getInstance(this).restartLoader(request.id, null, this)
        }
    }


    fun detachCursorOwner(callback: CursorOwner.Callback) {
        for (id in viewModel.activeRequests.keys) {
            val activeRequest = viewModel.activeRequests[id]!!
            if (activeRequest.callback == callback) {
                viewModel.activeRequests[id] = activeRequest.copy(callback = null)
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        val request = viewModel.activeRequests[id]!!.request
        return QueryLoader(this) {
            when (request) {
                is CursorOwner.Request.ProductsAvailable -> db.productDao
                    .query(
                        installed = false,
                        updates = false,
                        searchQuery = request.searchQuery,
                        section = request.section,
                        order = request.order,
                        signal = it
                    )
                is CursorOwner.Request.ProductsInstalled -> db.productDao
                    .query(
                        installed = true,
                        updates = false,
                        searchQuery = request.searchQuery,
                        section = request.section,
                        order = request.order,
                        signal = it
                    )
                is CursorOwner.Request.ProductsUpdates -> db.productDao
                    .query(
                        installed = true,
                        updates = true,
                        searchQuery = request.searchQuery,
                        section = request.section,
                        order = request.order,
                        signal = it
                    )
                is CursorOwner.Request.Repositories -> db.repositoryDao.allCursor
            }
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        val activeRequest = viewModel.activeRequests[loader.id]
        if (activeRequest != null) {
            viewModel.activeRequests[loader.id] = activeRequest.copy(cursor = data)
            activeRequest.callback?.onCursorData(activeRequest.request, data)
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) = onLoadFinished(loader, null)
}
