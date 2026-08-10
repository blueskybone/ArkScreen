package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.graphics.Bitmap
import com.blueskybone.arkscreen.domain.usecase.recruit.CalcResultUseCase
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotError
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.presentation.recruit.floating.RecruitResultDisplayer
import com.blueskybone.arkscreen.ui.recruit.ocr.RecruitTagRecognizer

class RecruitScreenshotFlow(
    private val tagRecognizer: RecruitTagRecognizer,
    private val calcRecruitResultUseCase: CalcResultUseCase,
    private val resultDisplayer: RecruitResultDisplayer,
) {

    suspend fun run(
        bitmap: Bitmap,
        source: ScreenshotStartSource
    ) {
        val tags = tagRecognizer.recognize(bitmap).getOrElse {
            resultDisplayer.showError("识别公招标签失败")
            return
        }

        if (tags.isEmpty()) {
            resultDisplayer.showError("未识别到公招标签")
            return
        }

        val results = calcRecruitResultUseCase(tags).getOrElse {
            resultDisplayer.showError("计算公招结果失败")
            return
        }

        resultDisplayer.showResult(
            tags = tags,
            results = results,
            source = source
        )
    }

    suspend fun onScreenshotFailed(error: ScreenshotError) {
        resultDisplayer.showError(error.message ?: "截图失败")
    }
}