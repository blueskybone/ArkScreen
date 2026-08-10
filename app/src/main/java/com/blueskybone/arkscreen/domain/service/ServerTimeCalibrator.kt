package com.blueskybone.arkscreen.domain.service

interface ServerTimeCalibrator {
    /** Calibrates the clock and returns server time minus local time, in seconds. */
    suspend fun calibrate(): Result<Long>
}
