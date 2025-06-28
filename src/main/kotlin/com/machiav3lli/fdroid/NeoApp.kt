package com.machiav3lli.fdroid

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.databaseModule
import com.machiav3lli.fdroid.data.entity.SyncRequest
import com.machiav3lli.fdroid.data.index.RepositoryUpdater
import com.machiav3lli.fdroid.data.repository.InstalledRepository
import com.machiav3lli.fdroid.data.repository.RepositoriesRepository
import com.machiav3lli.fdroid.data.repository.privacyModule
import com.machiav3lli.fdroid.manager.installer.BaseInstaller
import com.machiav3lli.fdroid.manager.installer.installerModule
import com.machiav3lli.fdroid.manager.network.CoilDownloader
import com.machiav3lli.fdroid.manager.network.Downloader
import com.machiav3lli.fdroid.manager.network.downloadClientModule
import com.machiav3lli.fdroid.manager.service.PackageChangedReceiver
import com.machiav3lli.fdroid.manager.work.BatchSyncWorker
import com.machiav3lli.fdroid.manager.work.WorkerManager
import com.machiav3lli.fdroid.manager.work.workmanagerModule
import com.machiav3lli.fdroid.utils.Utils.setLanguage
import com.machiav3lli.fdroid.utils.Utils.toInstalledItem
import com.machiav3lli.fdroid.utils.extension.android.Android
import io.ktor.client.engine.ProxyBuilder
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import org.koin.java.KoinJavaComponent.inject
import java.lang.ref.WeakReference
import java.net.Proxy

class NeoApp : Application(), SingletonImageLoader.Factory, KoinStartup {

    val db: DatabaseX by inject()
    lateinit var mActivity: AppCompatActivity
    val wm: WorkerManager by inject()
    val installedRepo: InstalledRepository by inject()
    val reposRepo: RepositoriesRepository by inject()

    companion object {
        val latestSyncs: MutableMap<Long, Long> = mutableMapOf()

        private var appRef: WeakReference<NeoApp> = WeakReference(null)
        private val neo_store: NeoApp get() = appRef.get()!!

        private var mainActivityRef: WeakReference<NeoActivity> = WeakReference(null)
        // TODO consider remove
        var mainActivity: NeoActivity?
            get() = mainActivityRef.get()
            set(mainActivity) {
                mainActivityRef = WeakReference(mainActivity)
            }

        val context: Context get() = neo_store.applicationContext

        val wm: WorkerManager get() = neo_store.wm
        val db: DatabaseX get() = neo_store.db
        val installer: BaseInstaller by inject(BaseInstaller::class.java)

        private val progress = mutableStateOf(Pair(false, 0f))

        fun setProgress(now: Int = 0, max: Int = 0) {
            if (max <= 0)
                progress.value = Pair(false, 0f)
            else
                progress.value = Pair(true, 1f * now / max)
        }
    }

    override fun onCreate() {
        super.onCreate()
        val ioScope = CoroutineScope(Dispatchers.IO)
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> Preferences[Preferences.Key.Theme] == Preferences.Theme.Dynamic }
                .build()
        )
        appRef = WeakReference(this)

        Preferences.init(this)
        RepositoryUpdater.init()
        ioScope.launch {
            listenApplications()
            listenPreferences()
        }

        wm.prune()
        Cache.cleanup(this)
        ioScope.launch {
            wm.updatePeriodicSyncJob(false)
        }
    }

    @KoinExperimentalAPI
    override fun onKoinStartup() = koinConfiguration {
        androidLogger()
        androidContext(this@NeoApp)
        modules(
            downloadClientModule,
            workmanagerModule,
            databaseModule,
            viewModelsModule,
            installerModule,
            privacyModule,
        )
    }

    private suspend fun listenApplications() {
        registerReceiver(
            PackageChangedReceiver(),
            IntentFilter().apply {
                addAction(Intent.ACTION_PACKAGE_ADDED)
                addAction(Intent.ACTION_PACKAGE_REMOVED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addDataScheme("package")
            }
        )
        val launcherActivitiesMap =
            packageManager
                .queryIntentActivities(
                    Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
                    0
                )
                .mapNotNull { resolveInfo -> resolveInfo.activityInfo }
                .groupBy { it.packageName }
                .mapNotNull { (packageName, activityInfos) ->
                    val aiNameLabels = activityInfos.mapNotNull {
                        val label = try {
                            it.loadLabel(packageManager).toString()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                        label?.let { label -> Pair(it.name, label) }
                    }
                    Pair(packageName, aiNameLabels)
                }.toMap()
        val installedItems = packageManager
            .getInstalledPackages(Android.PackageManager.signaturesFlag)
            .map { it.toInstalledItem(launcherActivitiesMap[it.packageName].orEmpty()) }
        withContext(Dispatchers.IO) {
            installedRepo.emptyTable()
            installedRepo.upsert(*installedItems.toTypedArray())
        }
    }

    private suspend fun listenPreferences() {
        updateProxy()
        withContext(Dispatchers.Default) {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ProxyType,
                    Preferences.Key.ProxyUrl,
                    Preferences.Key.ProxyHost,
                    Preferences.Key.ProxyPort,
                        -> updateProxy()

                    Preferences.Key.AutoSync,
                    Preferences.Key.AutoSyncInterval,
                        -> wm.updatePeriodicSyncJob(true)

                    Preferences.Key.UpdateUnstable,
                        -> forceSyncAll()

                    Preferences.Key.Theme,
                        -> {
                        launch(Dispatchers.Main) {
                            mActivity.recreate()
                        }
                    }

                    Preferences.Key.Language,
                        -> {
                        val refresh = Intent.makeRestartActivityTask(
                            ComponentName(
                                baseContext,
                                NeoActivity::class.java
                            )
                        )
                        applicationContext.startActivity(refresh)
                    }

                    else -> return@collect
                }
            }
        }
    }

    private fun updateProxy() {
        val type = Preferences[Preferences.Key.ProxyType].proxyType
        val url = Preferences[Preferences.Key.ProxyUrl]
        val host = Preferences[Preferences.Key.ProxyHost]
        val port = Preferences[Preferences.Key.ProxyPort]
        val proxy = when (type) {
            Proxy.Type.DIRECT -> {
                null
            }

            Proxy.Type.HTTP   -> {
                try {
                    ProxyBuilder.http(Url(url))
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }

            Proxy.Type.SOCKS  -> {
                try {
                    ProxyBuilder.socks(host, port)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        proxy?.let {
            Downloader.proxy = it
            CoilDownloader.proxy = it
        }
    }

    private suspend fun forceSyncAll() {
        withContext(Dispatchers.IO) {
            reposRepo.loadAll().forEach {
                if (it.lastModified.isNotEmpty() || it.entityTag.isNotEmpty()) {
                    reposRepo.upsert(
                        it.copy(
                            lastModified = "", entryLastModified = "",
                            entityTag = "", entryEntityTag = ""
                        )
                    )
                }
            }
            BatchSyncWorker.enqueue(SyncRequest.FORCE)
        }
    }

    override fun newImageLoader(context: Context): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory( // TODO migrate to Ktor on Coil 3.X
                        callFactory = CoilDownloader.Factory(Cache.getImagesDir(context))
                    )
                )
            }
            .crossfade(true)
            .build()
    }
}

class ContextWrapperX(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context): ContextWrapper {
            val config = context.setLanguage()
            return ContextWrapperX(context.createConfigurationContext(config))
        }
    }
}