package com.machiav3lli.fdroid

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.work.NetworkType
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.database.databaseModule
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.network.CoilDownloader
import com.machiav3lli.fdroid.network.Downloader
import com.machiav3lli.fdroid.network.downloadClientModule
import com.machiav3lli.fdroid.network.exodusModule
import com.machiav3lli.fdroid.service.PackageChangedReceiver
import com.machiav3lli.fdroid.service.WorkerManager
import com.machiav3lli.fdroid.service.workmanagerModule
import com.machiav3lli.fdroid.service.worker.SyncRequest
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.utility.Utils.setLanguage
import com.machiav3lli.fdroid.utility.Utils.toInstalledItem
import com.machiav3lli.fdroid.utility.extension.android.Android
import io.ktor.client.engine.ProxyBuilder
import io.ktor.http.Url
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.lang.ref.WeakReference
import java.net.Proxy
import java.util.*


@Suppress("unused")
class MainApplication : Application(), ImageLoaderFactory {

    val db: DatabaseX by inject()
    lateinit var mActivity: AppCompatActivity
    val wm: WorkerManager by inject()

    companion object {
        val enqueuedInstalls: MutableSet<String> = mutableSetOf()

        private var appRef: WeakReference<MainApplication> = WeakReference(null)
        private val neo_store: MainApplication get() = appRef.get()!!

        private var mainActivityRef: WeakReference<NeoActivity> = WeakReference(null)
        var mainActivity: NeoActivity?
            get() = mainActivityRef.get()
            set(mainActivity) {
                mainActivityRef = WeakReference(mainActivity)
            }

        val context: Context get() = neo_store.applicationContext

        val wm: WorkerManager get() = neo_store.wm
        val db: DatabaseX get() = neo_store.db

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
        DynamicColors.applyToActivitiesIfAvailable(
            this,
            DynamicColorsOptions.Builder()
                .setPrecondition { _, _ -> Preferences[Preferences.Key.Theme] == Preferences.Theme.Dynamic }
                .build()
        )
        appRef = WeakReference(this)

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                exodusModule,
                downloadClientModule,
                workmanagerModule,
                databaseModule,
            )
        }

        Preferences.init(this)
        RepositoryUpdater.init(this)
        listenApplications()
        listenPreferences()

        wm.prune()
        Cache.cleanup(this)
        updateSyncJob(false)
    }

    private fun listenApplications() {
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
        CoroutineScope(Dispatchers.Default).launch {
            db.getInstalledDao().emptyTable()
            db.getInstalledDao().put(*installedItems.toTypedArray())
        }
    }

    private fun listenPreferences() {
        updateProxy()
        CoroutineScope(Dispatchers.Default).launch {
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
                        updateSyncJob(true)
                    }

                    Preferences.Key.UpdateUnstable -> {
                        forceSyncAll()
                    }

                    Preferences.Key.Theme          -> {
                        CoroutineScope(Dispatchers.Main).launch { mActivity.recreate() }
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

    private fun updateSyncJob(force: Boolean) {
        val wm = MainApplication.wm.workManager
        val reschedule = force || wm.getWorkInfosByTag(TAG_SYNC_PERIODIC).get().isEmpty()
        if (reschedule) {
            when (val autoSync = Preferences[Preferences.Key.AutoSync]) {
                is Preferences.AutoSync.Never  -> {
                    wm.cancelAllWorkByTag(TAG_SYNC_PERIODIC)
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
        SyncWorker.enqueuePeriodic(
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
        SyncWorker.enqueueAll(SyncRequest.FORCE)
    }

    class BootReceiver : BroadcastReceiver() {
        @SuppressLint("UnsafeProtectedBroadcastReceiver")
        override fun onReceive(context: Context, intent: Intent) = Unit
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .callFactory(CoilDownloader.Factory(Cache.getImagesDir(this))) // TODO migrate to Ktor on Coil 3.X
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