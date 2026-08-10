package com.blueskybone.arkscreen.platform.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.blueskybone.arkscreen.R

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
            .setContentTitle("正在下载更新")
            .setContentText("准备下载")
            .setProgress(100, 0, true)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun updateProgress(percent: Int) {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("正在下载更新")
            .setContentText("$percent%")
            .setProgress(100, percent, false)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showCompleted() {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("下载完成")
            .setContentText("点击安装新版本")
            .setOngoing(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun showFailed() {
        ensureChannel()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("下载失败")
            .setContentText("请稍后重试")
            .setOngoing(false)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "应用更新",
            NotificationManager.IMPORTANCE_LOW,
        )

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "app_update"
        private const val NOTIFICATION_ID = 20001
    }
}