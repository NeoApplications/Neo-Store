package com.machiav3lli.fdroid

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.work.NetworkType
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.anggrayudi.storage.extension.postToUi
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.fdroid.data.content.Cache
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.DatabaseX
import com.machiav3lli.fdroid.data.database.databaseModule
import com.machiav3lli.fdroid.data.index.RepositoryUpdater
import com.machiav3lli.fdroid.manager.installer.AppInstaller
import com.machiav3lli.fdroid.manager.installer.BaseInstaller
import com.machiav3lli.fdroid.manager.installer.installerModule
import com.machiav3lli.fdroid.manager.network.CoilDownloader
import com.machiav3lli.fdroid.manager.network.Downloader
import com.machiav3lli.fdroid.manager.network.downloadClientModule
import com.machiav3lli.fdroid.manager.network.exodusModule
import com.machiav3lli.fdroid.manager.service.PackageChangedReceiver
import com.machiav3lli.fdroid.manager.work.WorkerManager
import com.machiav3lli.fdroid.manager.work.BatchSyncWorker
import com.machiav3lli.fdroid.data.entity.SyncRequest
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
import java.lang.ref.WeakReference
import java.net.Proxy

class NeoApp : Application(), SingletonImageLoader.Factory, KoinStartup {

    val db: DatabaseX by inject()
    lateinit var mActivity: AppCompatActivity
    val wm: WorkerManager by inject()
    val installer: AppInstaller by inject()

    companion object {
        val enqueuedInstalls: MutableSet<String> = mutableSetOf()
        val latestSyncs: MutableMap<Long, Long> = mutableMapOf()

        private var appRef: WeakReference<NeoApp> = WeakReference(null)
        private val neo_store: NeoApp get() = appRef.get()!!

        private var mainActivityRef: WeakReference<NeoActivity> = WeakReference(null)
        var mainActivity: NeoActivity? // TODO make private
            get() = mainActivityRef.get()
            set(mainActivity) {
                mainActivityRef = WeakReference(mainActivity)
            }

        val context: Context get() = neo_store.applicationContext

        val wm: WorkerManager get() = neo_store.wm
        val db: DatabaseX get() = neo_store.db
        val installer: BaseInstaller get() = neo_store.installer.defaultInstaller

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
        RepositoryUpdater.init(this)
        ioScope.launch {
            listenApplications()
            listenPreferences()
        }

        wm.prune()
        Cache.cleanup(this)
        ioScope.launch {
            updatePeriodicSyncJob(false)
        }
    }

    @KoinExperimentalAPI
    override fun onKoinStartup() = koinConfiguration {
        androidLogger()
        androidContext(this@NeoApp)
        modules(
            exodusModule,
            downloadClientModule,
            workmanagerModule,
            databaseModule,
            viewModelsModule,
            installerModule,
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
            db.getInstalledDao().emptyTable()
            db.getInstalledDao().put(*installedItems.toTypedArray())
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
                                                   -> {
                        updateProxy()
                    }

                    Preferences.Key.AutoSync,
                    Preferences.Key.AutoSyncInterval,
                                                   -> {
                        updatePeriodicSyncJob(true)
                    }

                    Preferences.Key.UpdateUnstable -> {
                        forceSyncAll()
                    }

                    Preferences.Key.Theme          -> {
                        postToUi { mActivity.recreate() }
                    }

                    Preferences.Key.Language       -> {
                        val refresh = Intent.makeRestartActivityTask(
                            ComponentName(
                                baseContext,
                                NeoActivity::class.java
                            )
                        )
                        applicationContext.startActivity(refresh)
                    }

                    else                           -> return@collect
                }
            }
        }
    }

    private suspend fun updatePeriodicSyncJob(force: Boolean) = withContext(Dispatchers.IO) {
        val wm = NeoApp.wm.workManager
        val reschedule =
            force || wm.getWorkInfosForUniqueWork(TAG_BATCH_SYNC_PERIODIC).get().isEmpty()
        if (reschedule) {
            when (val autoSync = Preferences[Preferences.Key.AutoSync]) {
                is Preferences.AutoSync.Never  -> {
                    wm.cancelUniqueWork(TAG_BATCH_SYNC_PERIODIC)
                    Log.i(this::javaClass.name, "Canceled next auto-sync run.")
                }

                is Preferences.AutoSync.Wifi,
                is Preferences.AutoSync.WifiBattery,
                                               -> {
                    autoSync(
                        connectionType = NetworkType.UNMETERED,
                        chargingBattery = autoSync is Preferences.AutoSync.WifiBattery,
                    )
                }

                is Preferences.AutoSync.Battery,
                                               -> {
                    autoSync(
                        connectionType = NetworkType.CONNECTED,
                        chargingBattery = true,
                    )
                }

                is Preferences.AutoSync.Always -> {
                    autoSync(
                        connectionType = NetworkType.CONNECTED
                    )
                }
            }
        }
    }

    private fun autoSync(
        connectionType: NetworkType,
        chargingBattery: Boolean = false,
    ) {
        BatchSyncWorker.enqueuePeriodic(
            connectionType = connectionType,
            chargingBattery = chargingBattery,
        )
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

    private fun forceSyncAll() {
        db.getRepositoryDao().getAll().forEach {
            if (it.lastModified.isNotEmpty() || it.entityTag.isNotEmpty()) {
                db.getRepositoryDao().put(it.copy(lastModified = "", entityTag = ""))
            }
        }
        BatchSyncWorker.enqueue(SyncRequest.FORCE)
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