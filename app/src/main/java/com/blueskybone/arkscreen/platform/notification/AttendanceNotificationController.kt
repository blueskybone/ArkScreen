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
            NotificationChannel(CHANNEL_ID, "签到通知", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    fun showProgress(index: Int, total: Int, accountName: String) {
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("正在签到 ($index/$total)")
            .setContentText(accountName)
            .setProgress(total, index - 1, false)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showAccountResult(result: AttendanceAccountResult) {
        val title = if (result.isSuccess) "签到成功" else "签到失败"
        notify(title, "${result.accountName}：${result.displayMessage()}")
    }

    fun showFailure(message: String) {
        notify("签到失败", message.substringAfterLast(": ").ifBlank { "未知错误" })
    }

    fun showNoAccounts() {
        notify("未执行签到", "没有可签到的账号")
    }

    fun showSummary(summary: AttendanceSummary) {
        val title = if (summary.isSuccess) "签到完成" else "签到部分失败"
        val summaryText = "成功 ${summary.successCount}，失败 ${summary.failureCount}"
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
            .setSummaryText(summaryText)
        summary.results.forEach { result ->
            val status = if (result.isSuccess) "成功" else "失败"
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
            ?: "签到失败"
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
