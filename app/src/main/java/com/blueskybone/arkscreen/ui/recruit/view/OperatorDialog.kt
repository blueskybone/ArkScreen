package com.blueskybone.arkscreen.ui.recruit

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.blueskybone.arkscreen.domain.model.recruit.RecruitOpe
import java.net.URLEncoder

class OperatorDialog(
    private val context: Context
) {
    fun show(operator: RecruitOpe) {
        AlertDialog.Builder(context)
            .setTitle(operator.name)
            .setMessage(operator.tags.joinToString(" / "))
            .setPositiveButton("打开 PRTS") { _, _ ->
                val url = "https://prts.wiki/w/" + URLEncoder.encode(operator.name, "UTF-8")
                // 这里调用你原来的 openLink
            }
            .setNegativeButton("关闭", null)
            .show()
    }
}