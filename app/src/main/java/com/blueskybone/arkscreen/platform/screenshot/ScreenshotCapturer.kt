package com.blueskybone.arkscreen.platform.screenshot

interface ScreenshotCapturer {
    suspend fun captureOnce(): ScreenshotResult
}