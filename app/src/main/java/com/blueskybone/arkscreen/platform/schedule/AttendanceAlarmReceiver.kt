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

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                if (settings.backAutoAtd.get()) {
                    Timber.i("收到开机广播，自动签到已开启，重新设置签到任务")
                    alarmController.schedule()
                } else {
                    Timber.i("收到开机广播，自动签到已关闭，忽略")
                }
            }
            ACTION_DAILY_ATTENDANCE -> {
                if (settings.backAutoAtd.get()) {
                    Timber.i("收到每日签到广播，准备执行自动签到")
                    AttendanceWorkScheduler.ensureRecovery(context)
                    AttendanceWorkScheduler.enqueue(
                        context,
                        requireEnabled = true,
                    )
                } else {
                    Timber.i("收到每日签到广播，自动签到已关闭，忽略")
                }
            }
            else -> Timber.w("收到未知签到广播：${intent.action}")
        }
    }

    companion object {
        const val ACTION_DAILY_ATTENDANCE = "com.blueskybone.arkscreen.DAILY_ATTENDANCE"
    }
}
