package com.blueskybone.arkscreen.domain.service

/** Single source of epoch time for application calculations and signed requests. */
interface AppClock {
    fun currentEpochSeconds(): Long
}
