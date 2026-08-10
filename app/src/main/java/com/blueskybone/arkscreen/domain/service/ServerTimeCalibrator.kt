package com.blueskybone.arkscreen.domain.service

interface ServerTimeCalibrator {
    /** 校准时钟并返回“服务器时间减本地时间”的秒数差值。 */
    suspend fun calibrate(): Result<Long>
}
