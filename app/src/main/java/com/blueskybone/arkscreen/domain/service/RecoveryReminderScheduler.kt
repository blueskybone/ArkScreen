package com.blueskybone.arkscreen.domain.service

import com.blueskybone.arkscreen.domain.model.account.AccountSk
import com.blueskybone.arkscreen.domain.model.realtime.RealTimeData

interface RecoveryReminderScheduler {
    suspend fun onRealtimeSynced(account: AccountSk, data: RealTimeData)
}
