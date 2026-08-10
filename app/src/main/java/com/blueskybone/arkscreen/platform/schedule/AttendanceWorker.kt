package com.blueskybone.arkscreen.platform.schedule

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.usecase.attendance.RunAttendanceUseCase
import com.blueskybone.arkscreen.domain.repository.AttendanceStateRepository
import com.blueskybone.arkscreen.platform.notification.AttendanceNotificationController
import kotlinx.coroutines.CancellationException
import org.koin.java.KoinJavaComponent.getKoin
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId

class AttendanceWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val settings: SettingPrefManager by getKoin().inject()
    private val innerPrefs: InnerPrefManager by getKoin().inject()
    private val runAttendance: RunAttendanceUseCase by getKoin().inject()
    private val notifications: AttendanceNotificationController by getKoin().inject()
    private val attendanceStates: AttendanceStateRepository by getKoin().inject()

    override suspend fun doWork(): Result {
        val allowRepeatToday = inputData.getBoolean(ALLOW_REPEAT_TODAY, false)
        val requireAutoEnabled = inputData.getBoolean(REQUIRE_AUTO_ENABLED, false)
        if (requireAutoEnabled && !settings.backAutoAtd.get()) return Result.success()
        if (!allowRepeatToday && hasRunToday()) return Result.success()

        return try {
            val summary = runAttendance { index, total, name ->
                Timber.tag("Attendance").i(
                    "Start attendance: index=%d/%d account=%s",
                    index,
                    total,
                    name,
                )
                notifications.showProgress(index, total, name)
            }
            val attemptedAt = Instant.now().epochSecond
            summary.results.forEach { result ->
                attendanceStates.record(result, attemptedAt)
            }
            if (summary.results.isEmpty()) {
                notifications.showNoAccounts()
                return Result.success()
            }
            summary.results.forEach { result ->
                if (result.isSuccess) {
                    Timber.tag("Attendance").i(
                        "Attendance succeeded: account=%s result=%s",
                        result.accountName,
                        result.message,
                    )
                } else {
                    Timber.tag("Attendance").w(
                        "Attendance failed: account=%s message=%s",
                        result.accountName,
                        result.error?.message,
                    )
                }
            }
            summary.results.forEach(notifications::showAccountResult)
            notifications.showSummary(summary)

            if (summary.isSuccess) {
                innerPrefs.lastAttendanceTs.set(Instant.now().epochSecond)
                Result.success()
            } else if (!allowRepeatToday && runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            Timber.tag("Attendance").w("Attendance task failed: %s", error.message)
            notifications.showFailure(error.message ?: "未知错误")
            if (!allowRepeatToday && runAttemptCount < MAX_RETRIES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    private fun hasRunToday(): Boolean {
        val last = innerPrefs.lastAttendanceTs.get()
        if (last <= 0) return false
        val zone = ZoneId.of("Asia/Shanghai")
        return Instant.ofEpochSecond(last).atZone(zone).toLocalDate() ==
            Instant.now().atZone(zone).toLocalDate()
    }

    companion object {
        const val ALLOW_REPEAT_TODAY = "allow_repeat_attendance_today"
        const val REQUIRE_AUTO_ENABLED = "require_auto_attendance_enabled"
        private const val MAX_RETRIES = 3
    }
}
