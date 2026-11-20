package com.blueskybone.arkscreen.platform.notification

internal object ReminderTiming {
    fun canRestore(at: Long, expiresAt: Long, now: Long): Boolean =
        now - at <= 24 * 60 * 60 && expiresAt > now

    fun keepOverdue(at: Long, expiresAt: Long, now: Long, replaced: Boolean): Boolean =
        !replaced && at <= now && canRestore(at, expiresAt, now)
}
