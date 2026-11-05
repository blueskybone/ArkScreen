package com.blueskybone.arkscreen.ui.common.view

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.blueskybone.arkscreen.R
import java.time.Instant
import java.time.ZoneId

enum class InfoStatusTone { SUCCESS, WARNING, ERROR, NEUTRAL }

fun TextView.renderInfoStatus(@StringRes textRes: Int, tone: InfoStatusTone) {
    val (container, content) = when (tone) {
        InfoStatusTone.SUCCESS -> R.color.status_success_container to R.color.status_success_content
        InfoStatusTone.WARNING -> R.color.status_warning_container to R.color.status_warning_content
        InfoStatusTone.ERROR -> R.color.status_error_container to R.color.status_error_content
        InfoStatusTone.NEUTRAL -> R.color.status_neutral_container to R.color.status_neutral_content
    }
    setText(textRes)
    backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, container))
    setTextColor(ContextCompat.getColor(context, content))
}

fun attendedToday(lastSuccessTs: Long, now: Instant = Instant.now()): Boolean {
    if (lastSuccessTs <= 0L) return false
    val zone = ZoneId.of("Asia/Shanghai")
    return Instant.ofEpochSecond(lastSuccessTs).atZone(zone).toLocalDate() ==
        now.atZone(zone).toLocalDate()
}
