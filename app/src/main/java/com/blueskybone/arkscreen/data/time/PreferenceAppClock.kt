package com.blueskybone.arkscreen.data.time

import com.blueskybone.arkscreen.data.local.pref.InnerPrefManager
import com.blueskybone.arkscreen.data.local.pref.SettingPrefManager
import com.blueskybone.arkscreen.domain.service.AppClock

class PreferenceAppClock(
    private val settings: SettingPrefManager,
    private val internal: InnerPrefManager,
) : AppClock {
    override fun currentEpochSeconds(): Long {
        val localTime = System.currentTimeMillis() / 1_000
        return if (settings.timeCorrect.get()) {
            localTime + internal.timeCorrectSec.get()
        } else {
            localTime
        }
    }
}
