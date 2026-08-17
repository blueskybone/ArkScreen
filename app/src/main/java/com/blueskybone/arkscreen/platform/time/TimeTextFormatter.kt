package com.blueskybone.arkscreen.platform.time

import android.content.res.Resources
import com.blueskybone.arkscreen.R
import com.blueskybone.arkscreen.domain.time.DurationParts
import com.blueskybone.arkscreen.domain.time.durationParts

class TimeTextFormatter(private val resources: Resources) {

    fun shortDuration(totalSeconds: Long): String =
        buildDuration(durationParts(totalSeconds), Style.SHORT)

    fun fullDuration(totalSeconds: Long): String =
        buildDuration(durationParts(totalSeconds), Style.FULL)

    fun compactDuration(totalSeconds: Long): String =
        buildDuration(durationParts(totalSeconds), Style.COMPACT)
            .ifEmpty { resources.getString(R.string.duration_restored) }

    private fun buildDuration(parts: DurationParts, style: Style): String = buildString {
        appendPart(parts.weeks, style.week)
        appendPart(parts.days, style.day)
        appendPart(parts.hours, style.hour)
        appendPart(parts.minutes, style.minute)
    }

    private fun StringBuilder.appendPart(value: Long, stringRes: Int) {
        if (value > 0) append(resources.getString(stringRes, value))
    }

    private enum class Style(
        val week: Int,
        val day: Int,
        val hour: Int,
        val minute: Int,
    ) {
        SHORT(
            R.string.duration_week_short,
            R.string.duration_day_short,
            R.string.duration_hour_short,
            R.string.duration_minute_short,
        ),
        FULL(
            R.string.duration_week_short,
            R.string.duration_day_short,
            R.string.duration_hour_full,
            R.string.duration_minute_full,
        ),
        COMPACT(
            R.string.duration_week_compact,
            R.string.duration_day_compact,
            R.string.duration_hour_compact,
            R.string.duration_minute_compact,
        ),
    }
}
