package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.content.Context
import android.graphics.Bitmap
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotError
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotTaskFlow

class RecruitScreenshotTaskFlow(
    private val source: ScreenshotStartSource,
    private val recruitScreenshotFlow: RecruitScreenshotFlow,
    override val captureDelayMillis: Long,
    private val onComplete: ((Boolean) -> Unit)? = null,
) : ScreenshotTaskFlow {

    override val keepCaptureSessionAlive: Boolean
        get() = true

    override suspend fun onScreenshot(bitmap: Bitmap) {
        val success = recruitScreenshotFlow.run(
            bitmap = bitmap,
            source = source
        )
        onComplete?.invoke(success)
    }

    override suspend fun onFailure(error: ScreenshotError) {
        onComplete?.invoke(false)
        recruitScreenshotFlow.onScreenshotFailed(error)
    }

}
