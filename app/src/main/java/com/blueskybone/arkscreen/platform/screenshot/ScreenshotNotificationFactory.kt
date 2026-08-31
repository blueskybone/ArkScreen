package com.blueskybone.arkscreen.platform.screenshot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.blueskybone.arkscreen.R

class ScreenshotNotificationFactory(
    private val context: Context,
) {

    fun createNotification(): Notification {
        ensureChannel()

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_rosm_tile) // 换成你的通知图标
            .setContentTitle(context.getString(R.string.screenshot_recognizing))
            .setContentText(context.getString(R.string.screenshot_in_progress))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                0,
                context.getString(R.string.screenshot_stop),
                PendingIntent.getService(
                    context,
                    STOP_REQUEST_CODE,
                    Intent(context, ScreenshotCaptureService::class.java).apply {
                        action = ScreenshotCaptureService.ACTION_STOP
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.screenshot_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        )

        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "screenshot_capture"
        private const val STOP_REQUEST_CODE = 10087
    }
}
