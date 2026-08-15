package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.graphics.Bitmap
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.usecase.recruit.CalcResultUseCase
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotError
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.presentation.recruit.floating.RecruitResultDisplayer
import com.blueskybone.arkscreen.ui.recruit.ocr.RecruitTagRecognizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class RecruitScreenshotFlow(
    private val tagRecognizer: RecruitTagRecognizer,
    private val calcRecruitResultUseCase: CalcResultUseCase,
    private val resultDisplayer: RecruitResultDisplayer,
) {

    suspend fun run(
        bitmap: Bitmap,
        source: ScreenshotStartSource
    ) {
        Timber.tag("RecruitFlow").d("Start recruit recognition: source=%s", source)
        val tags = tagRecognizer.recognize(bitmap).getOrElse { throwable ->
            Timber.tag("RecruitFlow").e(throwable, "Recognition stage failed")
            resultDisplayer.showError(R.string.recruit_recognition_failed)
            return
        }

        if (tags.isEmpty()) {
            resultDisplayer.showError(R.string.recruit_no_tags)
            return
        }

        Timber.tag("RecruitFlow").d("Calculate recruit combinations: tags=%s", tags)
        val calculation = withContext(Dispatchers.Default) {
            calcRecruitResultUseCase(tags, filter = true)
        }
        val results = calculation.getOrElse { throwable ->
            Timber.tag("RecruitFlow").e(throwable, "Recruit calculation failed: tags=%s", tags)
            resultDisplayer.showError(R.string.recruit_calculation_failed)
            return
        }
        Timber.tag("RecruitFlow").d(
            "Recruit calculation completed: tags=%s resultCount=%d results=%s",
            tags,
            results.size,
            results.map { result -> result.tags to result.operators.map { it.name } },
        )

        resultDisplayer.showResult(
            tags = tags,
            results = results,
            source = source
        )
    }

    suspend fun onScreenshotFailed(error: ScreenshotError) {
        error.message?.let(resultDisplayer::showError)
            ?: resultDisplayer.showError(R.string.screenshot_capture_failed)
    }
}
