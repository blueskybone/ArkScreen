package com.blueskybone.arkscreen.platform.screenshot

interface ScreenshotCapturer {
    suspend fun captureOnce(delayMillis: Long): ScreenshotResult

    fun close() = Unit
}
