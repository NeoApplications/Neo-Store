package com.machiav3lli.fdroid

import android.annotation.SuppressLint
import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build.VERSION_CODES
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import com.machiav3lli.fdroid.content.Cache
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.DatabaseX
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.network.CoilDownloader
import com.machiav3lli.fdroid.network.Downloader
import com.machiav3lli.fdroid.service.Connection
import com.machiav3lli.fdroid.service.PackageChangedReceiver
import com.machiav3lli.fdroid.service.SyncService
import com.machiav3lli.fdroid.ui.activities.MainActivityX
import com.machiav3lli.fdroid.ui.activities.PrefsActivityX
import com.machiav3lli.fdroid.utility.Utils.setLanguage
import com.machiav3lli.fdroid.utility.Utils.toInstalledItem
import com.machiav3lli.fdroid.utility.extension.android.Android
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import kotlin.time.Duration.Companion.minutes


@Suppress("unused")
@HiltAndroidApp
class MainApplication : Application(), ImageLoaderFactory {

    lateinit var db: DatabaseX
    lateinit var mActivity: AppCompatActivity
    //lateinit var wm: WorksManager

    companion object {
        private var appRef: WeakReference<MainApplication> = WeakReference(null)
        private val neo_store: MainApplication get() = appRef.get()!!

        private var mainActivityRef: WeakReference<MainActivityX> = WeakReference(null)
        var mainActivity: MainActivityX?
            get() = mainActivityRef.get()
            set(mainActivity) {
                mainActivityRef = WeakReference(mainActivity)
            }
        private var prefsActivityRef: WeakReference<PrefsActivityX> = WeakReference(null)
        var prefsActivity: PrefsActivityX?
            get() = prefsActivityRef.get()
            set(mainActivity) {
                prefsActivityRef = WeakReference(mainActivity)
            }

        //val wm: WorksManager get() = neo_store.wm
        //val db: DatabaseX get() = neo_store.db
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

        db = DatabaseX.getInstance(applicationContext)
        Preferences.init(this)
        RepositoryUpdater.init(this)
        listenApplications()
        listenPreferences()

        /*if (databaseUpdated) {
            forceSyncAll()
        }*/

        //wm = WorksManager(applicationContext)
        //wm.prune()
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
            db.installedDao.emptyTable()
            db.installedDao.put(*installedItems.toTypedArray())
        }
    }

    private fun listenPreferences() {
        updateProxy()
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                when (it) {
                    Preferences.Key.ProxyType,
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
                                MainActivityX::class.java
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
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val reschedule = force || jobScheduler.allPendingJobs.none { it.id == JOB_ID_SYNC }
        if (reschedule) {
            when (val autoSync = Preferences[Preferences.Key.AutoSync]) {
                is Preferences.AutoSync.Never  -> {
                    jobScheduler.cancel(JOB_ID_SYNC)
                    Log.i(this::javaClass.name, "Canceled next auto-sync run.")
                }
                is Preferences.AutoSync.Wifi,
                is Preferences.AutoSync.WifiBattery,
                                               -> {
                    autoSync(
                        jobScheduler = jobScheduler,
                        connectionType = NETWORK_TYPE_WIFI,
                        chargingBattery = autoSync is Preferences.AutoSync.WifiBattery,
                    )
                }
                is Preferences.AutoSync.Battery,
                                               -> {
                    autoSync(
                        jobScheduler = jobScheduler,
                        connectionType = JobInfo.NETWORK_TYPE_ANY,
                        chargingBattery = true,
                    )
                }
                is Preferences.AutoSync.Always -> {
                    autoSync(
                        jobScheduler = jobScheduler,
                        connectionType = JobInfo.NETWORK_TYPE_ANY
                    )
                }
            }
        }
    }

    private fun autoSync(
        jobScheduler: JobScheduler,
        connectionType: Int,
        chargingBattery: Boolean = false,
    ) {
        val period = Preferences[Preferences.Key.AutoSyncInterval].minutes.inWholeMilliseconds
        jobScheduler.schedule(
            JobInfo
                .Builder(
                    JOB_ID_SYNC,
                    ComponentName(this, SyncService.Job::class.java)
                )
                .setRequiresCharging(chargingBattery)
                .apply {
                    if (connectionType == NETWORK_TYPE_WIFI) {
                        if (Android.sdk(VERSION_CODES.P)) setRequiredNetwork(
                            NetworkRequest.Builder()
                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI_AWARE)
                                .build()
                        )
                        else setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                    } else setRequiredNetworkType(connectionType)
                    if (Android.sdk(VERSION_CODES.N))
                        setPeriodic(period, JobInfo.getMinFlexMillis())
                    else setPeriodic(period)
                    if (Android.sdk(VERSION_CODES.O)) {
                        setRequiresBatteryNotLow(true)
                        setRequiresStorageNotLow(true)
                    }
                }
                .setPersisted(true)
                .build()
        )
    }

    private fun updateProxy() {
        val type = Preferences[Preferences.Key.ProxyType].proxyType
        val host = Preferences[Preferences.Key.ProxyHost]
        val port = Preferences[Preferences.Key.ProxyPort]
        val socketAddress = when (type) {
            Proxy.Type.DIRECT                 -> {
                null
            }
            Proxy.Type.HTTP, Proxy.Type.SOCKS -> {
                try {
                    InetSocketAddress.createUnresolved(host, port)
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        val proxy = socketAddress?.let { Proxy(type, it) }
        Downloader.proxy = proxy
    }

    private fun forceSyncAll() {
        db.repositoryDao.all.forEach {
            if (it.lastModified.isNotEmpty() || it.entityTag.isNotEmpty()) {
                db.repositoryDao.put(it.copy(lastModified = "", entityTag = ""))
            }
        }
        Connection(SyncService::class.java, onBind = { connection, binder ->
            binder.sync(SyncService.SyncRequest.FORCE)
            connection.unbind(this)
        }).bind(this)
    }

    class BootReceiver : BroadcastReceiver() {
        @SuppressLint("UnsafeProtectedBroadcastReceiver")
        override fun onReceive(context: Context, intent: Intent) = Unit
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .callFactory(CoilDownloader.Factory(Cache.getImagesDir(this)))
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