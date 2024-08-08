package com.blueskybone.arkscreen.util

import android.annotation.SuppressLint
import com.blueskybone.arkscreen.base.PrefManager
import org.koin.java.KoinJavaComponent.getKoin
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale
import java.util.TimeZone


object TimeUtils {
    data class WDHMS(
        val week: Long,
        val day: Long,
        val hour: Long,
        val min: Long,
        val sec: Long
    )

    private val zoneId = ZoneId.of("Asia/Shanghai")

    //time to n day n hour n min
    private fun getMinusWDHMS(sec: Long): WDHMS {
        val w = sec / 604800
        val d = (sec % 604800) / 86400
        val h = (sec % 86400) / 3600
        val m = (sec % 3600) / 60
        val s = sec % 60
        return WDHMS(w, d, h, m, s)
    }

    private fun getWDHMSToStr(wdhms: WDHMS): String {
        val stringBuilder = StringBuilder()
        if (wdhms.week > 0) stringBuilder.append("${wdhms.week}周")
        if (wdhms.day > 0) stringBuilder.append("${wdhms.day}天")
        if (wdhms.hour > 0) stringBuilder.append("${wdhms.hour}时")
        if (wdhms.min > 0) stringBuilder.append("${wdhms.min}分")

        return stringBuilder.toString()
    }

    fun getRemainTimeStr(sec: Long): String {
        return getWDHMSToStr(getMinusWDHMS(sec))
    }

    @SuppressLint("SimpleDateFormat")
    fun getTimeStr(ts: Long, format: String = "MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(format)
        sdf.timeZone = TimeZone.getTimeZone(zoneId)
        val date = Date(ts)
        return sdf.format(date)
    }

    fun getCurrentTs(): Long {
        val prefManager: PrefManager by getKoin().inject()
        return if (prefManager.isSklandCorrectOn.get()) {
            prefManager.CorrectionTs.get() + System.currentTimeMillis() / 1000
        } else {
            System.currentTimeMillis() / 1000
        }
    }

    //TODO:fix
    //follow the time util api, a new week begin from Sunday
    //beijing time, 00:00 divide two days
    //follow ark-time, ts should add one day + 4 hours = (86400 + 3600*4)*1000
    fun getWeekNum(ts: Long): Long {
        val tsc = ts + 100800000L
        val weekFields = WeekFields.of(Locale.CHINA)
        val weekNum = Instant.ofEpochMilli(tsc)
            .atZone(zoneId)
            .get(weekFields.weekOfWeekBasedYear())
        return weekNum.toLong()
    }

    fun getMonthNum(ts: Long): Long {
        val monthNum = Instant.ofEpochMilli(ts)
            .atZone(zoneId)
            .get(ChronoField.MONTH_OF_YEAR)
        return monthNum.toLong()
    }

    //TODO:check this.
    fun getDayNum(ts: Long): Long {
        return (ts + 28800) / 86400
    }

    fun getLoggerTimeStr(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val formatted = current.format(formatter)
        return "[$formatted]"
    }
}