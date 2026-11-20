package com.blueskybone.arkscreen.platform.notification

import org.junit.Assert.*
import org.junit.Test

class ReminderTimingTest {
    @Test fun syncKeepsOverdueUndeliveredCompletion() {
        assertTrue(ReminderTiming.keepOverdue(1000, Long.MAX_VALUE, 1100, false))
    }
    @Test fun freshRecoveryCycleReplacesOldCompletion() {
        assertFalse(ReminderTiming.keepOverdue(1000, Long.MAX_VALUE, 1100, true))
    }
    @Test fun expiredEarlyReminderIsNotSentAfterFullRecovery() {
        assertFalse(ReminderTiming.keepOverdue(1000, 1100, 1100, false))
    }
    @Test fun restartRestoresFutureAndRecentButNotOldEvents() {
        assertTrue(ReminderTiming.canRestore(2000, Long.MAX_VALUE, 1100))
        assertTrue(ReminderTiming.canRestore(1000, Long.MAX_VALUE, 1100))
        assertFalse(ReminderTiming.canRestore(1000, Long.MAX_VALUE, 87401))
    }
}
