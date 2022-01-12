package com.looker.droidify

import android.annotation.SuppressLint
import android.app.Application
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.os.BatteryManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.looker.droidify.content.Cache
import com.looker.droidify.content.Preferences
import com.looker.droidify.content.ProductPreferences
import com.looker.droidify.database.DatabaseX
import com.looker.droidify.index.RepositoryUpdater
import com.looker.droidify.network.CoilDownloader
import com.looker.droidify.network.Downloader
import com.looker.droidify.service.Connection
import com.looker.droidify.service.SyncService
import com.looker.droidify.utility.Utils.setLanguage
import com.looker.droidify.utility.Utils.toInstalledItem
import com.looker.droidify.utility.extension.android.Android
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Proxy
import kotlin.time.Duration.Companion.hours


@Suppress("unused")
class MainApplication : Application(), ImageLoaderFactory {

    lateinit var db: DatabaseX

    override fun onCreate() {
        super.onCreate()

        db = DatabaseX.getInstance(applicationContext)
        Preferences.init(this)
        ProductPreferences.init(this)
        RepositoryUpdater.init(this)
        listenApplications()
        listenPreferences()

        /*if (databaseUpdated) {
            forceSyncAll()
        }*/

        Cache.cleanup(this)
        updateSyncJob(false)
    }

    private fun listenApplications() {
        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val packageName =
                    intent.data?.let { if (it.scheme == "package") it.schemeSpecificPart else null }
                if (packageName != null) {
                    when (intent.action.orEmpty()) {
                        Intent.ACTION_PACKAGE_ADDED,
                        Intent.ACTION_PACKAGE_REMOVED,
                        -> {
                            val packageInfo = try {
                                packageManager.getPackageInfo(
                                    packageName,
                                    Android.PackageManager.signaturesFlag
                                )
                            } catch (e: Exception) {
                                null
                            }
                            if (packageInfo != null) {
                                db.installedDao.put(packageInfo.toInstalledItem())
                            } else {
                                db.installedDao.delete(packageName)
                            }
                        }
                    }
                }
            }
        }, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        })
        val installedItems =
            packageManager.getInstalledPackages(Android.PackageManager.signaturesFlag)
                .map { it.toInstalledItem() }
        CoroutineScope(Dispatchers.Default).launch {
            db.installedDao.put(*installedItems.toTypedArray())
        }
    }

    private fun listenPreferences() {
        updateProxy()
        var lastAutoSync = Preferences[Preferences.Key.AutoSync]
        var lastUpdateUnstable = Preferences[Preferences.Key.UpdateUnstable]
        var lastLanguage = Preferences[Preferences.Key.Language]
        CoroutineScope(Dispatchers.Default).launch {
            Preferences.subject.collect {
                if (it == Preferences.Key.ProxyType || it == Preferences.Key.ProxyHost || it == Preferences.Key.ProxyPort) {
                    updateProxy()
                } else if (it == Preferences.Key.AutoSync) {
                    val autoSync = Preferences[Preferences.Key.AutoSync]
                    if (lastAutoSync != autoSync) {
                        lastAutoSync = autoSync
                        updateSyncJob(true)
                    }
                } else if (it == Preferences.Key.UpdateUnstable) {
                    val updateUnstable = Preferences[Preferences.Key.UpdateUnstable]
                    if (lastUpdateUnstable != updateUnstable) {
                        lastUpdateUnstable = updateUnstable
                        forceSyncAll()
                    }
                } else if (it == Preferences.Key.Language) {
                    val language = Preferences[Preferences.Key.Language]
                    if (language != lastLanguage) {
                        lastLanguage = language
                        val refresh = Intent.makeRestartActivityTask(
                            ComponentName(
                                baseContext,
                                MainActivity::class.java
                            )
                        )
                        applicationContext.startActivity(refresh)
                    }
                }
            }
        }
    }

    private fun updateSyncJob(force: Boolean) {
        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        val reschedule = force || !jobScheduler.allPendingJobs.any { it.id == JOB_ID_SYNC }
        if (reschedule) {
            val autoSync = Preferences[Preferences.Key.AutoSync]
            when (autoSync) {
                is Preferences.AutoSync.Never -> {
                    jobScheduler.cancel(JOB_ID_SYNC)
                }
                is Preferences.AutoSync.Wifi -> {
                    autoSync(
                        jobScheduler = jobScheduler,
                        connectionType = JobInfo.NETWORK_TYPE_UNMETERED
                    )
                }
                is Preferences.AutoSync.WifiBattery -> {
                    if (isCharging(this)) {
                        autoSync(
                            jobScheduler = jobScheduler,
                            connectionType = JobInfo.NETWORK_TYPE_UNMETERED
                        )
                    }
                    Unit
                }
                is Preferences.AutoSync.Always -> {
                    autoSync(
                        jobScheduler = jobScheduler,
                        connectionType = JobInfo.NETWORK_TYPE_ANY
                    )
                }
            }::class.java
        }
    }

    private fun autoSync(jobScheduler: JobScheduler, connectionType: Int) {
        val period = 12.hours.inWholeMilliseconds
        jobScheduler.schedule(
            JobInfo
                .Builder(
                    JOB_ID_SYNC,
                    ComponentName(this, SyncService.Job::class.java)
                )
                .setRequiredNetworkType(connectionType)
                .apply {
                    if (Android.sdk(26)) {
                        setRequiresBatteryNotLow(true)
                        setRequiresStorageNotLow(true)
                    }
                    if (Android.sdk(24)) setPeriodic(period, JobInfo.getMinFlexMillis())
                    else setPeriodic(period)
                }
                .build()
        )
    }

    private fun isCharging(context: Context): Boolean {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val plugged = intent!!.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        return plugged == BatteryManager.BATTERY_PLUGGED_AC
                || plugged == BatteryManager.BATTERY_PLUGGED_USB
                || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
    }

    private fun updateProxy() {
        val type = Preferences[Preferences.Key.ProxyType].proxyType
        val host = Preferences[Preferences.Key.ProxyHost]
        val port = Preferences[Preferences.Key.ProxyPort]
        val socketAddress = when (type) {
            Proxy.Type.DIRECT -> {
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
        db.repositoryDao.all.mapNotNull { it.trueData }.forEach {
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