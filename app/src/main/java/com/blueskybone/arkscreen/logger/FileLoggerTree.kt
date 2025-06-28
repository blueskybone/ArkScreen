package com.blueskybone.arkscreen.logger

/**
 *   Created by blueskybone
 *   Date: 2025/6/13
 */
import android.annotation.SuppressLint
import timber.log.Timber
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log
import com.blueskybone.arkscreen.APP

@SuppressLint("ConstantLocale")
class FileLoggingTree : Timber.Tree() {

    companion object{
        val logDir = File(APP.externalCacheDir, "logs")
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        private const val MAX_FILE_SIZE = 1024 * 1024 // 1MB
        private const val MAX_FILE_COUNT = 5 // 最多保留5个日志文件
    }

    init {
        if (!logDir.exists()) {
            logDir.mkdirs()
        }
        cleanupOldLogs()
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        try {
            val date = dateFormat.format(Date())
            val time = timeFormat.format(Date())
            val logFile = File(logDir, "app_$date.log")

            val logMessage = StringBuilder()
                .append(time)
                .append(" ")
                .append(getPriorityChar(priority))
                .append("/")
                .append(tag ?: "?")
                .append(": ")
                .append(message)
                .append("\n")

            if (t != null) {
                logMessage.append(Log.getStackTraceString(t)).append("\n")
            }

            // 检查文件大小，如果过大则滚动
            if (logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
                rollOver(logFile, date)
            }

            FileWriter(logFile, true).use { writer ->
                writer.append(logMessage.toString())
            }
        } catch (e: Exception) {
            // 避免因日志记录失败导致应用崩溃
        }
    }

    private fun getPriorityChar(priority: Int): Char {
        return when (priority) {
            Log.VERBOSE -> 'V'
            Log.DEBUG -> 'D'
            Log.INFO -> 'I'
            Log.WARN -> 'W'
            Log.ERROR -> 'E'
            Log.ASSERT -> 'A'
            else -> '?'
        }
    }

    private fun rollOver(currentFile: File, date: String) {
        for (i in MAX_FILE_COUNT - 1 downTo 1) {
            val src = File(logDir, "app_${date}_${i}.log")
            val dst = File(logDir, "app_${date}_${i + 1}.log")
            if (src.exists()) {
                src.renameTo(dst)
            }
        }
        currentFile.renameTo(File(logDir, "app_${date}_1.log"))
    }

    private fun cleanupOldLogs() {
        val files = logDir.listFiles()?.sortedBy { it.lastModified() }
        files?.let {
            if (it.size > MAX_FILE_COUNT) {
                for (i in 0 until it.size - MAX_FILE_COUNT) {
                    it[i].delete()
                }
            }
        }
    }
}