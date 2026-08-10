package com.blueskybone.arkscreen.platform.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber

class AttendanceAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val settings: SettingPrefManager by getKoin().inject()
        val alarmController: AttendanceAlarmController by getKoin().inject()
        Timber.i("收到后台签到广播：${intent.action}")

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (settings.backAutoAtd.get()) alarmController.schedule()
            }
            ACTION_DAILY_ATTENDANCE -> {
                if (settings.backAutoAtd.get()) {
                    AttendanceWorkScheduler.enqueue(context, force = true)
                }
            }
        }
    }

    companion object {
        const val ACTION_DAILY_ATTENDANCE = "com.blueskybone.arkscreen.DAILY_ATTENDANCE"
    }
}
