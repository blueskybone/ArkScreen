package com.blueskybone.arkscreen.platform.schedule

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingPeriodicWorkPolicy
import java.util.concurrent.TimeUnit

object AttendanceWorkScheduler {
    private const val UNIQUE_WORK_NAME = "daily-attendance"
    private const val RECOVERY_WORK_NAME = "daily-attendance-recovery"

    fun enqueue(
        context: Context,
        allowRepeatToday: Boolean = false,
        requireEnabled: Boolean = false,
    ) {
        val requestBuilder = OneTimeWorkRequestBuilder<AttendanceWorker>()
            .setInputData(
                androidx.work.workDataOf(
                    AttendanceWorker.ALLOW_REPEAT_TODAY to allowRepeatToday,
                    AttendanceWorker.REQUIRE_AUTO_ENABLED to requireEnabled,
                )
            )
        if (requireEnabled) {
            requestBuilder.setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
        }
        val request = requestBuilder.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            if (allowRepeatToday) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }

    /** Checks again later when the configured alarm was missed or the device was offline. */
    fun ensureRecovery(context: Context) {
        val request = PeriodicWorkRequestBuilder<AttendanceRecoveryWorker>(
            RECOVERY_INTERVAL_HOURS,
            TimeUnit.HOURS,
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            RECOVERY_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelRecovery(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(RECOVERY_WORK_NAME)
    }

    private const val RECOVERY_INTERVAL_HOURS = 6L
}
