package com.blueskybone.arkscreen.presentation.recruit.floating

import android.content.Context
import androidx.annotation.StringRes
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.platform.screenshot.ScreenshotStartSource
import com.blueskybone.arkscreen.ui.recruit.screenshot.FloatWindowController
import com.hjq.toast.Toaster

class RecruitResultDisplayer(
    context: Context,
    private val floatWindowController: FloatWindowController
) {
    private val appContext = context.applicationContext

    fun showError(@StringRes messageRes: Int) {
        Toaster.show(appContext.getString(messageRes))
    }

    fun showError(message: String) {
        Toaster.show(message)
    }

    fun showResult(
        tags: List<String>,
        results: List<RecruitResult>,
        source: ScreenshotStartSource,
    ) {
        floatWindowController.showResult(tags, results)
    }
}
