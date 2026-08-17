package com.blueskybone.arkscreen.core.logger

object CrashLogger {
    fun install(tree: FileLoggingTree) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { tree.writeFatal(thread, throwable) }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
