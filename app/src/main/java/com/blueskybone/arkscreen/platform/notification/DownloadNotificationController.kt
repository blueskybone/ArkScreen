package com.blueskybone.arkscreen.platform.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.ui.main.MainActivity

/**
 * Created by blueskybone
 * Date: 2026/7/9
 */
class DownloadNotificationController(
    private val context: Context,
) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showStarted() {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_update_downloading))
            .setContentText(context.getString(R.string.app_update_preparing_download))
            .setProgress(100, 0, true)
            .setOngoing(true)
            .build()

        notifySafely(notification)
    }

    fun updateProgress(percent: Int) {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_update_downloading))
            .setContentText(context.getString(R.string.app_update_download_progress, percent))
            .setProgress(100, percent, false)
            .setOngoing(true)
            .build()

        notifySafely(notification)
    }

    fun showCompleted(filePath: String) {
        ensureChannel()

        val openApp = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_INSTALL_DOWNLOADED_UPDATE
                putExtra(MainActivity.EXTRA_APK_PATH, filePath)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_update_download_completed))
            .setContentText(context.getString(R.string.app_update_continue_install))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()

        notifySafely(notification)
    }

    fun showFailed() {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_update_download_failed))
            .setContentText(context.getString(R.string.retry_later))
            .setOngoing(false)
            .build()

        notifySafely(notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.app_update_notification_channel),
            NotificationManager.IMPORTANCE_LOW,
        )

        notificationManager.createNotificationChannel(channel)
    }

    private fun notifySafely(notification: android.app.Notification) {
        runCatching {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        private const val CHANNEL_ID = "app_update"
        private const val NOTIFICATION_ID = 20001
    }
}
