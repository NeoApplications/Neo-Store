package com.machiav3lli.fdroid.manager.work

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.machiav3lli.fdroid.ARG_PACKAGE_NAMES
import com.machiav3lli.fdroid.ARG_REPOSITORY_IDS
import com.machiav3lli.fdroid.ContextWrapperX
import com.machiav3lli.fdroid.NOTIFICATION_CHANNEL_UPDATES
import com.machiav3lli.fdroid.NOTIFICATION_ID_UPDATES
import com.machiav3lli.fdroid.NeoActivity
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.data.entity.ProductItem
import com.machiav3lli.fdroid.manager.service.ActionReceiver
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class UpdatesNotificationManager(
    private val context: Context,
    private val notificationManager: NotificationManagerCompat,
) {
    private val mutex = Mutex()
    private val possibleUpdates = mutableSetOf<ProductItem>()
    private val langContext = ContextWrapperX.wrap(context)

    suspend fun addUpdates(vararg products: ProductItem) = mutex.withLock {
        possibleUpdates.addAll(products)
        updateNotification()
    }

    suspend fun removeUpdates(vararg products: ProductItem) = mutex.withLock {
        possibleUpdates.removeAll(products)
        updateNotification()
    }

    suspend fun replaceUpdates(vararg products: ProductItem) = mutex.withLock {
        possibleUpdates.clear()
        possibleUpdates.addAll(products)
        updateNotification()
    }

    private fun updateNotification() {
        if (possibleUpdates.isEmpty()) {
            notificationManager.cancel(NOTIFICATION_ID_UPDATES)
            return
        }

        val notification = createSyncNotification()
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) notificationManager.notify(NOTIFICATION_ID_UPDATES, notification)
    }

    private fun createSyncNotification(): Notification {
        val maxUpdates = 5
        val possibleUpdatesList = possibleUpdates.toList()
        val totalUpdates = possibleUpdatesList.size

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, NeoActivity::class.java)
                .setAction(NeoActivity.ACTION_UPDATES)
                .putExtra(
                    NeoActivity.EXTRA_UPDATES,
                    possibleUpdatesList.map { it.packageName }.toTypedArray()
                ),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val batchUpdateIntent = Intent(context, ActionReceiver::class.java).apply {
            this.action = ActionReceiver.COMMAND_BATCH_UPDATE
            putExtra(ARG_PACKAGE_NAMES, possibleUpdatesList.map { it.packageName }.toTypedArray())
            putExtra(ARG_REPOSITORY_IDS, possibleUpdatesList.map { it.repositoryId }.toLongArray())
        }
        val batchUpdatesPendingIntent = PendingIntent.getBroadcast(
            context,
            "batch_updates".hashCode(),
            batchUpdateIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_UPDATES)
            .setSortKey("0")
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setOngoing(false)
            .setContentTitle(
                langContext.getString(
                    if (possibleUpdatesList.isNotEmpty()) R.string.new_updates_available
                    else R.string.no_updates_available
                )
            )
            .setContentText(
                if (possibleUpdatesList.isNotEmpty())
                    langContext.resources.getQuantityString(
                        R.plurals.new_updates_DESC_FORMAT,
                        totalUpdates, totalUpdates
                    )
                else null
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.stat_sys_download,
                langContext.getString(R.string.update_all),
                batchUpdatesPendingIntent
            )

        val inboxStyle = NotificationCompat.InboxStyle()
        for (productItem in possibleUpdatesList.take(maxUpdates)) {
            val stringBuilder = SpannableStringBuilder(productItem.name)
            stringBuilder.setSpan(
                ForegroundColorSpan(Color.BLACK), 0, stringBuilder.length,
                SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            stringBuilder.append(' ').append(productItem.version)
            inboxStyle.addLine(stringBuilder)
        }
        if (totalUpdates > maxUpdates) {
            val summary = langContext.getString(
                R.string.plus_more_FORMAT,
                totalUpdates - maxUpdates
            )
            inboxStyle.addLine(summary)
        }
        builder.setStyle(inboxStyle)

        return builder.build()
    }
}