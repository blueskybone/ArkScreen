package com.blueskybone.arkscreen.presentation.recruit.floating

import android.content.Context
import androidx.annotation.StringRes
import com.blueskybone.arkscreen.domain.model.recruit.RecruitResult
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.ui.common.bindinginfo.RecruitMode
import com.blueskybone.arkscreen.ui.recruit.screenshot.FloatWindowController
import com.hjq.toast.Toaster

class RecruitResultDisplayer(
    context: Context,
    private val floatWindowController: FloatWindowController,
    private val settings: SettingPrefManager,
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
    ) {
        when (settings.recruitMode.get()) {
            RecruitMode.TOAST -> showToastResult(tags, results)
            RecruitMode.AUTO -> {
                val bestRarity = results.firstOrNull()?.rare
                if (bestRarity in AUTO_FLOAT_RARITIES) {
                    floatWindowController.showResult(tags, results)
                } else {
                    showToastResult(tags, results)
                }
            }
            else -> floatWindowController.showResult(tags, results)
        }
    }

    private fun showToastResult(tags: List<String>, results: List<RecruitResult>) {
        floatWindowController.close()
        val best = results.firstOrNull()
        if (best == null) {
            Toaster.show(appContext.getString(R.string.recruit_no_high_rarity_result))
            return
        }
        val operatorName = best.operators
            .firstOrNull { operator -> operator.rare == best.rare }
            ?.name
            ?: best.operators.firstOrNull()?.name.orEmpty()
        Toaster.show(
            appContext.getString(
                R.string.recruit_toast_result,
                best.tags.ifEmpty { tags }.joinToString(" "),
                best.rare,
                operatorName,
            )
        )
    }

    private companion object {
        val AUTO_FLOAT_RARITIES = setOf(1, 5, 6)
    }
}
