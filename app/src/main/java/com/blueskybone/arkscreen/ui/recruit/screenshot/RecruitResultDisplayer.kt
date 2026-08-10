package com.blueskybone.arkscreen.presentation.recruit.floating

import android.content.Context
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.ui.recruit.screenshot.FloatWindowController
import com.hjq.toast.Toaster

class RecruitResultDisplayer(
    private val floatWindowController: FloatWindowController
) {

    fun showError(message: String) {
        Toaster.show(message)
    }

    fun showResult(
        tags: List<String>,
        results: List<RecruitResult>,
        source: ScreenshotStartSource,
    ) {
        if (tags.isEmpty()) {
            Toaster.show("未识别到公招标签")
            return
        }

        if (results.isEmpty()) {
            Toaster.show("没有可用公招组合")
            return
        }

        floatWindowController.showResult(tags, results)
    }
}