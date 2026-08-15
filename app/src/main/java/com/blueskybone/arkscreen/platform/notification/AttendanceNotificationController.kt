package com.blueskybone.arkscreen.platform.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.model.attendance.AttendanceAccountResult
import com.blueskybone.arkscreen.domain.model.attendance.AttendanceSummary

class AttendanceNotificationController(context: Context) {
    private val appContext = context.applicationContext
    private val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                appContext.getString(R.string.attendance_notification_channel),
                NotificationManager.IMPORTANCE_DEFAULT,
            )
        )
    }

    fun showProgress(index: Int, total: Int, accountName: String) {
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                appContext.getString(R.string.attendance_notification_progress, index, total)
            )
            .setContentText(accountName)
            .setProgress(total, index - 1, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showAccountResult(result: AttendanceAccountResult) {
        val title = appContext.getString(
            if (result.isSuccess) R.string.attendance_success else R.string.attendance_failed
        )
        notify(title, "${result.accountName}：${result.displayMessage()}")
    }

    fun showFailure(message: String) {
        notify(
            appContext.getString(R.string.attendance_failed),
            message.substringAfterLast(": ")
                .ifBlank { appContext.getString(R.string.attendance_unknown_error) },
        )
    }

    fun showNoAccounts() {
        notify(
            appContext.getString(R.string.attendance_not_run),
            appContext.getString(R.string.attendance_no_accounts),
        )
    }

    fun showSummary(summary: AttendanceSummary) {
        val title = appContext.getString(
            if (summary.isSuccess) {
                R.string.attendance_completed
            } else {
                R.string.attendance_partially_failed
            }
        )
        val summaryText = appContext.getString(
            R.string.attendance_summary,
            summary.successCount,
            summary.failureCount,
        )
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText(summaryText)
        summary.results.forEach { result ->
            val status = appContext.getString(
                if (result.isSuccess) R.string.status_success else R.string.status_failed
            )
            val detail = result.displayMessage()
            style.addLine("${result.accountName}：$status $detail")
        }

        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(summaryText)
            .setStyle(style)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setOngoing(false)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun AttendanceAccountResult.displayMessage(): String {
        message?.takeIf { it.isNotBlank() }?.let { return it }
        return error?.message
            ?.substringAfterLast(": ")
            ?.takeIf { it.isNotBlank() }
            ?: appContext.getString(R.string.attendance_failed)
    }

    private fun notify(title: String, message: String) {
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setProgress(0, 0, false)
            .setOngoing(false)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHANNEL_ID = "attendance_notification"
        const val NOTIFICATION_ID = 1001
    }
}
