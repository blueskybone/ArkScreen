package com.blueskybone.arkscreen.core.logger

import android.content.Context
import android.util.Log
import timber.log.Timber
import java.io.File

class FileLoggingTree(
    context: Context,
    private val minimumPriority: Int = Log.WARN,
) : Timber.Tree() {
    private val writer = RotatingLogWriter(logDirectory(context), "app")

    override fun isLoggable(tag: String?, priority: Int): Boolean = priority >= minimumPriority

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        val priorityName = when (priority) {
            Log.VERBOSE -> "V"
            Log.DEBUG -> "D"
            Log.INFO -> "I"
            Log.WARN -> "W"
            Log.ERROR -> "E"
            Log.ASSERT -> "A"
            else -> "?"
        }
        val throwable = t?.let { "\n${Log.getStackTraceString(it)}" }.orEmpty()
        writer.write("$priorityName/${tag ?: "ArkScreen"}: $message$throwable")
    }

    companion object {
        fun logDirectory(context: Context): File =
            File(context.externalCacheDir ?: context.cacheDir, "logs")
    }
}
