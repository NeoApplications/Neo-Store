package com.machiav3lli.fdroid.utility

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import androidx.core.app.NotificationCompat
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_INSTALLER
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_UPDATES
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_VULNS
import com.machiav3lli.fdroid.NOTIFICATION_ID_INSTALLER
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_ID_UPDATES
import com.machiav3lli.fdroid.NOTIFICATION_ID_VULNS
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Repository
import com.machiav3lli.fdroid.entity.ProductItem
import com.machiav3lli.fdroid.index.RepositoryUpdater
import com.machiav3lli.fdroid.service.ActionReceiver
import com.machiav3lli.fdroid.service.InstallerReceiver
import com.machiav3lli.fdroid.service.installIntent
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.service.worker.SyncWorker
import com.machiav3lli.fdroid.service.worker.ValidationError
import com.machiav3lli.fdroid.utility.extension.android.Android
import com.machiav3lli.fdroid.utility.extension.android.notificationManager
import com.machiav3lli.fdroid.utility.extension.resources.getColorFromAttr
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Displays summary of available updates.
 *
 * @param productItems list of apps pending updates
 */
fun Context.displayUpdatesNotification(
    productItems: List<ProductItem>,
    enforceNotify: Boolean = false,
) {
    val maxUpdates = 5
    fun <T> T.applyHack(callback: T.() -> Unit): T = apply(callback)
    if (productItems.isNotEmpty() || enforceNotify)
        notificationManager.notify(
            NOTIFICATION_ID_UPDATES, NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_UPDATES)
                .setSmallIcon(R.drawable.ic_new_releases)
                .setContentTitle(getString(if (productItems.isNotEmpty()) R.string.new_updates_available else R.string.no_updates_available))
                .setContentText(
                    if (productItems.isNotEmpty())
                        resources.getQuantityString(
                            R.plurals.new_updates_DESC_FORMAT,
                            productItems.size, productItems.size
                        )
                    else null
                )
                .setColor(
                    ContextThemeWrapper(this, R.style.Theme_Main_Amoled)
                        .getColorFromAttr(android.R.attr.colorPrimary).defaultColor
                )
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, NeoActivity::class.java)
                            .setAction(NeoActivity.ACTION_UPDATES)
                            .putExtra(
                                NeoActivity.EXTRA_UPDATES,
                                productItems.map { it.packageName }.toTypedArray()
                            ),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setStyle(NotificationCompat.InboxStyle().applyHack {
                    for (productItem in productItems.take(maxUpdates)) {
                        val builder = SpannableStringBuilder(productItem.name)
                        builder.setSpan(
                            ForegroundColorSpan(Color.BLACK), 0, builder.length,
                            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        builder.append(' ').append(productItem.version)
                        addLine(builder)
                    }
                    if (productItems.size > maxUpdates) {
                        val summary =
                            getString(R.string.plus_more_FORMAT, productItems.size - maxUpdates)
                        addLine(summary)
                    }
                })
                .build()
        )
    else notificationManager.cancel(NOTIFICATION_ID_UPDATES)
}

fun Context.displayVulnerabilitiesNotification(
    productItems: List<ProductItem>,
) {
    fun <T> T.applyHack(callback: T.() -> Unit): T = apply(callback)
    if (productItems.isNotEmpty())
        notificationManager.notify(
            NOTIFICATION_ID_VULNS, NotificationCompat
                .Builder(this, NOTIFICATION_CHANNEL_VULNS)
                .setSmallIcon(R.drawable.ic_new_releases)
                .setContentTitle(
                    getString(
                        if (productItems.isNotEmpty()) R.string.vulnerabilities_installed_apps
                        else R.string.no_vulnerabilities_installed_apps
                    )
                )
                .setColor(
                    ContextThemeWrapper(this, R.style.Theme_Main_Amoled)
                        .getColorFromAttr(android.R.attr.textColorTertiary).defaultColor
                )
                .setContentIntent(
                    PendingIntent.getActivity(
                        this,
                        0,
                        Intent(this, NeoActivity::class.java)
                            .setAction(NeoActivity.ACTION_UPDATES)
                            .putExtra(
                                NeoActivity.EXTRA_UPDATES,
                                productItems.map { it.packageName }.toTypedArray()
                            ),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                )
                .setStyle(NotificationCompat.InboxStyle().applyHack {
                    productItems.forEach { productItem ->
                        val builder = SpannableStringBuilder(productItem.name)
                        builder.setSpan(
                            ForegroundColorSpan(Color.BLACK), 0, builder.length,
                            SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        builder.append(' ').append(productItem.version)
                        addLine(builder)
                    }
                })
                .build()
        )
    else notificationManager.cancel(NOTIFICATION_ID_VULNS)
}

fun Context.showNotificationError(repository: Repository, exception: Exception) {
    notificationManager.notify(
        "repository-${repository.id}", NOTIFICATION_ID_SYNCING, NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_SYNCING)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setColor(
                ContextThemeWrapper(this, R.style.Theme_Main_Amoled)
                    .getColorFromAttr(android.R.attr.colorPrimary).defaultColor
            )
            .setContentTitle(getString(R.string.could_not_sync_FORMAT, repository.name))
            .setContentText(
                getString(
                    when (exception) {
                        is RepositoryUpdater.UpdateException -> when (exception.errorType) {
                            RepositoryUpdater.ErrorType.NETWORK    -> R.string.network_error_DESC
                            RepositoryUpdater.ErrorType.HTTP       -> R.string.http_error_DESC
                            RepositoryUpdater.ErrorType.VALIDATION -> R.string.validation_index_error_DESC
                            RepositoryUpdater.ErrorType.PARSING    -> R.string.parsing_index_error_DESC
                        }

                        else                                 -> R.string.unknown_error_DESC
                    }
                )
            )
            .build()
    )
}

fun Context.syncNotificationBuilder() = NotificationCompat
    .Builder(MainApplication.context, NOTIFICATION_CHANNEL_SYNCING)
    .setSmallIcon(R.drawable.ic_sync)
    .setGroup(NOTIFICATION_CHANNEL_SYNCING)
    .setOngoing(true)
    .setSilent(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    .addAction(
        R.drawable.ic_cancel,
        MainApplication.context.getString(R.string.cancel_all),
        PendingIntent.getBroadcast(
            MainApplication.context,
            "<SYNC_ALL>".hashCode(),
            Intent(MainApplication.context, ActionReceiver::class.java).apply {
                action = ActionReceiver.COMMAND_CANCEL_SYNC_ALL
            },
            PendingIntent.FLAG_IMMUTABLE
        )
    )

fun Context.downloadNotificationBuilder() = NotificationCompat
    .Builder(this, NOTIFICATION_CHANNEL_DOWNLOADING)
    .setSmallIcon(android.R.drawable.stat_sys_download)
    .setGroup(NOTIFICATION_CHANNEL_DOWNLOADING)
    .setOngoing(true)
    .setSilent(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setCategory(NotificationCompat.CATEGORY_PROGRESS)
    .setProgress(0, 0, true)

fun Context.installNotificationBuilder() = NotificationCompat
    .Builder(this, NOTIFICATION_CHANNEL_INSTALLER)
    .setSmallIcon(android.R.drawable.stat_sys_download_done)
    .setGroup(NOTIFICATION_CHANNEL_INSTALLER)
    .setOngoing(true)
    .setSilent(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setCategory(NotificationCompat.CATEGORY_STATUS)

fun NotificationCompat.Builder.updateProgress(
    context: Context,
    progress: SyncWorker.Progress,
) {
    when (progress.stage) {
        RepositoryUpdater.Stage.DOWNLOAD -> {
            if (progress.total >= 0) {
                setContentText("${progress.read.formatSize()} / ${progress.total.formatSize()}")
                setProgress(
                    100,
                    progress.percentage,
                    false
                )
            } else {
                setContentText(progress.read.formatSize())
                setProgress(0, 0, true)
            }
        }

        RepositoryUpdater.Stage.PROCESS  -> {
            setContentText(
                context.getString(
                    R.string.processing_FORMAT,
                    "${progress.percentage}%"
                )
            )
            setProgress(100, progress.percentage, progress == null)
        }

        RepositoryUpdater.Stage.MERGE    -> {
            setContentText(
                context.getString(
                    R.string.merging_FORMAT,
                    "${progress.read} / ${progress.total}"
                )
            )
            setProgress(100, progress.percentage, false)
        }

        RepositoryUpdater.Stage.COMMIT   -> {
            setContentText(context.getString(R.string.saving_details))
            setProgress(0, 0, true)
        }
    }
}

fun NotificationCompat.Builder.updateWithError(
    context: Context,
    state: DownloadState,
    errorType: ValidationError,
) = apply {
    setSmallIcon(android.R.drawable.stat_sys_warning)
    setContentTitle(
        context.getString(
            R.string.downloading_error_FORMAT,
            "${state.name} (${state.version})"
        )
    )
    setContentText(
        context.getString(
            R.string.validation_error_FORMAT,
            context.getString(
                when (errorType) {
                    ValidationError.INTEGRITY   -> R.string.integrity_check_error_DESC
                    ValidationError.FORMAT      -> R.string.file_format_error_DESC
                    ValidationError.METADATA    -> R.string.invalid_metadata_error_DESC
                    ValidationError.SIGNATURE   -> R.string.invalid_signature_error_DESC
                    ValidationError.PERMISSIONS -> R.string.invalid_permissions_error_DESC
                    ValidationError.FILE_SIZE   -> R.string.file_size_error_DESC
                    ValidationError.UNKNOWN     -> R.string.unknown_error_DESC
                    ValidationError.NONE        -> -1
                }
            )
        )
    )
}

/**
 * Notifies user of installer outcome. This can be success, error, or a request for user action
 * if installation cannot proceed automatically.
 *
 * @param intent provided by PackageInstaller to the callback service/activity.
 */
fun notifyStatus(context: Context, intent: Intent?) {

    if (Android.sdk(26)) {
        NotificationChannel(
            NOTIFICATION_CHANNEL_INSTALLER,
            context.getString(R.string.install), NotificationManager.IMPORTANCE_LOW
        )
            .let(context.notificationManager::createNotificationChannel)
    }

    val scope = CoroutineScope(Dispatchers.IO)
    // unpack from intent
    val status = intent?.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
    val sessionId = intent?.getIntExtra(PackageInstaller.EXTRA_SESSION_ID, -1) ?: 0

    // get package information from session
    val sessionInstaller = context.packageManager.packageInstaller
    val session = if (sessionId > 0) sessionInstaller.getSessionInfo(sessionId) else null

    val packageName =
        session?.appPackageName ?: intent?.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
    val message = intent?.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
    val installerAction = intent?.getStringExtra(InstallerReceiver.KEY_ACTION)

    // get application name for notifications
    val appLabel = session?.appLabel ?: intent?.getStringExtra(InstallerReceiver.KEY_PACKAGE_LABEL)
    ?: try {
        if (packageName != null) context.packageManager.getApplicationLabel(
            context.packageManager.getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
        ) else null
    } catch (_: Exception) {
        null
    }

    val notificationTag = "${InstallerReceiver.NOTIFICATION_TAG_PREFIX}$packageName"

    // start building
    val builder = NotificationCompat
        .Builder(context, NOTIFICATION_CHANNEL_INSTALLER)
        .setAutoCancel(true)
        .setColor(
            ContextThemeWrapper(context, R.style.Theme_Main_Amoled)
                .getColorFromAttr(androidx.appcompat.R.attr.colorPrimary).defaultColor
        )

    when (status) {
        PackageInstaller.STATUS_PENDING_USER_ACTION -> {
            // request user action with "downloaded" notification that triggers a working prompt
            context.notificationManager.notify(
                notificationTag, NOTIFICATION_ID_INSTALLER, builder
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentIntent(installIntent(context, intent))
                    .setContentTitle(context.getString(R.string.downloaded_FORMAT, appLabel))
                    .setContentText(context.getString(R.string.tap_to_install_DESC))
                    .build()
            )
        }

        PackageInstaller.STATUS_SUCCESS             -> {
            if (installerAction == InstallerReceiver.ACTION_UNINSTALL) {
                // remove any notification for this app
                context.notificationManager.cancel(notificationTag, NOTIFICATION_ID_INSTALLER)
            } else {
                packageName?.let {
                    scope.launch {
                        MainApplication.db.getInstallTaskDao().delete(it)
                    }
                }
                val notification = builder
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .setContentTitle(context.getString(R.string.installed))
                    .setContentText(appLabel)
                    .apply {
                        if (!Preferences[Preferences.Key.KeepInstallNotification])
                            setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
                        else
                            setContentIntent(
                                PendingIntent.getActivity(
                                    context,
                                    0,
                                    Intent(context, NeoActivity::class.java)
                                        .setAction(Intent.ACTION_VIEW)
                                        .setData(Uri.parse("market://details?id=$packageName")),
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )
                            )
                    }
                    .build()
                context.notificationManager.notify(
                    notificationTag,
                    NOTIFICATION_ID_INSTALLER,
                    notification
                )
            }
        }

        PackageInstaller.STATUS_FAILURE_ABORTED     -> {
            // do nothing if user cancels
        }

        else                                        -> {
            // problem occurred when installing/uninstalling package
            // STATUS_FAILURE, STATUS_FAILURE_STORAGE ,STATUS_FAILURE_BLOCKED, STATUS_FAILURE_INCOMPATIBLE, STATUS_FAILURE_CONFLICT, STATUS_FAILURE_INVALID
            packageName?.let { scope.launch { MainApplication.db.getInstallTaskDao().delete(it) } }
            val notification = builder
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle(context.getString(R.string.installing_error_FORMAT, appLabel))
                .setContentText(
                    message ?: context.getString(
                        when (status) {
                            PackageInstaller.STATUS_FAILURE_STORAGE
                                 -> R.string.installing_error_storage_DESC

                            PackageInstaller.STATUS_FAILURE_BLOCKED
                                 -> R.string.installing_error_blocked_DESC

                            PackageInstaller.STATUS_FAILURE_INCOMPATIBLE
                                 -> R.string.installing_error_incompatible_DESC

                            PackageInstaller.STATUS_FAILURE_CONFLICT
                                 -> R.string.installing_error_conflict_DESC

                            PackageInstaller.STATUS_FAILURE_TIMEOUT
                                 -> R.string.installing_error_timeout_DESC

                            PackageInstaller.STATUS_FAILURE_INVALID
                                 -> R.string.installing_error_invalid_DESC

                            else -> R.string.installing_error_unknown_DESC // PackageInstaller.STATUS_FAILURE & unknown
                        }
                    )
                )
                .build()
            context.notificationManager.notify(
                notificationTag,
                NOTIFICATION_ID_INSTALLER,
                notification
            )
        }
    }
}

fun notifyFinishedInstall(context: Context, packageName: String) {
    val notificationTag = "${InstallerReceiver.NOTIFICATION_TAG_PREFIX}$packageName"

    val notification = NotificationCompat
        .Builder(context, NOTIFICATION_CHANNEL_INSTALLER)
        .setAutoCancel(true)
        .setColor(
            ContextThemeWrapper(context, R.style.Theme_Main_Amoled)
                .getColorFromAttr(androidx.appcompat.R.attr.colorPrimary).defaultColor
        )
        .setSmallIcon(android.R.drawable.stat_sys_download_done)
        .setContentTitle(context.getString(R.string.installed))
        .setContentText(
            try {
                context.packageManager.getApplicationLabel(
                    context.packageManager.getApplicationInfo(
                        packageName,
                        PackageManager.GET_META_DATA
                    )
                )
            } catch (_: Exception) {
                null
            }
        )
        .apply {
            if (!Preferences[Preferences.Key.KeepInstallNotification])
                setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
        }
        .build()
    context.notificationManager.notify(
        notificationTag,
        NOTIFICATION_ID_INSTALLER,
        notification
    )
}