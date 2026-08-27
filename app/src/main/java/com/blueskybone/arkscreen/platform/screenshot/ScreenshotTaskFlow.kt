package com.blueskybone.arkscreen.platform.screenshot

import android.graphics.Bitmap

//用于解决授权成功后activity结束不返回，无法触发后续流程的问题
interface ScreenshotTaskFlow {
    val keepCaptureSessionAlive: Boolean get() = false
    val captureDelayMillis: Long get() = 1_000L

    suspend fun onScreenshot(bitmap: Bitmap)
    suspend fun onFailure(error: ScreenshotError) {}
}
