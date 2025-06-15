package com.blueskybone.arkscreen.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.network.NetWorkTask
import com.blueskybone.arkscreen.preference.PrefManager
import com.blueskybone.arkscreen.room.ArkDatabase
import com.blueskybone.arkscreen.util.TimeUtils.getCurrentTs
import com.blueskybone.arkscreen.widget.AttendanceWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin
import org.koin.java.KoinJavaComponent
import org.koin.java.KoinJavaComponent.getKoin
import java.util.concurrent.TimeUnit

/**
 *   Created by blueskybone
 *   Date: 2025/2/4
 */

class AtdAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        println("Received intent: $intent")
        if (intent.action == "android.intent.action.BOOT_COMPLETED") { //Intent.ACTION_BOOT_COMPLETED
            val prefManager: PrefManager by getKoin().inject()
            if (!prefManager.backAutoAtd.get()) return
            APP.setDailyAlarm()
            return
        }else{
            CoroutineScope(Dispatchers.IO).launch {
                attendance()
            }
            showNotification(context,"森空岛自动签到","已签到")
        }
    }

    private suspend fun attendance() {
        val prefManager: PrefManager by getKoin().inject()
        val database = ArkDatabase.getDatabase(APP)
        val accountSkDao = database.getAccountSkDao()
        val accountList = accountSkDao.getAll()
        //TODO:Update Attendance progress in notification
        for (account in accountList) {
            NetWorkTask.sklandAttendance(account)
        }
        prefManager.lastAttendanceTs.set(getCurrentTs())
    }

    private fun showNotification(context: Context, title: String, message: String) {
        // 创建通知渠道（适用于 Android 8.0 及以上）
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1
        val channelId = "sign_in_channel"

        val channel =
            NotificationChannel(channelId, "签到通知", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}

