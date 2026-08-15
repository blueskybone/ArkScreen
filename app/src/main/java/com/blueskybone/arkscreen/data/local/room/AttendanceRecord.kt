package com.blueskybone.arkscreen.data.local.room

import androidx.room.Entity

@Entity(primaryKeys = ["accountType", "accountUid"])
data class AttendanceRecord(
    val accountType: String,
    val accountUid: String,
    val lastAttemptTs: Long,
    val lastSuccessTs: Long,
    val lastError: String,
)
