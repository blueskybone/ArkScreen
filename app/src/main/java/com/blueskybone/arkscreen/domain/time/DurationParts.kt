package com.blueskybone.arkscreen.domain.time

data class DurationParts(
    val weeks: Long,
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
)

fun durationParts(totalSeconds: Long): DurationParts {
    val seconds = totalSeconds.coerceAtLeast(0)
    return DurationParts(
        weeks = seconds / SECONDS_PER_WEEK,
        days = (seconds % SECONDS_PER_WEEK) / SECONDS_PER_DAY,
        hours = (seconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR,
        minutes = (seconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE,
        seconds = seconds % SECONDS_PER_MINUTE,
    )
}

/** 鹰角服务端日序号按北京时间计算。 */
fun serverDayNumber(epochSeconds: Long): Long =
    (epochSeconds + CHINA_TIME_OFFSET_SECONDS) / SECONDS_PER_DAY

private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L
private const val SECONDS_PER_DAY = 86_400L
private const val SECONDS_PER_WEEK = 604_800L
private const val CHINA_TIME_OFFSET_SECONDS = 28_800L
