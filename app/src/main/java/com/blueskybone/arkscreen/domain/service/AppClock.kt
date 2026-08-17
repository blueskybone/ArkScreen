package com.blueskybone.arkscreen.domain.service

/** 应用内时间计算和请求签名统一使用的 Unix 时间来源。 */
interface AppClock {
    fun currentEpochSeconds(): Long
}
