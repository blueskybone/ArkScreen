package com.blueskybone.arkscreen.platform.time

import com.blueskybone.arkscreen.APP
import com.blueskybone.arkscreen.domain.service.AppClock
import com.blueskybone.arkscreen.domain.time.serverDayNumber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


object TimeUtils {
    private val zoneId = ZoneId.of("Asia/Shanghai")
    private val textFormatter by lazy { TimeTextFormatter(APP.resources) }

    fun getRemainTimeStr(sec: Long): String {
        return textFormatter.shortDuration(sec)
    }

    fun getLastUpdateStr(sec: Long): String {
        return textFormatter.fullDuration(sec)
    }

    fun getRemainTimeMinStr(sec: Long): String {
        return textFormatter.compactDuration(sec)
    }

    
    fun getTimeStr(epochMillis: Long, format: String = "MM-dd HH:mm:ss"): String =
        DateTimeFormatter.ofPattern(format)
            .withZone(zoneId)
            .format(Instant.ofEpochMilli(epochMillis))

    fun getCurrentTs(clock: AppClock): Long = clock.currentEpochSeconds()

    // 服务端日序号按北京时间计算。
    fun getDayNum(epochSeconds: Long): Long = serverDayNumber(epochSeconds)


    
    fun getTimeStrYMD(epochSeconds: Long): String {
        val localDate = Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")
        return localDate.format(formatter)
    }

    fun getDigitalString(hour: Int, min: Int): String {
        return String.format(Locale.ROOT, "%02d:%02d", hour, min)
    }
}
