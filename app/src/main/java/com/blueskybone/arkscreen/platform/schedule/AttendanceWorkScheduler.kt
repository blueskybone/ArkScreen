package com.blueskybone.arkscreen.platform.schedule

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object AttendanceWorkScheduler {
    private const val UNIQUE_WORK_NAME = "daily-attendance"

    fun enqueue(
        context: Context,
        force: Boolean,
        allowRepeatToday: Boolean = false,
    ) {
        val request = OneTimeWorkRequestBuilder<AttendanceWorker>()
            .setInputData(
                androidx.work.workDataOf(
                    AttendanceWorker.FORCE to force,
                    AttendanceWorker.ALLOW_REPEAT_TODAY to allowRepeatToday,
                )
            )
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            if (allowRepeatToday) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
