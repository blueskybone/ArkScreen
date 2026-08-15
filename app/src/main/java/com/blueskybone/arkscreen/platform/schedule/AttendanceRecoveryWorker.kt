package com.blueskybone.arkscreen.platform.schedule

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import org.koin.java.KoinJavaComponent.getKoin

/** 定期补偿设备关机或不可用期间错过的签到任务。 */
class AttendanceRecoveryWorker(
    context: Context,
    params: WorkerParameters,
) : Worker(context, params) {
    private val settings: SettingPrefManager by getKoin().inject()

    override fun doWork(): Result {
        if (settings.backAutoAtd.get()) {
            AttendanceWorkScheduler.enqueue(
                context = applicationContext,
                requireEnabled = true,
            )
        }
        return Result.success()
    }
}
