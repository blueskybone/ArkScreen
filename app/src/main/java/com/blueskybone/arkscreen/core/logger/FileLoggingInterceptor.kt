package com.blueskybone.arkscreen.core.logger

import android.content.Context
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

class FileLoggingInterceptor(context: Context) : HttpLoggingInterceptor.Logger {
    private val writer = RotatingLogWriter(logDirectory(context), "network")

    override fun log(message: String) {
        writer.write(message)
    }

    companion object {
        fun logDirectory(context: Context): File =
            File(context.externalCacheDir ?: context.cacheDir, "network_logs")
    }
}
