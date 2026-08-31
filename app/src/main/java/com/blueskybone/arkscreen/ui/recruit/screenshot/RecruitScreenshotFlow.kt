package com.blueskybone.arkscreen.ui.recruit.screenshot

import android.graphics.Bitmap
import android.os.SystemClock
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
        val startedAt = SystemClock.elapsedRealtime()
        Timber.tag("RecruitFlow").i(
            "Recognition started: source=%s bitmap=%dx%d",
            source,
            bitmap.width,
            bitmap.height,
        )
        val tags = tagRecognizer.recognize(bitmap).getOrElse { throwable ->
            Timber.tag("RecruitFlow").e(throwable, "Recognition stage failed")
            resultDisplayer.showError(R.string.recruit_recognition_failed)
            return
        }

        if (tags.isEmpty()) {
            Timber.tag("RecruitFlow").w(
                "Recognition completed without tags: source=%s durationMs=%d",
                source,
                SystemClock.elapsedRealtime() - startedAt,
            )
            resultDisplayer.showError(R.string.recruit_no_tags)
            return
        }

        Timber.tag("RecruitFlow").i("Calculate combinations: tagCount=%d", tags.size)
        Timber.tag("RecruitFlow").d("Calculate combinations: tags=%s", tags)
        val calculation = withContext(Dispatchers.Default) {
            calcRecruitResultUseCase(tags, filter = true)
        }
        val results = calculation.getOrElse { throwable ->
            Timber.tag("RecruitFlow").e(throwable, "Recruit calculation failed: tags=%s", tags)
            resultDisplayer.showError(R.string.recruit_calculation_failed)
            return
        }
        Timber.tag("RecruitFlow").i(
            "Recognition completed: source=%s tagCount=%d resultCount=%d durationMs=%d",
            source,
            tags.size,
            results.size,
            SystemClock.elapsedRealtime() - startedAt,
        )
        Timber.tag("RecruitFlow").d(
            "Calculation details: tags=%s results=%s",
            tags,
            results.map { result -> result.tags to result.operators.map { it.name } },
        )

        resultDisplayer.showResult(
            tags = tags,
            results = results,
        )
    }

    suspend fun onScreenshotFailed(error: ScreenshotError) {
        error.message?.let(resultDisplayer::showError)
            ?: resultDisplayer.showError(R.string.screenshot_capture_failed)
    }
}
