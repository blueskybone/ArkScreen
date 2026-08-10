package com.blueskybone.arkscreen.platform.screenshot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
            .setContentTitle("正在截图识别")
            .setContentText("正在进行屏幕截图，请稍候")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            CHANNEL_ID,
            "截图服务",
            NotificationManager.IMPORTANCE_LOW
        )

        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "screenshot_capture"
    }
}