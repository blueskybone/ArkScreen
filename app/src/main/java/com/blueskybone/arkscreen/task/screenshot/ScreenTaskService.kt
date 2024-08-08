package com.blueskybone.arkscreen.task.screenshot

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.activity.MainActivity
import com.blueskybone.arkscreen.base.PrefManager
import org.koin.android.ext.android.getKoin

/**
 *   Created by blueskybone
 *   Date: 2024/2/29
 */

class ScreenTaskService : Service() {

//    private val loggerRun: LoggerRun by KoinJavaComponent.getKoin().inject()

    private val prefManager: PrefManager by getKoin().inject()

    companion object {
        private const val TAG = "ScreenTaskService"
        private const val FOREGROUND_SERVICE_ID = 7594

        private const val CHANNEL_FORE_ID = "1094"
        private const val CHANNEL_FORE_NAME = "screenshot_fore_service"
        private var notification: Notification? = null

        var instance: ScreenTaskService? = null
        var debug: Boolean = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        instance = this
        createNotification()
        super.onCreate()
    }

    override fun onDestroy() {
//        if (debug)
//            loggerRun.write("$TAG: onDestroy")

        APP.releaseMutexService()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if (debug)
//            loggerRun.write("$TAG: onStartCommand()")
//
//        debug = settingPreference.enableDebug.get()
        foreground()

        val scIntent = Intent(this, ScreenshotActivity::class.java)
        scIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        this.startActivity(scIntent)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification() {
        val channelFore = NotificationChannel(
            CHANNEL_FORE_ID,
            CHANNEL_FORE_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channelFore)

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        notification = Notification.Builder(this, CHANNEL_FORE_ID)
            .setChannelId(CHANNEL_FORE_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setContentTitle("正在截图...")
            .setContentText("此通知是用于截图的必要操作，可自行关闭")
            .setAutoCancel(true)
            .build()
    }

    private fun foreground() {
        println("$TAG: foreground()")
//        if (debug)
//            loggerRun.write("$TAG: foreground()")
        startForeground(FOREGROUND_SERVICE_ID, notification)
    }

    private fun background() {
        println("background")
//        if (debug)
//            loggerRun.write("$TAG: background()")
        stopForeground(STOP_FOREGROUND_DETACH)
    }
}