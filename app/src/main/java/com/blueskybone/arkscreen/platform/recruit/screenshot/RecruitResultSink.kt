package com.blueskybone.arkscreen.platform.recruit.screenshot

import androidx.annotation.StringRes
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult

/** Presentation boundary for the Android capture/recognition pipeline. */
interface RecruitResultSink {
    fun showError(@StringRes messageRes: Int)
    fun showError(message: String)
    fun showResult(tags: List<String>, results: List<RecruitResult>)
}
