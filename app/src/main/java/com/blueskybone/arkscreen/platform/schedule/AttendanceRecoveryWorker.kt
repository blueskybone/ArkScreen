package com.blueskybone.arkscreen.platform.schedule

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import org.koin.java.KoinJavaComponent.getKoin

/** Periodic, failure-proof trigger for attendance missed while the device was unavailable. */
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
