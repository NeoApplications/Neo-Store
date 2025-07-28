package com.machiav3lli.fdroid.utils

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DEBUG
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_DOWNLOADING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_INSTALLER
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_VULNS
import com.machiav3lli.fdroid.NOTIFICATION_ID_DEBUG
import com.machiav3lli.fdroid.NOTIFICATION_ID_INSTALLER
import com.machiav3lli.fdroid.NOTIFICATION_ID_SYNCING
import com.machiav3lli.fdroid.NOTIFICATION_ID_VULNS
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.NeoApp
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.content.Preferences
import com.machiav3lli.fdroid.data.database.entity.Repository
import com.machiav3lli.fdroid.data.entity.DownloadState
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.data.entity.SyncState
import com.machiav3lli.fdroid.data.entity.ValidationError
import com.machiav3lli.fdroid.data.index.RepositoryUpdater
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import com.machiav3lli.fdroid.manager.service.InstallerReceiver
import com.machiav3lli.fdroid.manager.service.installIntent
import com.machiav3lli.fdroid.utils.extension.android.Android
import com.machiav3lli.fdroid.utils.extension.android.notificationManager
import com.machiav3lli.fdroid.utils.extension.resources.getColorFromAttr
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

fun Context.syncNotificationBuilder(title: String, content: String = "", percent: Int = -1) =
    NotificationCompat
        .Builder(this, NOTIFICATION_CHANNEL_SYNCING)
        .setGroup(NOTIFICATION_CHANNEL_SYNCING)
        .setSmallIcon(R.drawable.ic_sync)
        .setContentTitle(title)
        .setTicker(title)
        .setContentText(content)
        .setOngoing(true)
        .setSilent(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setProgress(100, percent, percent == -1)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, NeoActivity::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
        .addAction(
            R.drawable.ic_cancel,
            NeoApp.context.getString(R.string.cancel_all),
            PendingIntent.getBroadcast(
                NeoApp.context,
                "<SYNC_ALL>".hashCode(),
                Intent(NeoApp.context, ActionReceiver::class.java).apply {
                    action = ActionReceiver.COMMAND_CANCEL_SYNC_ALL
                },
                PendingIntent.FLAG_IMMUTABLE
            )
        )

fun Context.downloadNotificationBuilder(title: String, content: String = "", percent: Int = -1) =
    NotificationCompat
        .Builder(this, NOTIFICATION_CHANNEL_DOWNLOADING)
        .setGroup(NOTIFICATION_CHANNEL_DOWNLOADING)
        .setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle(title)
        .setTicker(title)
        .setContentText(content)
        .setOngoing(true)
        .setSilent(true)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setCategory(NotificationCompat.CATEGORY_PROGRESS)
        .setProgress(100, percent, percent == -1)
        .setContentIntent(
            PendingIntent.getActivity(
                this, 0,
                Intent(this, NeoActivity::class.java).setAction(NeoActivity.ACTION_UPDATES),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )

fun Context.installNotificationBuilder() = NotificationCompat
    .Builder(this, NOTIFICATION_CHANNEL_INSTALLER)
    .setSmallIcon(android.R.drawable.stat_sys_download_done)
    .setGroup(NOTIFICATION_CHANNEL_INSTALLER)
    .setOngoing(true)
    .setSilent(true)
    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    .setCategory(NotificationCompat.CATEGORY_STATUS)

fun Context.reportSyncFail(repoId: Long, state: SyncState.Failed) {
    val title = getString(
        R.string.syncing_FORMAT,
        state.repoName
    )
    val builder = syncNotificationBuilder(title)
        .setContentText("${getString(R.string.action_failed)}:\n${state.error}")
        .setSmallIcon(R.drawable.ic_new_releases)
        .setOngoing(false)

    if (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    ) notificationManager.notify(
        "$NOTIFICATION_CHANNEL_SYNCING$repoId".hashCode(),
        builder.setOngoing(true)
            .build()
    )
}

fun Context.notifySensitivePermissionsChanged(packageName: String, newPermissions: Set<String>) {
    val intent = Intent(this, NeoActivity::class.java).apply {
        action = NeoActivity.ACTION_UPDATES
        putExtra("EXTRA_SENSITIVE_PERMISSIONS", newPermissions.toTypedArray())
        putExtra("EXTRA_PACKAGE_NAME", packageName)
    }

    val pendingIntent = PendingIntent.getActivity(
        this,
        packageName.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_INSTALLER)
        .setSmallIcon(R.drawable.ic_new_releases)
        .setContentTitle(getString(R.string.new_sensitive_permission_title))
        .setContentText(getString(R.string.sensitive_permission_detected, packageName))
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setColor(
            ContextThemeWrapper(this, R.style.Theme_Main_Amoled)
                .getColorFromAttr(android.R.attr.textColorTertiary).defaultColor
        )
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .setStyle(
            NotificationCompat.InboxStyle().also { style ->
            style.addLine(getString(R.string.sensitive_permission_detected_list))
            newPermissions.forEach { style.addLine("• $it") }
         }
        )
        .build()

    notificationManager.notify("SENSITIVE-$packageName", NOTIFICATION_ID_INSTALLER + 99, notification)
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
                    ValidationError.SENSITIVE_PERMISSION -> R.string.sensitive_permission_error_DESC
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

    if (Android.sdk(Build.VERSION_CODES.O)) {
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
                        NeoApp.db.getInstallTaskDao().delete(it)
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
                                        .setData("market://details?id=$packageName".toUri()),
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
            packageName?.let { scope.launch { NeoApp.db.getInstallTaskDao().delete(it) } }
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

fun notifyDebugStatus(context: Context, title: String, message: String) =
    if (context.packageName.endsWith("debug")) {
        if (Android.sdk(Build.VERSION_CODES.O)) {
            NotificationChannel(
                NOTIFICATION_CHANNEL_DEBUG,
                context.getString(R.string.notify_channel_debug), NotificationManager.IMPORTANCE_LOW
            )
                .let(context.notificationManager::createNotificationChannel)
        }

        val notificationTag = "${InstallerReceiver.NOTIFICATION_TAG_PREFIX}-debug"

        // start building
        val builder = NotificationCompat
            .Builder(context, NOTIFICATION_CHANNEL_DEBUG)
            .setAutoCancel(false)
            .setColor(
                ContextThemeWrapper(context, R.style.Theme_Main_Amoled)
                    .getColorFromAttr(androidx.appcompat.R.attr.colorPrimary).defaultColor
            )

        val notification = builder
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle(title)
            .setContentText(message)
            .build()
        context.notificationManager.notify(
            notificationTag,
            NOTIFICATION_ID_DEBUG,
            notification
        )
    } else {
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