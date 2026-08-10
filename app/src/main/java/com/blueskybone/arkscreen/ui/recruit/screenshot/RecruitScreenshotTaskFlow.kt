package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.content.Context
import android.graphics.Bitmap
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotError
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotTaskFlow

class RecruitScreenshotTaskFlow(
    private val source: ScreenshotStartSource,
    private val recruitScreenshotFlow: RecruitScreenshotFlow,
) : ScreenshotTaskFlow {

    override suspend fun onScreenshot(bitmap: Bitmap) {
        recruitScreenshotFlow.run(
            bitmap = bitmap,
            source = source
        )
    }

    override suspend fun onFailure(error: ScreenshotError) {
        recruitScreenshotFlow.onScreenshotFailed(error)
    }

}