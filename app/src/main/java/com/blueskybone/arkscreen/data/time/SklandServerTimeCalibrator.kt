package com.blueskybone.arkscreen.data.time

import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.network.ApiService
import com.blueskybone.arkscreen.data.common.repositoryResultOf
import com.blueskybone.arkscreen.domain.service.ServerTimeCalibrator
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SklandServerTimeCalibrator(
    private val api: ApiService,
    private val internal: InnerPrefManager,
    private val objectMapper: ObjectMapper,
) : ServerTimeCalibrator {

    override suspend fun calibrate(): Result<Long> = repositoryResultOf {
        val requestStartedAt = System.currentTimeMillis()
        val response = api.getServerTimestamp()
        val requestFinishedAt = System.currentTimeMillis()
        val serverTimeMillis = parseDateHeader(response.headers()["Date"])
            ?: response.body()?.findValue("timestamp")?.asLong()?.toMillis()
            ?: response.errorBody()?.string()?.let(::parseTimestampFromBody)
            ?: throw IllegalStateException("服务器响应缺少可用时间")

        // 使用请求往返时间的中点，降低网络延迟对校准结果的影响。
        val localMidpoint = (requestStartedAt + requestFinishedAt) / 2
        val offsetSeconds = (serverTimeMillis - localMidpoint) / 1_000
        internal.timeCorrectSec.set(offsetSeconds)
        offsetSeconds
    }

    private fun parseDateHeader(value: String?): Long? = runCatching {
        value?.let {
            ZonedDateTime.parse(it, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toInstant()
                .toEpochMilli()
        }
    }.getOrNull()

    private fun parseTimestampFromBody(body: String): Long? = runCatching {
        objectMapper.readTree(body)
            .findValue("timestamp")
            ?.asLong()
            ?.toMillis()
    }.getOrNull()

    private fun Long.toMillis(): Long {
        // 兼容不同错误格式中以秒或毫秒表示的 Unix 时间戳。
        return if (this < 10_000_000_000L) this * 1_000 else this
    }
}
