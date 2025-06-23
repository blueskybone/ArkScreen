package com.blueskybone.arkscreen.logger

/**
 *   Created by blueskybone
 *   Date: 2025/6/14
 */
import android.content.Context
import com.blueskybone.arkscreen.APP
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class FileLoggingInterceptor : HttpLoggingInterceptor.Logger {

    private val logDir = File(APP.externalCacheDir, "network_logs")
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
    private val executor = Executors.newSingleThreadExecutor()

    private val excludedUrls = listOf(
        "/api/v1/game/player/info"
    )

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        cleanupOldLogs()
    }

    override fun log(message: String) {
        if (shouldSkipLogging(message)) {
            return
        }
        executor.execute {
            try {
                val date = dateFormat.format(Date())
                val time = timeFormat.format(Date())
                val logFile = File(logDir, "network_$date.log")

                FileWriter(logFile, true).use { writer ->
                    writer.append("$time $message\n")
                }
            } catch (e: Exception) {
                // 静默处理，避免因日志记录失败影响网络请求
            }
        }
    }

    private fun cleanupOldLogs(daysToKeep: Int = 7) {
        val cutoff = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        logDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }

    private fun shouldSkipLogging(message: String): Boolean {
        return excludedUrls.any { url -> message.contains(url) }
    }

}