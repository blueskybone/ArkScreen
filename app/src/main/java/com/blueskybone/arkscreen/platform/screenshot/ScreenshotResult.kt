package com.blueskybone.arkscreen.platform.screenshot

import android.graphics.Bitmap

sealed interface ScreenshotResult {
    data class Success(val bitmap: Bitmap) : ScreenshotResult
    data class Failure(val error: ScreenshotError) : ScreenshotResult
}