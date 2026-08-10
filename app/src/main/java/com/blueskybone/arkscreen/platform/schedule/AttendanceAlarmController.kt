package com.blueskybone.arkscreen.platform.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import java.util.Calendar

/** Owns the single daily attendance alarm and its stable PendingIntent identity. */
class AttendanceAlarmController(
    context: Context,
    private val settings: SettingPrefManager,
) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule() {
        if (!settings.backAutoAtd.get()) {
            cancel()
            return
        }

        val now = System.currentTimeMillis()
        val triggerAt = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, settings.alarmAtdHour.get())
            set(Calendar.MINUTE, settings.alarmAtdMin.get())
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= now) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            AlarmManager.INTERVAL_DAY,
            pendingIntent(),
        )
    }

    fun cancel() {
        pendingIntent().let {
            alarmManager.cancel(it)
            it.cancel()
        }
    }

    private fun pendingIntent(): PendingIntent = PendingIntent.getBroadcast(
        appContext,
        REQUEST_CODE,
        Intent(appContext, AttendanceAlarmReceiver::class.java).apply {
            action = AttendanceAlarmReceiver.ACTION_DAILY_ATTENDANCE
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private companion object {
        const val REQUEST_CODE = 0
    }
}
