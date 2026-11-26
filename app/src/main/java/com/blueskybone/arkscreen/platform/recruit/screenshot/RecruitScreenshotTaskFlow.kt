package com.blueskybone.arkscreen.platform.recruit.screenshot

import com.blueskybone.arkscreen.domain.model.recruit.AutoRecruitSession
import android.content.Context
import android.graphics.Bitmap
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotError
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotTaskFlow

class RecruitScreenshotTaskFlow(
    private val source: ScreenshotStartSource,
    private val recruitScreenshotFlow: RecruitScreenshotFlow,
    override val captureDelayMillis: Long,
    private val autoSession: AutoRecruitSession? = null,
    private val onComplete: ((Boolean) -> Unit)? = null,
) : ScreenshotTaskFlow {

    override val keepCaptureSessionAlive: Boolean
        get() = true

    override suspend fun onScreenshot(bitmap: Bitmap) {
        val success = recruitScreenshotFlow.run(
            bitmap = bitmap,
            autoSession = autoSession,
            source = source
        )
        onComplete?.invoke(success)
    }

    override suspend fun onFailure(error: ScreenshotError) {
        if (autoSession?.active != false) recruitScreenshotFlow.onScreenshotFailed(error)
        autoSession?.active = false
        onComplete?.invoke(false)
    }

}
